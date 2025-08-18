package com.mindera.rocketscience.data.repository

import com.mindera.rocketscience.data.mapper.toDomainModel
import com.mindera.rocketscience.data.remote.datasource.CompanyRemoteDataSource
import com.mindera.rocketscience.domain.model.Company
import javax.inject.Inject
import javax.inject.Singleton

interface CompanyRepository {
    suspend fun getCompanyInfo(): Result<Company>
}

@Singleton
class CompanyRepositoryImpl @Inject constructor(
    private val remoteDataSource: CompanyRemoteDataSource
) : CompanyRepository {

    override suspend fun getCompanyInfo(): Result<Company> =
        remoteDataSource.getCompanyInfo()
            .map { dto -> dto.toDomainModel() }
}