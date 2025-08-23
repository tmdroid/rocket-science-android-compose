package com.mindera.rocketscience.data.mapper

import com.mindera.rocketscience.data.local.entity.LaunchEntity
import com.mindera.rocketscience.data.remote.dto.LaunchDto
import com.mindera.rocketscience.domain.model.Launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun LaunchDto.toEntity(): LaunchEntity {
    val payload = rocket.secondStage?.payloads?.firstOrNull()
    val customers = payload?.customers?.let { 
        Json.encodeToString(it) 
    }
    
    return LaunchEntity(
        flightNumber = flightNumber,
        missionName = missionName,
        launchYear = launchYear,
        launchDateUnix = launchDateUnix,
        launchDateUtc = launchDateUtc,
        launchDateLocal = launchDateLocal,
        rocketId = rocket.rocketId,
        rocketName = rocket.rocketName,
        rocketType = rocket.rocketType,
        launchSiteName = launchSite.siteName,
        launchSiteNameLong = launchSite.siteNameLong,
        launchSuccess = launchSuccess,
        upcoming = upcoming,
        details = details,
        missionPatchUrl = links.missionPatch,
        missionPatchSmallUrl = links.missionPatchSmall,
        articleUrl = links.articleLink,
        videoUrl = links.videoLink,
        wikipediaUrl = links.wikipedia,
        customers = customers,
        payloadType = payload?.payloadType,
        orbit = payload?.orbit,
        payloadMassKg = payload?.payloadMassKg
    )
}

fun LaunchEntity.toDomainModel(): Launch {
    val (formattedDate, formattedTime) = formatDateTime(launchDateUtc)
    return Launch(
        id = flightNumber.toString(),
        missionName = missionName,
        launchDate = formattedDate,
        launchTime = formattedTime,
        launchDateUnix = launchDateUnix,
        rocketName = rocketName,
        rocketType = rocketType,
        missionPatchUrl = missionPatchSmallUrl ?: missionPatchUrl,
        success = launchSuccess,
        wikipediaUrl = wikipediaUrl,
        videoUrl = videoUrl
    )
}

private fun formatDateTime(dateUtc: String): Pair<String, String> {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputFormat.parse(dateUtc)
        val formattedDate = dateFormat.format(date ?: Date())
        val formattedTime = timeFormat.format(date ?: Date())
        Pair(formattedDate, formattedTime)
    } catch (e: Exception) {
        try {
            // Try alternative format without milliseconds
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val date = inputFormat.parse(dateUtc)
            val formattedDate = dateFormat.format(date ?: Date())
            val formattedTime = timeFormat.format(date ?: Date())
            Pair(formattedDate, formattedTime)
        } catch (e: Exception) {
            Pair(dateUtc, "") // Return original if parsing fails
        }
    }
}