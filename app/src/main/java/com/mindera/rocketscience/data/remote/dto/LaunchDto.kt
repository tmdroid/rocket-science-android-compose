package com.mindera.rocketscience.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LaunchDto(
    @SerialName("flight_number")
    val flightNumber: Int,
    @SerialName("mission_name")
    val missionName: String,
    @SerialName("launch_year")
    val launchYear: String,
    @SerialName("launch_date_unix")
    val launchDateUnix: Long,
    @SerialName("launch_date_utc")
    val launchDateUtc: String,
    @SerialName("launch_date_local")
    val launchDateLocal: String,
    @SerialName("rocket")
    val rocket: RocketDto,
    @SerialName("launch_site")
    val launchSite: LaunchSiteDto,
    @SerialName("launch_success")
    val launchSuccess: Boolean? = null,
    @SerialName("links")
    val links: LinksDto,
    @SerialName("details")
    val details: String? = null,
    @SerialName("upcoming")
    val upcoming: Boolean = false
)

@Serializable
data class RocketDto(
    @SerialName("rocket_id")
    val rocketId: String,
    @SerialName("rocket_name")
    val rocketName: String,
    @SerialName("rocket_type")
    val rocketType: String,
    @SerialName("second_stage")
    val secondStage: SecondStageDto? = null
)

@Serializable
data class SecondStageDto(
    @SerialName("payloads")
    val payloads: List<PayloadDto> = emptyList()
)

@Serializable
data class PayloadDto(
    @SerialName("payload_id")
    val payloadId: String,
    @SerialName("customers")
    val customers: List<String> = emptyList(),
    @SerialName("payload_type")
    val payloadType: String? = null,
    @SerialName("payload_mass_kg")
    val payloadMassKg: Double? = null,
    @SerialName("orbit")
    val orbit: String? = null
)

@Serializable
data class LaunchSiteDto(
    @SerialName("site_id")
    val siteId: String,
    @SerialName("site_name")
    val siteName: String,
    @SerialName("site_name_long")
    val siteNameLong: String
)

@Serializable
data class LinksDto(
    @SerialName("mission_patch")
    val missionPatch: String? = null,
    @SerialName("mission_patch_small")
    val missionPatchSmall: String? = null,
    @SerialName("article_link")
    val articleLink: String? = null,
    @SerialName("wikipedia")
    val wikipedia: String? = null,
    @SerialName("video_link")
    val videoLink: String? = null
)