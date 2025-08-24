package com.mindera.rocketscience.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mindera.rocketscience.data.local.dao.LaunchDao
import com.mindera.rocketscience.data.local.entity.LaunchEntity

@Database(
    entities = [LaunchEntity::class],
    version = 2,
    exportSchema = false
)
abstract class RocketScienceDatabase : RoomDatabase() {

    abstract fun launchDao(): LaunchDao

    companion object {
        const val DATABASE_NAME = "rocket_science_database"
    }
}