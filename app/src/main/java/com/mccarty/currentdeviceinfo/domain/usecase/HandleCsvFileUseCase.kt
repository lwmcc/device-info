package com.mccarty.currentdeviceinfo.domain.usecase

import com.mccarty.currentdeviceinfo.repository.LocalRepository
import javax.inject.Inject

class HandleCsvFileUseCase @Inject constructor(private val localRepository: LocalRepository): HandleCsvFile {
    override suspend fun appendCsvFile(vararg deviceData: String) {
        localRepository.writeDeviceDetailsCsv(*deviceData)
    }
}