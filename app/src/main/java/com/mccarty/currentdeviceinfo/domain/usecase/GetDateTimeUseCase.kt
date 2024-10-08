package com.mccarty.currentdeviceinfo.domain.usecase

import android.os.Build
import androidx.annotation.RequiresApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import java.time.LocalTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Named

class GetDateTimeUseCase @Inject constructor(
    @Named("default")
    private val defaultDispatcher: CoroutineDispatcher,
) : GetDateTime {
        @RequiresApi(Build.VERSION_CODES.O)
        override suspend fun getCurrentLocalTime(): Flow<String> = flow {
                while (true) {
                    delay(TIME_DELAY)
                    emit(LocalTime.now().format(DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT)))
                }
        }.buffer().flowOn(defaultDispatcher)

    @RequiresApi(Build.VERSION_CODES.O)
    override suspend fun getCurrentUtcTime(): Flow<String> = flow {
            while (true) {
                delay(TIME_DELAY)
                emit(OffsetDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT)))
            }
    }

    companion object {
        const val TIME_DELAY = 1_000L
        const val TIME_DISPLAY_FORMAT = "HH:mm:ss"
    }
}