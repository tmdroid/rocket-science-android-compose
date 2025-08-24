package com.mindera.rocketscience.data.local.datasource

import com.mindera.rocketscience.data.local.dao.CompanyDao
import com.mindera.rocketscience.data.local.dao.LaunchDao
import com.mindera.rocketscience.data.local.entity.CompanyEntity
import com.mindera.rocketscience.data.local.entity.LaunchEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalDataSource @Inject constructor(
    private val launchDao: LaunchDao,
    private val companyDao: CompanyDao
) {
    fun getAllLaunches(): Flow<List<LaunchEntity>> = launchDao.getAllLaunches()
    
    suspend fun insertLaunches(launches: List<LaunchEntity>) = launchDao.insertLaunches(launches)
    
    suspend fun deleteAllLaunches() = launchDao.deleteAllLaunches()
    
    private suspend fun getLaunchCount(): Int = launchDao.getLaunchCount()
    
    private suspend fun getStaleEntries(timestamp: Long): List<LaunchEntity> = launchDao.getStaleEntries(timestamp)
    
    suspend fun isDataStale(): Boolean {
        val oneDayAgo = System.currentTimeMillis() - ONE_DAY_IN_MILLIS
        val staleEntries = getStaleEntries(oneDayAgo)
        return staleEntries.isNotEmpty() || getLaunchCount() == 0
    }

    // Company operations
    suspend fun getCompanyInfoSync(): CompanyEntity? = companyDao.getCompanyInfoSync()
    
    suspend fun insertCompany(company: CompanyEntity) = companyDao.insertCompany(company)
    
    suspend fun isCompanyDataStale(): Boolean {
        val lastUpdated = companyDao.getLastUpdatedTimestamp() ?: return true
        val oneWeekAgo = System.currentTimeMillis() - ONE_WEEK_IN_MILLIS
        return lastUpdated < oneWeekAgo
    }

    companion object {
        private const val ONE_DAY_IN_MILLIS = 24 * 60 * 60 * 1000L
        private const val ONE_WEEK_IN_MILLIS = 7 * 24 * 60 * 60 * 1000L
    }
}