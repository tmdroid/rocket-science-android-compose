package com.mindera.rocketscience.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "launches")
data class LaunchEntity(
    @PrimaryKey
    val flightNumber: Int,
    val missionName: String,
    val launchYear: String,
    val launchDateUnix: Long,
    val launchDateUtc: String,
    val launchDateLocal: String,
    val rocketId: String,
    val rocketName: String,
    val rocketType: String,
    val launchSiteName: String,
    val launchSiteNameLong: String,
    val launchSuccess: Boolean?,
    val upcoming: Boolean,
    val details: String?,
    val missionPatchUrl: String?,
    val missionPatchSmallUrl: String?,
    val articleUrl: String?,
    val videoUrl: String?,
    val wikipediaUrl: String?,
    val customers: String?, // JSON string of customer list
    val payloadType: String?,
    val orbit: String?,
    val payloadMassKg: Double?,
    val lastUpdated: Long = System.currentTimeMillis()
)