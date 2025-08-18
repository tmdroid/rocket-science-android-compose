package com.mindera.rocketscience.data.local.datasource

import com.mindera.rocketscience.data.local.dao.LaunchDao
import com.mindera.rocketscience.data.local.entity.LaunchEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val launchDao: LaunchDao
) {
    fun getAllLaunches(): Flow<List<LaunchEntity>> = launchDao.getAllLaunches()
    
    suspend fun insertLaunches(launches: List<LaunchEntity>) = launchDao.insertLaunches(launches)
    
    suspend fun deleteAllLaunches() = launchDao.deleteAllLaunches()
    
    suspend fun getLaunchCount(): Int = launchDao.getLaunchCount()
    
    suspend fun getStaleEntries(timestamp: Long): List<LaunchEntity> = launchDao.getStaleEntries(timestamp)
    
    suspend fun isDataStale(): Boolean {
        val oneDayAgo = System.currentTimeMillis() - ONE_DAY_IN_MILLIS
        val staleEntries = getStaleEntries(oneDayAgo)
        return staleEntries.isNotEmpty() || getLaunchCount() == 0
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
    }
}