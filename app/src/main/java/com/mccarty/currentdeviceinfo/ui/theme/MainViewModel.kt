package com.mccarty.currentdeviceinfo.ui.theme

import android.net.ConnectivityManager
import android.net.LinkProperties
import androidx.lifecycle.ViewModel
import com.mccarty.currentdeviceinfo.MainActivity
import com.mccarty.currentdeviceinfo.domain.usecase.GetDataTime
import com.mccarty.currentdeviceinfo.domain.usecase.NetworkState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val connectivityManager: ConnectivityManager,
    private val networkState: NetworkState,
    private val getDataTime: GetDataTime,
    private val defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _hasCellularConnection = MutableStateFlow<Boolean>(false)
    val hasCellularConnection = _hasCellularConnection

    private val _hasWifiConnection = MutableStateFlow<Boolean>(false)
    val hasWifiConnection = _hasWifiConnection

    private val _hasInternet = MutableStateFlow<Boolean>(false)
    val hasInternet = _hasInternet

    private val _cellIpAddress = MutableStateFlow<String?>(null)
    val cellIpAddress = _cellIpAddress

    private val _wifiIpAddress = MutableStateFlow<String?>(null)
    val wifiIpAddress = _wifiIpAddress

    private val _currentLocalTime = MutableStateFlow<String?>(null)
    val currentLocalTime = _currentLocalTime

    private val _currentUtcTime = MutableStateFlow<String?>(null)
    val currentUtcTime = _currentUtcTime

    private val _currentPosition = MutableStateFlow(MainActivity.Position())
    val currentPosition = _currentPosition

    private val parentScope = SupervisorJob()
    private val scope = CoroutineScope(defaultDispatcher + parentScope)

    private var localTimeJob: Job? = null
    private var utcTimeJob: Job? = null

    fun setIpAddress() {
        val linkProperties = connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties

        val address = linkProperties.linkAddresses.firstOrNull() {
            !it.address.isLoopbackAddress
                    && !it.address.isLinkLocalAddress
                    && networkState.ipAddressPattern.matcher(it.address.hostAddress as CharSequence).matches()
        }.toString()

        if (hasWifiConnection.value) {
            wifiIpAddress.value = address
            cellIpAddress.value = null
        } else if (hasCellularConnection.value) {
            wifiIpAddress.value = null
            cellIpAddress.value = address
        }
    }

    fun hasInternetConnection(hasInternet: Boolean) {
        _hasInternet.value = hasInternet
    }

    fun isWifiNetwork(isWifi: Boolean) {
        if (isWifi) {
            hasWifiConnection.value = true
            hasCellularConnection.value = false
        } else {
            hasWifiConnection.value = false
            hasCellularConnection.value = true
        }
    }

    fun wifiAndCellDisconnected() {
        hasWifiConnection.value = false
        hasCellularConnection.value = false
    }

    suspend fun startCurrentLocalTimeJob() {
        if (localTimeJob == null) {
            localTimeJob = scope.launch {
                getDataTime.getCurrentLocalTime().cancellable().catch {
                    // TODO: long and handle error
                }.collect { time ->
                    _currentLocalTime.value = time
                }
            }
        }
    }

    fun startCurrentUtcTimeJob() {
        if (utcTimeJob == null) {
            utcTimeJob = scope.launch {
                getDataTime.getCurrentUtcTime().cancellable().catch {
                    // TODO: log and handle error
                }.collect { time ->
                    _currentUtcTime.value = time
                }
            }
        }
    }

    fun cancelCurrentLocalTimeJob() {
        localTimeJob?.cancel()
        localTimeJob = null
    }

    fun cancelCurrentUtcTimeJob() {
        utcTimeJob?.cancel()
        utcTimeJob = null
    }

    fun setPosition(position: MainActivity.Position) {
        _currentPosition.value = position
    }
}