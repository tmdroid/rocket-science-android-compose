package com.mindera.rocketscience.di

import android.content.Context
import androidx.room.Room
import com.mindera.rocketscience.data.local.dao.CompanyDao
import com.mindera.rocketscience.data.local.dao.LaunchDao
import com.mindera.rocketscience.data.local.database.RocketScienceDatabase
import com.mindera.rocketscience.util.BuildUtils
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): RocketScienceDatabase =
        Room.databaseBuilder(
            context,
            RocketScienceDatabase::class.java,
            RocketScienceDatabase.DATABASE_NAME
        ).apply {
            // For development - recreate DB on schema change
            if (BuildUtils.isDebugBuild(context)) {
                fallbackToDestructiveMigration(true)
            }
        }.build()

    @Provides
    fun provideLaunchDao(database: RocketScienceDatabase): LaunchDao = database.launchDao()
    
    @Provides
    fun provideCompanyDao(database: RocketScienceDatabase): CompanyDao = database.companyDao()
}