package com.mccarty.currentdeviceinfo.domain.usecase

interface GetIpAddress {
    suspend fun getIpAddress(): String
}