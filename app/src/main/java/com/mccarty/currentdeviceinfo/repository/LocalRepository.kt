package com.mccarty.currentdeviceinfo.repository

interface LocalRepository {
    suspend fun writeDeviceDetailsCsv(vararg deviceData: String)
}