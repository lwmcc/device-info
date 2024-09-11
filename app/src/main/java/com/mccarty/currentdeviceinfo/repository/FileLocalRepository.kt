package com.mccarty.currentdeviceinfo.repository

import android.content.Context
import com.mccarty.currentdeviceinfo.MainActivity.Companion.DEVICE_OUTPUT
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Named

class FileLocalRepository @Inject constructor(
    private val context: Context,
    @Named("io")
    private val ioDispatcher: CoroutineDispatcher,
) : LocalRepository {

    override suspend fun writeDeviceDetailsCsv(vararg deviceData: String) {
        withContext(ioDispatcher) {
            if (File(DEVICE_OUTPUT).exists()) {
                context.openFileOutput(DEVICE_OUTPUT, Context.MODE_APPEND).use {
                    it.write(deviceData[1].toByteArray())
                }
            } else {
                context.openFileOutput(DEVICE_OUTPUT, Context.MODE_PRIVATE).use {
                    it.write(deviceData[0].toByteArray())
                    it.write(deviceData[1].toByteArray())
                }
            }
        }
    }
}