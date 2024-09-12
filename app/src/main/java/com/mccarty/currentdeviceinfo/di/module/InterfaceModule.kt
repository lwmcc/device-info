package com.mccarty.currentdeviceinfo.di.module

import com.mccarty.currentdeviceinfo.domain.usecase.GetDateTime
import com.mccarty.currentdeviceinfo.domain.usecase.GetDateTimeUseCase
import com.mccarty.currentdeviceinfo.domain.usecase.GetIpAddress
import com.mccarty.currentdeviceinfo.domain.usecase.GetIpAddressUseCase
import com.mccarty.currentdeviceinfo.domain.usecase.HandleCsvFile
import com.mccarty.currentdeviceinfo.domain.usecase.HandleCsvFileUseCase
import com.mccarty.currentdeviceinfo.repository.FileLocalRepository
import com.mccarty.currentdeviceinfo.repository.LocalRepository import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class InterfaceModule {

    @Binds
    abstract fun provideGetDateTime(getDataTimeUseCase: GetDateTimeUseCase): GetDateTime

    @Binds
    abstract fun provideFileLocalRepository(fileLocalRepository: FileLocalRepository): LocalRepository

    @Binds
    abstract fun provideHandleCsvFileUseCase(handleCsvFileUseCase: HandleCsvFileUseCase): HandleCsvFile

    @Binds
    abstract fun provideGetIpAddressUseCase(getIpAddressUseCase: GetIpAddressUseCase): GetIpAddress
}