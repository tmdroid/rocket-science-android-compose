package com.mindera.rocketscience.domain.usecase

import com.mindera.rocketscience.data.repository.LaunchesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetAvailableYearsUseCase @Inject constructor(
    private val launchesRepository: LaunchesRepository
) {

    operator fun invoke(): Flow<Result<List<String>>> =
        launchesRepository.getLaunches().map { result ->
            result.map { launches ->
                launches
                    .map { launch -> extractYear(launch.launchDate) }
                    .distinct()
                    .sorted()
            }
        }

    private fun extractYear(dateString: String): String {
        // Extract year from "MMM dd, yyyy" format
        return dateString.takeLast(4)
    }
}