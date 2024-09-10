package com.mccarty.currentdeviceinfo.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.isActive
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class GetDataTimeUseCase @Inject constructor(
    private val defaultDispatcher: CoroutineDispatcher
) : GetDataTime {
        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun getCurrentLocalTime(): Flow<String> = flow {
            //coroutineScope {
                while (true) {
                    delay(TIME_DELAY)
                    emit(LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT_12)))
                }
            //}
        }.buffer().flowOn(defaultDispatcher)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getCurrentUtcTime(): Flow<String> = flow {
        //coroutineScope {
            while (true) {
                delay(TIME_DELAY)
                emit(OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT_24)))
            }
        //}
    }

    companion object {
        const val TIME_DELAY = 1_000L
        const val TIME_DISPLAY_FORMAT_12 = "HH:mm:ss a"
        const val TIME_DISPLAY_FORMAT_24 = "HH:mm:ss"
    }
}