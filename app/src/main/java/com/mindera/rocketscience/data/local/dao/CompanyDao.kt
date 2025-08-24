package com.mindera.rocketscience.data.local.dao

import androidx.room.*
import com.mindera.rocketscience.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    
    @Query("SELECT * FROM company WHERE id = 1")
    fun getCompanyInfo(): Flow<CompanyEntity?>
    
    @Query("SELECT * FROM company WHERE id = 1")
    suspend fun getCompanyInfoSync(): CompanyEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompany(company: CompanyEntity)
    
    @Query("SELECT lastUpdated FROM company WHERE id = 1")
    suspend fun getLastUpdatedTimestamp(): Long?
    
    @Query("DELETE FROM company")
    suspend fun deleteCompany()
}