package com.mccarty.currentdeviceinfo.domain.usecase

import app.cash.turbine.test
import com.mccarty.currentdeviceinfo.domain.usecase.GetDateTimeUseCase.Companion.TIME_DISPLAY_FORMAT
import com.mccarty.currentdeviceinfo.ui.theme.CoroutineRule
import com.mccarty.currentdeviceinfo.ui.theme.MainViewModelTest.Companion.FAKE_TIME
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class GetDataTimeUseCaseTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    @get:Rule
    val coroutineRule = CoroutineRule()

    @OptIn(ExperimentalCoroutinesApi::class)
    val getDateTimeUseCase = GetDateTimeUseCase(coroutineRule.testDispatcher)

    private val localTime = LocalTime.parse(FAKE_TIME, DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT))

    @Test
    fun `assert local time format is correct`() = runTest {
        getDateTimeUseCase.getCurrentLocalTime().test {
            assertThat(localTime, CoreMatchers.instanceOf(LocalTime::class.java))
        }
    }

    @Test
    fun `assert utc time format is correct`() = runTest {
        getDateTimeUseCase.getCurrentUtcTime().test {
            assertThat(localTime, CoreMatchers.instanceOf(LocalTime::class.java))
        }
    }

    @Test
    fun `assert getCurrentLocalTime format is correct`() = runTest {
        getDateTimeUseCase.getCurrentLocalTime().test {
            val isFormattedTime: Boolean = try {
                awaitItem().format(DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT))
                true
            } catch (e: Exception) {
                false
            }
            assertTrue(isFormattedTime)
        }
    }

    @Test
    fun `assert getCurrentUtcTime format is correct`() = runTest {
        val utc = getDateTimeUseCase.getCurrentUtcTime().first()
        val isFormattedTime: Boolean = try {
            utc.format(DateTimeFormatter.ofPattern(TIME_DISPLAY_FORMAT))
            true
        } catch(e: Exception) {
            false
        }
        assertTrue(isFormattedTime)
    }
}