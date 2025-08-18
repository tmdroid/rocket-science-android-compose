package com.mindera.rocketscience.data.repository

import com.mindera.rocketscience.data.local.datasource.LocalDataSource
import com.mindera.rocketscience.data.remote.datasource.RemoteDataSource
import com.mindera.rocketscience.data.mapper.toDomainModel
import com.mindera.rocketscience.data.mapper.toEntity
import com.mindera.rocketscience.domain.model.Launch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

interface LaunchesRepository {
    fun getLaunches(): Flow<Result<List<Launch>>>
    suspend fun refreshLaunches(): Result<Unit>
}

@Singleton
class LaunchesRepositoryImpl @Inject constructor(
    private val remoteDataSource: RemoteDataSource,
    private val localDataSource: LocalDataSource
) : LaunchesRepository {

    override fun getLaunches(): Flow<Result<List<Launch>>> = flow {
        try {
            // First emit cached data
            localDataSource.getAllLaunches().collect { cachedLaunches ->
                if (cachedLaunches.isNotEmpty()) {
                    emit(Result.success(cachedLaunches.map { it.toDomainModel() }))
                }

                // Check if data is stale and refresh if needed
                if (localDataSource.isDataStale()) {
                    val refreshResult = refreshFromRemote()
                    if (refreshResult.isSuccess) {
                        // Emit fresh data after refresh
                        emitAll(
                            localDataSource.getAllLaunches().map { freshLaunches ->
                                Result.success(freshLaunches.map { it.toDomainModel() })
                            }
                        )
                    } else {
                        // If refresh failed but we have cached data, keep showing cached data
                        if (cachedLaunches.isEmpty()) {
                            emit(
                                Result.failure(
                                    refreshResult.exceptionOrNull() ?: Exception("Unknown error")
                                )
                            )
                        }
                    }
                }
            }
        } catch (exception: Exception) {
            emit(Result.failure(exception))
        }
    }

    override suspend fun refreshLaunches(): Result<Unit> = refreshFromRemote()

    private suspend fun refreshFromRemote(): Result<Unit> {
        val remoteResult = remoteDataSource.getAllLaunches()

        return if (remoteResult.isSuccess) {
            try {
                val launchEntities = remoteResult.getOrNull()
                    ?.map { launchDto -> launchDto.toEntity() }
                    ?: emptyList()

                localDataSource.deleteAllLaunches()
                localDataSource.insertLaunches(launchEntities)
                Result.success(Unit)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        } else {
            Result.failure(remoteResult.exceptionOrNull() ?: Exception("Failed to fetch launches"))
        }
    }
}