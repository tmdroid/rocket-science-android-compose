package com.mindera.rocketscience.di

import com.mindera.rocketscience.data.repository.LaunchesRepository
import com.mindera.rocketscience.domain.usecase.GetLaunchesUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {
    
    @Provides
    @Singleton
    fun provideGetLaunchesUseCase(launchesRepository: LaunchesRepository): GetLaunchesUseCase =
        GetLaunchesUseCase(launchesRepository)
}