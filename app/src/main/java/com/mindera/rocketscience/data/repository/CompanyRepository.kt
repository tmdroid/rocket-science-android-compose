package com.mindera.rocketscience.data.repository

import com.mindera.rocketscience.data.local.datasource.LocalDataSource
import com.mindera.rocketscience.data.mapper.toDomainModel
import com.mindera.rocketscience.data.mapper.toEntity
import com.mindera.rocketscience.data.remote.datasource.CompanyRemoteDataSource
import com.mindera.rocketscience.domain.model.Company
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

interface CompanyRepository {
    fun getCompanyInfo(): Flow<Result<Company>>
}

@Singleton
class CompanyRepositoryImpl @Inject constructor(
    private val remoteDataSource: CompanyRemoteDataSource,
    private val localDataSource: LocalDataSource
) : CompanyRepository {


    override fun getCompanyInfo(): Flow<Result<Company>> = flow {
        // Emit cached data immediately if available
        val cachedCompany = localDataSource.getCompanyInfoSync()
        if (cachedCompany != null) {
            emit(Result.success(cachedCompany.toDomainModel()))
        }

        // Try to refresh data from remote if cache is stale or doesn't exist
        val shouldRefresh = cachedCompany == null || localDataSource.isCompanyDataStale()

        if (shouldRefresh) {
            try {
                val remoteResult = remoteDataSource.getCompanyInfo()
                if (remoteResult.isSuccess) {
                    val companyDto = remoteResult.getOrThrow()
                    val companyEntity = companyDto.toEntity()

                    // Cache the new data
                    localDataSource.insertCompany(companyEntity)

                    // Emit the fresh data
                    emit(Result.success(companyDto.toDomainModel()))
                } else {
                    // If we DON'T have cached data, emit the remote error
                    if (cachedCompany == null) {
                        // Only emit error if we have no cached data at all
                        emit(remoteResult.map { it.toDomainModel() })
                    }
                    // If we have cached data, we already emitted it above, so do nothing here
                }
            } catch (exception: Exception) {
                // Network error - keep using cached data if available
                if (cachedCompany == null) {
                    // Only emit error if we have no cached data at all
                    emit(Result.failure(exception))
                }
                // If we have cached data, we already emitted it above, so do nothing
            }
        }
    }
}