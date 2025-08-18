package com.mindera.rocketscience.data.remote.datasource

import com.mindera.rocketscience.data.remote.api.SpaceXApiService
import com.mindera.rocketscience.data.remote.dto.CompanyDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRemoteDataSource @Inject constructor(
    private val apiService: SpaceXApiService
) {
    suspend fun getCompanyInfo(): Result<CompanyDto> {
        return try {
            val company = apiService.getCompanyInfo()
            Result.success(company)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}