package com.mccarty.currentdeviceinfo.domain.usecase

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.Collections
import java.util.regex.Matcher
import java.util.regex.Pattern
import javax.inject.Inject


class NetworkState @Inject constructor(private val connectivityManager: ConnectivityManager) {
    val ipAddressPattern = Pattern.compile(
        "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                + "|[1-9][0-9]|[0-9]))"
    )

    // TODO: flatten those loops
    fun getCellularIpAddress() {
        val interfaces: List<NetworkInterface> =
            Collections.list(NetworkInterface.getNetworkInterfaces())

        for (intf in interfaces) {
            val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
            for (addr in addrs) {
                if (!addr.isLoopbackAddress && !addr.isLinkLocalAddress) {
                    val matcher: Matcher =
                        ipAddressPattern.matcher(addr.hostAddress as CharSequence)
                    if (matcher.matches()) {
                        println("NetworkState ***** IS IP ${addr.hostAddress}")
                    }
                }
            }
        }
    }
}