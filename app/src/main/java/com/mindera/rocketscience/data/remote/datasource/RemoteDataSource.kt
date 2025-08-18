package com.mindera.rocketscience.data.remote.datasource

import com.mindera.rocketscience.data.remote.api.SpaceXApiService
import com.mindera.rocketscience.data.remote.dto.LaunchDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteDataSource @Inject constructor(
    private val apiService: SpaceXApiService
) {
    suspend fun getAllLaunches(): Result<List<LaunchDto>> {
        return try {
            val launches = apiService.getAllLaunches()
            Result.success(launches)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }
}