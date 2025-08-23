package com.mindera.rocketscience.domain.usecase

import com.mindera.rocketscience.data.repository.LaunchesRepository
import com.mindera.rocketscience.domain.model.Launch
import com.mindera.rocketscience.domain.model.LaunchStatus
import com.mindera.rocketscience.features.launches.LaunchFilterState
import com.mindera.rocketscience.features.launches.LaunchSuccessFilter
import com.mindera.rocketscience.features.launches.LaunchUiModel
import com.mindera.rocketscience.features.launches.SortOrder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class GetLaunchesUseCase @Inject constructor(
    private val launchesRepository: LaunchesRepository
) {

    operator fun invoke(filterState: LaunchFilterState = LaunchFilterState()): Flow<Result<List<LaunchUiModel>>> =
        launchesRepository.getLaunches().map { result ->
            result.map { launches ->
                launches
                    .map { launch -> launch.toUiModel() }
                    .filter { launch -> matchesYearFilter(launch, filterState.selectedYear) }
                    .filter { launch -> matchesSuccessFilter(launch, filterState.launchSuccess) }
                    .sortedWith { launch1, launch2 ->
                        when (filterState.sortOrder) {
                            SortOrder.ASC -> launch1.launchDateUnix.compareTo(launch2.launchDateUnix)
                            SortOrder.DESC -> launch2.launchDateUnix.compareTo(launch1.launchDateUnix)
                        }
                    }
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
            success = success,
            launchYear = extractYear(launchDate),
            launchDateUnix = launchDateUnix,
            wikipediaUrl = wikipediaUrl,
            videoUrl = videoUrl
        )
    }
    
    private fun extractYear(dateString: String): String {
        // Extract year from "MMM dd, yyyy" format
        return dateString.takeLast(4)
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
    
    private fun matchesYearFilter(launch: LaunchUiModel, selectedYear: String?): Boolean {
        return selectedYear == null || launch.launchYear == selectedYear
    }
    
    private fun matchesSuccessFilter(launch: LaunchUiModel, successFilter: LaunchSuccessFilter): Boolean {
        return when (successFilter) {
            LaunchSuccessFilter.ALL -> true
            LaunchSuccessFilter.SUCCESS_ONLY -> launch.success == true
            LaunchSuccessFilter.FAILED_ONLY -> launch.success == false
        }
    }
}