package com.mindera.rocketscience.domain.model

/**
 * Domain entity representing a SpaceX launch
 * This is the core business object, independent of UI or database concerns
 */
data class Launch(
    val id: String,
    val missionName: String,
    val launchDate: String,
    val launchTime: String,
    val launchDateUnix: Long,
    val rocketName: String,
    val rocketType: String,
    val missionPatchUrl: String?,
    val success: Boolean?
)