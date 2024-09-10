package com.mccarty.currentdeviceinfo

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.mccarty.currentdeviceinfo.ui.theme.CurrentDeviceInfoTheme
import com.mccarty.currentdeviceinfo.ui.theme.MainViewModel
import com.mccarty.currentdeviceinfo.ui.theme.components.MainScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        when {
            permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                println("MainActivity ***** FINE GRANTED")
            }

            else -> {
                println("MainActivity ***** NOT GRANTED")
            }
        }
    }
    private val connectivityReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            println("MainActivity ***** onReceive ${intent.toString()}")
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
            println("MainActivity ***** onAvailable UNMETERED")
        }

        @RequiresApi(Build.VERSION_CODES.Q)
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)

            // Not metered means it is on wifi, metered is cellular
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

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                mainViewModel.setPosition(Position(lat = it.latitude, lon = it.longitude))


                println("MainActivity ***** LAT ${it.latitude}")
                println("MainActivity ***** LON  ${it.latitude}")
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
/*        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, object : CancellationToken() {
            override fun onCanceledRequested(p0: OnTokenCanceledListener): CancellationToken {
               return CancellationToken()
            }

            override fun isCancellationRequested(): Boolean = false
        })*/

        when {
            ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                println("MainActivity ***** GRANTED onCreate")
            }

            else -> {
                requestPermissions()
            }
        }

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

                    })
                }
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

    data class Position(val lat: Double = 0.0, val lon: Double = 0.0)
}