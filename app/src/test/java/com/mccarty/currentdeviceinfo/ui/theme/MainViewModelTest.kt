package com.mccarty.currentdeviceinfo.ui.theme

import com.mccarty.currentdeviceinfo.domain.usecase.GetDataTime
import com.mccarty.currentdeviceinfo.domain.usecase.GetIpAddress
import com.mccarty.currentdeviceinfo.domain.usecase.HandleCsvFile
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class MainViewModelTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineRule = CoroutineRule()

    private lateinit var mainViewModel: MainViewModel

    val fakeTime = "09:11:03"

    @OptIn(ExperimentalCoroutinesApi::class)
    @Before
    fun setup() {
        mainViewModel = MainViewModel(
            GetDataTimeFake(),
            HandleCsvFileFake(),
            GetIpAddressFake(),
            coroutineRule.testDispatcher,
        )
    }

    @Test
    fun `assert hasWifiConnection true and hasCellularConnection false if wifi true`() {

        mainViewModel.isWifiNetwork(true)
        mainViewModel.setIpAddress()
        assertTrue(mainViewModel.hasWifiConnection)
        assertFalse(mainViewModel.hasCellularConnection)
    }

    @Test
    fun `assert hasWifiConnection false and hasCellularConnection true if wifi false`() {
        mainViewModel.isWifiNetwork(false)
        mainViewModel.setIpAddress()
        assertFalse(mainViewModel.hasWifiConnection)
        assertTrue(mainViewModel.hasCellularConnection)
    }

    @Test
    fun `assert hasInternet true`() {
        mainViewModel.hasInternetConnection(true)
        assertTrue(mainViewModel.hasInternet)
    }

    @Test
    fun `assert local times are equal`() = runTest {
        mainViewModel.startCurrentLocalTimeJob()
        assertEquals(fakeTime,  mainViewModel.currentLocalTime.value)
    }

    @Test
    fun `assert utc times are equal`() = runTest {
        mainViewModel.startCurrentUtcTimeJob()
        assertEquals(fakeTime,  mainViewModel.currentUtcTime.value)
    }

    @Test
    fun `assert localTimeJob is null`() = runTest {
        mainViewModel.startCurrentLocalTimeJob()
        mainViewModel.cancelCurrentLocalTimeJob()
        assertNull(mainViewModel.localTimeJob)
    }

    @Test
    fun`assert utcTimeJob is null`() {
        mainViewModel.startCurrentUtcTimeJob()
        mainViewModel.cancelCurrentUtcTimeJob()
        assertNull( mainViewModel.utcTimeJob)
    }

    inner class GetDataTimeFake: GetDataTime {
        override suspend fun getCurrentLocalTime(): Flow<String> = flow {
            emit(fakeTime)
        }

        override suspend fun getCurrentUtcTime(): Flow<String> = flow {
            emit(fakeTime)
        }
    }

    class HandleCsvFileFake: HandleCsvFile {
        override suspend fun appendCsvFile(vararg deviceData: String) {
            // TODO: implement
        }
    }

    class GetIpAddressFake: GetIpAddress {
        override fun getIpAddress(): String {
            // TODO: implement
            return ""
        }
    }
}