package com.mccarty.currentdeviceinfo

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider.getUriForFile
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.mccarty.currentdeviceinfo.ui.theme.CurrentDeviceInfoTheme
import com.mccarty.currentdeviceinfo.ui.theme.MainViewModel
import com.mccarty.currentdeviceinfo.ui.theme.components.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_REQUEST_INTERVAL)
        .setWaitForAccurateLocation(false)
        .setMinUpdateIntervalMillis(MIN_UPDATE_INTERVAL)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            for (location in result.locations) { // TODO: reduce number of updates?
                mainViewModel.setPosition(
                    this@MainActivity.getString(R.string.csv_file_header),
                    Position(
                        lat = location.latitude,
                        lon = location.longitude,
                        time = location.time
                    ),
                )
            }
        }
    }

    private val locationPermissionRequest =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                    try {
                        fusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            Looper.getMainLooper(),
                        )
                    } catch (se: SecurityException) {
                        Timber.e(se.message ?: "An error occurred while trying to request location updates")
                    }
                }

                else -> {
                    Timber.d("Startup location not granted")
                }
            }
        }

    private val networkRequest = NetworkRequest.Builder()
        .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)
        .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        .build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            mainViewModel.hasInternetConnection(true)
            mainViewModel.setIpAddress()
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            // Not metered means device is connected to wifi, metered is cellular
            val notMetered = networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
            mainViewModel.isWifiNetwork(notMetered)
            mainViewModel.setIpAddress()
        }

        override fun onUnavailable() {
            super.onUnavailable()
            mainViewModel.hasInternetConnection(false)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            mainViewModel.hasInternetConnection(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CurrentDeviceInfoTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val wifiIpAddress = mainViewModel.wifiIpAddress.collectAsStateWithLifecycle().value
                    val cellIpAddress = mainViewModel.cellIpAddress.collectAsStateWithLifecycle().value
                    val currentLocalTime = mainViewModel.currentLocalTime.collectAsStateWithLifecycle().value
                    val currentUtcTime = mainViewModel.currentUtcTime.collectAsStateWithLifecycle().value
                    val currentPosition = mainViewModel.currentPosition.collectAsStateWithLifecycle().value

                    MainScreen(
                        currentLocalTime = currentLocalTime,
                        currentUtcTime = currentUtcTime,
                        wifiIpAddress = wifiIpAddress,
                        cellIpAddress = cellIpAddress,
                        currentPosition = currentPosition,
                        onClick = {
                            val filePath = File(this@MainActivity.filesDir.absolutePath, "/")
                            val newFile = File(filePath, DEVICE_OUTPUT)
                            val contentUri: Uri =
                                getUriForFile(this@MainActivity, FILE_AUTHORITY, newFile)

                            startForResult.launch(Intent(Intent.ACTION_VIEW).apply {
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                                setDataAndType(contentUri, "*/*")
                            })
                        },
                    )
                }
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                mainViewModel.setPosition(
                    this@MainActivity.getString(R.string.csv_file_header),
                    Position(
                        lat = it.latitude,
                        lon = it.longitude,
                        time = it.time
                    ),
                )
            }
        }

        checkPermission()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            Timber.d("Return from file export ${result.resultCode}")
        }

    private fun checkPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }

            else -> {
                requestPermissions()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        getConnectivityManager().registerNetworkCallback(networkRequest, networkCallback)
        lifecycleScope.launch {
            mainViewModel.startCurrentLocalTimeJob()
            mainViewModel.startCurrentUtcTimeJob()
        }
    }

    override fun onPause() {
        super.onPause()
        getConnectivityManager().unregisterNetworkCallback(networkCallback)
        mainViewModel.cancelCurrentLocalTimeJob()
        mainViewModel.cancelCurrentUtcTimeJob()
    }

    private fun requestPermissions() {
        locationPermissionRequest.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
        )
    }

    private fun getConnectivityManager() =
        this@MainActivity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    data class Position(val lat: Double? = 0.0, val lon: Double? = 0.0, val time: Long? = 0)

    companion object {
        const val FILE_AUTHORITY = "com.mccarty.fileprovider"
        const val DEVICE_OUTPUT = "current_device_data.txt"
        const val LOCATION_REQUEST_INTERVAL = 10_000L
        const val MIN_UPDATE_INTERVAL = 5_000L
    }
}