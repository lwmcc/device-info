package com.mccarty.currentdeviceinfo.domain.usecase

interface HandleCsvFile {
    suspend fun appendCsvFile(vararg deviceData: String)
}