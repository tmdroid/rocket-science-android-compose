package com.mindera.rocketscience.features.launches

import com.mindera.rocketscience.domain.model.LaunchStatus

data class LaunchUiModel(
    val id: String,
    val name: String,
    val dateTime: String,
    val rocketInfo: String,
    val launchStatus: LaunchStatus,
    val missionPatchUrl: String?,
    val success: Boolean?,
    val launchYear: String,
    val launchDateUnix: Long,
    val wikipediaUrl: String?,
    val videoUrl: String?
)