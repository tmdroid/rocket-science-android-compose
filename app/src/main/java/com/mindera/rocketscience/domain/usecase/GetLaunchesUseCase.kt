package com.mindera.rocketscience.domain.usecase

import com.mindera.rocketscience.data.repository.LaunchesRepository
import com.mindera.rocketscience.domain.model.Launch
import com.mindera.rocketscience.domain.model.LaunchStatus
import com.mindera.rocketscience.features.launches.LaunchUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class GetLaunchesUseCase @Inject constructor(
    private val launchesRepository: LaunchesRepository
) {

    operator fun invoke(): Flow<Result<List<LaunchUiModel>>> =
        launchesRepository.getLaunches().map { result ->
            result.map { launches ->
                launches.map { launch -> launch.toUiModel() }
            }
        }

    private fun Launch.toUiModel(): LaunchUiModel {
        return LaunchUiModel(
            id = id,
            name = missionName,
            dateTime = "$launchDate at $launchTime",
            rocketInfo = "$rocketName / $rocketType",
            launchStatus = calculateLaunchStatus(launchDateUnix),
            missionPatchUrl = missionPatchUrl,
            success = success
        )
    }

    private fun calculateLaunchStatus(launchDateUnix: Long): LaunchStatus {
        val currentTime = System.currentTimeMillis() / 1000 // Convert to seconds
        val daysDiff = (currentTime - launchDateUnix) / (24 * 60 * 60) // Convert to days

        return when {
            daysDiff > 0 -> LaunchStatus.DaysSinceLaunch(daysDiff.toInt())
            daysDiff < 0 -> LaunchStatus.DaysUntilLaunch(abs(daysDiff).toInt())
            else -> LaunchStatus.LaunchingToday
        }
    }
}