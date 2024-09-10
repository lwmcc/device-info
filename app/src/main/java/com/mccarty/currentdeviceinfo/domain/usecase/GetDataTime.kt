package com.mccarty.currentdeviceinfo.domain.usecase

import kotlinx.coroutines.flow.Flow

interface GetDataTime {
    suspend fun getCurrentLocalTime(): Flow<String>
    suspend fun getCurrentUtcTime(): Flow<String>
}