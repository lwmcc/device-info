package com.mccarty.currentdeviceinfo.domain.usecase

import android.net.ConnectivityManager
import android.net.LinkProperties
import com.mccarty.currentdeviceinfo.ipAddressPattern
import javax.inject.Inject

class GetIpAddressUseCase @Inject constructor(private val connectivityManager: ConnectivityManager) :
    GetIpAddress {
    override suspend fun getIpAddress(): String {
        val linkProperties =
            connectivityManager.getLinkProperties(connectivityManager.activeNetwork) as LinkProperties
        return linkProperties.linkAddresses.firstOrNull {
            !it.address.isLoopbackAddress
                    && !it.address.isLinkLocalAddress
                    && ipAddressPattern.matcher(it.address.hostAddress as CharSequence).matches()
        }.toString()
    }
}