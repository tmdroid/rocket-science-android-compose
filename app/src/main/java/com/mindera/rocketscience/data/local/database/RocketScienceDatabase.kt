package com.mindera.rocketscience.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mindera.rocketscience.data.local.dao.CompanyDao
import com.mindera.rocketscience.data.local.dao.LaunchDao
import com.mindera.rocketscience.data.local.entity.CompanyEntity
import com.mindera.rocketscience.data.local.entity.LaunchEntity

@Database(
    entities = [LaunchEntity::class, CompanyEntity::class],
    version = 3,
    exportSchema = false
)
abstract class RocketScienceDatabase : RoomDatabase() {

    abstract fun launchDao(): LaunchDao
    
    abstract fun companyDao(): CompanyDao

    companion object {
        const val DATABASE_NAME = "rocket_science_database"
    }
}