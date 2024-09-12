package com.mccarty.currentdeviceinfo.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mccarty.currentdeviceinfo.MainActivity
import com.mccarty.currentdeviceinfo.domain.usecase.GetDataTime
import com.mccarty.currentdeviceinfo.domain.usecase.GetIpAddress
import com.mccarty.currentdeviceinfo.domain.usecase.HandleCsvFile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.StringBuilder
import javax.inject.Inject
import javax.inject.Named

@HiltViewModel
class MainViewModel @Inject constructor(
    private val getDataTime: GetDataTime,
    private val handleCsvFile: HandleCsvFile,
    private val getIpAddress: GetIpAddress,
    @Named("default")
    defaultDispatcher: CoroutineDispatcher,
) : ViewModel() {

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

    var hasCellularConnection = false
        private set

    var hasWifiConnection = false
        private set

    var hasInternet = false
        private set

    var localTimeJob: Job? = null
        private set

    var utcTimeJob: Job? = null
       private set

    fun setIpAddress() {
        if (hasWifiConnection) {
            _wifiIpAddress.value = getIpAddress.getIpAddress()
            _cellIpAddress.value = null
        } else if (hasCellularConnection) {
            _wifiIpAddress.value = null
            _cellIpAddress.value = getIpAddress.getIpAddress()
        }
    }

    fun hasInternetConnection(hasInternet: Boolean) {
        this.hasInternet = hasInternet
    }

    fun isWifiNetwork(isWifi: Boolean) {
        if (isWifi) {
            hasWifiConnection = true
            hasCellularConnection = false
        } else {
            hasWifiConnection = false
            hasCellularConnection = true
        }
    }

    suspend fun startCurrentLocalTimeJob() {
        if (localTimeJob == null) {
            localTimeJob = scope.launch {
                getDataTime.getCurrentLocalTime().cancellable().catch {
                    Timber.e(it.message ?: "An error occurred while trying to get the time")
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
                    Timber.e(it.message ?: "An error occurred while trying to get the time")
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

    fun setPosition(header: String, position: MainActivity.Position) {
        _currentPosition.value = position

        val builder = StringBuilder()
        builder.append("${_currentLocalTime.value},")
        builder.append("${_currentUtcTime.value},")
        builder.append("${_currentPosition.value.lat.toString()},")
        builder.append("${_currentPosition.value.lon.toString()},")
        builder.append("${_wifiIpAddress.value},")
        builder.append("${_cellIpAddress.value}\n")

        viewModelScope.launch {
            handleCsvFile.appendCsvFile(header, builder.toString())
        }
    }
}