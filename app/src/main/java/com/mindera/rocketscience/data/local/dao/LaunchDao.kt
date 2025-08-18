package com.mindera.rocketscience.data.local.dao

import androidx.room.*
import com.mindera.rocketscience.data.local.entity.LaunchEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LaunchDao {
    
    @Query("SELECT * FROM launches ORDER BY launchDateUnix DESC")
    fun getAllLaunches(): Flow<List<LaunchEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLaunches(launches: List<LaunchEntity>)
    
    @Query("DELETE FROM launches")
    suspend fun deleteAllLaunches()
    
    @Query("SELECT COUNT(*) FROM launches")
    suspend fun getLaunchCount(): Int
    
    @Query("SELECT * FROM launches WHERE lastUpdated < :timestamp")
    suspend fun getStaleEntries(timestamp: Long): List<LaunchEntity>
}