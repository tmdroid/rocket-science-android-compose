package com.mindera.rocketscience.di

import com.mindera.rocketscience.data.repository.LaunchesRepository
import com.mindera.rocketscience.data.repository.LaunchesRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    @Binds
    @Singleton
    abstract fun bindLaunchesRepository(
        launchesRepositoryImpl: LaunchesRepositoryImpl
    ): LaunchesRepository
}