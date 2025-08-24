package com.mindera.rocketscience.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.local.entity.LaunchEntity
import com.mindera.rocketscience.data.remote.dto.LaunchDto
import com.mindera.rocketscience.data.remote.dto.LaunchSiteDto
import com.mindera.rocketscience.data.remote.dto.LinksDto
import com.mindera.rocketscience.data.remote.dto.PayloadDto
import com.mindera.rocketscience.data.remote.dto.RocketDto
import com.mindera.rocketscience.data.remote.dto.SecondStageDto
import org.junit.Before
import org.junit.Test
import java.util.Locale

class LaunchMapperTest {

    companion object {
        // Flight numbers
        private const val FLIGHT_1 = 1
        private const val FLIGHT_2 = 2
        private const val FLIGHT_3 = 3
        private const val FLIGHT_4 = 4
        private const val FLIGHT_5 = 5
        private const val FLIGHT_6 = 6
        private const val FLIGHT_7 = 7
        private const val FLIGHT_8 = 8
        private const val FLIGHT_9 = 9

        // Mission names
        private const val TEST_MISSION = "Test Mission"
        private const val MINIMAL_MISSION = "Minimal Mission"
        private const val NO_PAYLOAD_MISSION = "No Payload Mission"
        private const val DATETIME_TEST_MISSION = "DateTime Test"
        private const val ALT_FORMAT_TEST_MISSION = "Alternative Format Test"
        private const val MALFORMED_DATE_TEST_MISSION = "Malformed Date Test"
        private const val MULTI_CUSTOMER_MISSION = "Multi Customer Mission"
        private const val UNKNOWN_SUCCESS_MISSION = "Unknown Success Mission"
        private const val MORNING_LAUNCH_MISSION = "Morning Launch"
        private const val MULTI_PAYLOAD_MISSION = "Multi Payload Mission"
        private const val MISSION_WITH_REGULAR_PATCH = "Mission with regular patch"

        // Years
        private const val YEAR_2020 = "2020"
        private const val YEAR_2021 = "2021"
        private const val YEAR_2022 = "2022"
        private const val YEAR_2023 = "2023"
        private const val YEAR_2024 = "2024"
        private const val YEAR_2025 = "2025"

        // Unix timestamps
        private const val UNIX_2020_JAN_15 = 1579082400L
        private const val UNIX_2021_MAR_20 = 1616249100L
        private const val UNIX_2022_JAN_01 = 1640995200L
        private const val UNIX_2023_JAN_01 = 1672531200L
        private const val UNIX_2024_JAN_01 = 1704067200L
        private const val UNIX_2025_JAN_01 = 1735689600L

        // UTC date strings
        private const val UTC_2020_JAN_15 = "2020-01-15T10:30:00.000Z"
        private const val UTC_2021_MAR_20 = "2021-03-20T14:45:00.000Z"
        private const val UTC_2022_JAN_01 = "2022-01-01T00:00:00.000Z"
        private const val UTC_2023_JAN_01_NO_MS = "2023-01-01T00:00:00Z"
        private const val UTC_2025_JAN_01 = "2025-01-01T00:00:00.000Z"
        private const val UTC_MORNING_LAUNCH = "2020-01-15T06:15:30.123Z"
        private const val INVALID_DATE_FORMAT = "invalid-date-format"

        // Local date strings
        private const val LOCAL_2020_JAN_15 = "2020-01-15T10:30:00-05:00"
        private const val LOCAL_2021_MAR_20 = "2021-03-20T14:45:00-05:00"
        private const val LOCAL_2021_DEC_31 = "2021-12-31T19:00:00-05:00"
        private const val LOCAL_2022_DEC_31 = "2022-12-31T19:00:00-05:00"
        private const val LOCAL_2024_JAN_01 = "2024-01-01T00:00:00-05:00"
        private const val LOCAL_2024_DEC_31 = "2024-12-31T19:00:00-05:00"
        private const val LOCAL_MORNING_LAUNCH = "2020-01-15T01:15:30-05:00"

        // Rocket IDs and names
        private const val FALCON9_ID = "falcon9"
        private const val FALCON9_NAME = "Falcon 9"
        private const val FALCON_HEAVY_ID = "falconheavy"
        private const val FALCON_HEAVY_NAME = "Falcon Heavy"
        private const val STARSHIP_ID = "starship"
        private const val STARSHIP_NAME = "Starship"

        // Rocket types
        private const val ROCKET_TYPE_FT = "FT"
        private const val ROCKET_TYPE_FH = "FH"
        private const val ROCKET_TYPE_SN = "SN"
        private const val ROCKET_TYPE_BLOCK5 = "Block 5"

        // Launch sites
        private const val KSC_SITE_ID = "ksc_lc_39a"
        private const val KSC_SITE_NAME = "KSC LC 39A"
        private const val KSC_SITE_LONG = "Kennedy Space Center Historic Launch Complex 39A"
        private const val VAFB_SITE_ID = "vafb_slc_4e"
        private const val VAFB_SITE_NAME = "VAFB SLC 4E"
        private const val VAFB_SITE_LONG = "Vandenberg Air Force Base Space Launch Complex 4E"
        private const val STARBASE_SITE_ID = "stls"
        private const val STARBASE_SITE_NAME = "Starbase"
        private const val STARBASE_SITE_LONG = "SpaceX Starbase"

        // URLs
        private const val PATCH_URL = "https://example.com/patch.png"
        private const val PATCH_SMALL_URL = "https://example.com/patch_small.png"
        private const val ARTICLE_URL = "https://example.com/article"
        private const val VIDEO_URL = "https://youtube.com/watch"
        private const val WIKIPEDIA_URL = "https://wikipedia.com/mission"

        // Payload data
        private const val PAYLOAD_ID_1 = "payload1"
        private const val PAYLOAD_TYPE_SATELLITE = "Satellite"
        private const val PAYLOAD_MASS_5000 = 5000.0
        private const val ORBIT_LEO = "LEO"
        private const val ORBIT_GTO = "GTO"
        private const val CUSTOMERS_NASA_SPACEX = "[\"NASA\",\"SpaceX\"]"

        // Mission details
        private const val TEST_MISSION_DETAILS = "Test mission details"
        private const val FUTURE_TEST_MISSION = "Future test mission"

        // Customers arrays
        private val NASA_SPACEX_CUSTOMERS = listOf("NASA", "SpaceX")
        private val MULTI_CUSTOMERS = listOf("NASA", "ESA", "JAXA", "Commercial Operator")
        private val PRIMARY_CUSTOMER = listOf("Primary Customer")
        private val SECONDARY_CUSTOMER = listOf("Secondary Customer")

        // Complex payload data
        private const val MULTI_SATELLITE_TYPE = "Multi-Satellite Deployment"
        private const val PAYLOAD_MASS_15000_5 = 15000.5
        private const val CUSTOMERS_MULTI = "[\"NASA\",\"ESA\",\"JAXA\",\"Commercial Operator\"]"

        // Primary/Secondary payloads
        private const val PRIMARY_PAYLOAD_ID = "primary"
        private const val SECONDARY_PAYLOAD_ID = "secondary"
        private const val PRIMARY_SATELLITE_TYPE = "Primary Satellite"
        private const val SECONDARY_SATELLITE_TYPE = "Secondary Satellite"
        private const val PAYLOAD_MASS_3000 = 3000.0
        private const val PAYLOAD_MASS_1000 = 1000.0
        private const val CUSTOMERS_PRIMARY = "[\"Primary Customer\"]"

        // Expected formatted dates
        private const val FORMATTED_JAN_15_2020 = "Jan 15, 2020"
        private const val FORMATTED_JAN_01_2022 = "Jan 01, 2022"
        private const val FORMATTED_JAN_01_2023 = "Jan 01, 2023"
        private const val FORMATTED_TIME_10_30 = "10:30"
        private const val FORMATTED_TIME_00_00 = "00:00"
        private const val FORMATTED_TIME_06_15 = "06:15"
        private const val EMPTY_TIME = ""

        // Expected IDs
        private const val EXPECTED_ID_1 = "1"
        private const val EXPECTED_ID_7 = "7"
    }

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
    }

    @Test
    fun `LaunchDto toEntity maps all fields correctly`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = FLIGHT_1,
            missionName = TEST_MISSION,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_2020_JAN_15,
            launchDateUtc = UTC_2020_JAN_15,
            launchDateLocal = LOCAL_2020_JAN_15,
            rocket = RocketDto(
                rocketId = FALCON9_ID,
                rocketName = FALCON9_NAME,
                rocketType = ROCKET_TYPE_FT,
                secondStage = SecondStageDto(
                    payloads = listOf(
                        PayloadDto(
                            payloadId = PAYLOAD_ID_1,
                            customers = NASA_SPACEX_CUSTOMERS,
                            payloadType = PAYLOAD_TYPE_SATELLITE,
                            payloadMassKg = PAYLOAD_MASS_5000,
                            orbit = ORBIT_LEO
                        )
                    )
                )
            ),
            launchSite = LaunchSiteDto(
                siteId = KSC_SITE_ID,
                siteName = KSC_SITE_NAME,
                siteNameLong = KSC_SITE_LONG
            ),
            launchSuccess = true,
            links = LinksDto(
                missionPatch = PATCH_URL,
                missionPatchSmall = PATCH_SMALL_URL,
                articleLink = ARTICLE_URL,
                wikipedia = WIKIPEDIA_URL,
                videoLink = VIDEO_URL
            ),
            details = TEST_MISSION_DETAILS,
            upcoming = false
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.flightNumber).isEqualTo(FLIGHT_1)
        assertThat(entity.missionName).isEqualTo(TEST_MISSION)
        assertThat(entity.launchYear).isEqualTo(YEAR_2020)
        assertThat(entity.launchDateUnix).isEqualTo(UNIX_2020_JAN_15)
        assertThat(entity.launchDateUtc).isEqualTo(UTC_2020_JAN_15)
        assertThat(entity.launchDateLocal).isEqualTo(LOCAL_2020_JAN_15)
        assertThat(entity.rocketId).isEqualTo(FALCON9_ID)
        assertThat(entity.rocketName).isEqualTo(FALCON9_NAME)
        assertThat(entity.rocketType).isEqualTo(ROCKET_TYPE_FT)
        assertThat(entity.launchSiteName).isEqualTo(KSC_SITE_NAME)
        assertThat(entity.launchSiteNameLong).isEqualTo(KSC_SITE_LONG)
        assertThat(entity.launchSuccess).isTrue()
        assertThat(entity.upcoming).isFalse()
        assertThat(entity.details).isEqualTo(TEST_MISSION_DETAILS)
        assertThat(entity.missionPatchUrl).isEqualTo(PATCH_URL)
        assertThat(entity.missionPatchSmallUrl).isEqualTo(PATCH_SMALL_URL)
        assertThat(entity.articleUrl).isEqualTo(ARTICLE_URL)
        assertThat(entity.videoUrl).isEqualTo(VIDEO_URL)
        assertThat(entity.wikipediaUrl).isEqualTo(WIKIPEDIA_URL)
        assertThat(entity.customers).isEqualTo(CUSTOMERS_NASA_SPACEX)
        assertThat(entity.payloadType).isEqualTo(PAYLOAD_TYPE_SATELLITE)
        assertThat(entity.orbit).isEqualTo(ORBIT_LEO)
        assertThat(entity.payloadMassKg).isEqualTo(PAYLOAD_MASS_5000)
    }

    @Test
    fun `LaunchDto toEntity handles null optional fields`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = FLIGHT_2,
            missionName = MINIMAL_MISSION,
            launchYear = YEAR_2021,
            launchDateUnix = UNIX_2021_MAR_20,
            launchDateUtc = UTC_2021_MAR_20,
            launchDateLocal = LOCAL_2021_MAR_20,
            rocket = RocketDto(
                rocketId = FALCON_HEAVY_ID,
                rocketName = FALCON_HEAVY_NAME,
                rocketType = ROCKET_TYPE_FH,
                secondStage = null
            ),
            launchSite = LaunchSiteDto(
                siteId = VAFB_SITE_ID,
                siteName = VAFB_SITE_NAME,
                siteNameLong = VAFB_SITE_LONG
            ),
            launchSuccess = null,
            links = LinksDto(),
            details = null,
            upcoming = true
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.flightNumber).isEqualTo(FLIGHT_2)
        assertThat(entity.missionName).isEqualTo(MINIMAL_MISSION)
        assertThat(entity.launchSuccess).isNull()
        assertThat(entity.upcoming).isTrue()
        assertThat(entity.details).isNull()
        assertThat(entity.missionPatchUrl).isNull()
        assertThat(entity.missionPatchSmallUrl).isNull()
        assertThat(entity.articleUrl).isNull()
        assertThat(entity.videoUrl).isNull()
        assertThat(entity.wikipediaUrl).isNull()
        assertThat(entity.customers).isNull()
        assertThat(entity.payloadType).isNull()
        assertThat(entity.orbit).isNull()
        assertThat(entity.payloadMassKg).isNull()
    }

    @Test
    fun `LaunchDto toEntity handles empty payloads list`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = FLIGHT_3,
            missionName = NO_PAYLOAD_MISSION,
            launchYear = YEAR_2022,
            launchDateUnix = UNIX_2022_JAN_01,
            launchDateUtc = UTC_2022_JAN_01,
            launchDateLocal = LOCAL_2021_DEC_31,
            rocket = RocketDto(
                rocketId = STARSHIP_ID,
                rocketName = STARSHIP_NAME,
                rocketType = ROCKET_TYPE_SN,
                secondStage = SecondStageDto(payloads = emptyList())
            ),
            launchSite = LaunchSiteDto(
                siteId = STARBASE_SITE_ID,
                siteName = STARBASE_SITE_NAME,
                siteNameLong = STARBASE_SITE_LONG
            ),
            launchSuccess = false,
            links = LinksDto(
                missionPatch = PATCH_URL
            )
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.customers).isNull()
        assertThat(entity.payloadType).isNull()
        assertThat(entity.orbit).isNull()
        assertThat(entity.payloadMassKg).isNull()
        assertThat(entity.missionPatchUrl).isEqualTo(PATCH_URL)
    }

    @Test
    fun `LaunchEntity toDomainModel maps all fields correctly`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = FLIGHT_1,
            missionName = TEST_MISSION,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_2020_JAN_15,
            launchDateUtc = UTC_2020_JAN_15,
            launchDateLocal = LOCAL_2020_JAN_15,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_FT,
            launchSiteName = KSC_SITE_NAME,
            launchSiteNameLong = KSC_SITE_LONG,
            launchSuccess = true,
            upcoming = false,
            details = TEST_MISSION_DETAILS,
            missionPatchUrl = PATCH_URL,
            missionPatchSmallUrl = PATCH_SMALL_URL,
            articleUrl = ARTICLE_URL,
            videoUrl = VIDEO_URL,
            wikipediaUrl = WIKIPEDIA_URL,
            customers = CUSTOMERS_NASA_SPACEX,
            payloadType = PAYLOAD_TYPE_SATELLITE,
            orbit = ORBIT_LEO,
            payloadMassKg = PAYLOAD_MASS_5000
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.id).isEqualTo(EXPECTED_ID_1)
        assertThat(domainModel.missionName).isEqualTo(TEST_MISSION)
        assertThat(domainModel.launchDate).isEqualTo(FORMATTED_JAN_15_2020)
        assertThat(domainModel.launchTime).isEqualTo(FORMATTED_TIME_10_30)
        assertThat(domainModel.launchDateUnix).isEqualTo(UNIX_2020_JAN_15)
        assertThat(domainModel.rocketName).isEqualTo(FALCON9_NAME)
        assertThat(domainModel.rocketType).isEqualTo(ROCKET_TYPE_FT)
        assertThat(domainModel.missionPatchUrl).isEqualTo(PATCH_SMALL_URL) // Prefers small
        assertThat(domainModel.success).isTrue()
        assertThat(domainModel.wikipediaUrl).isEqualTo(WIKIPEDIA_URL)
        assertThat(domainModel.videoUrl).isEqualTo(VIDEO_URL)
    }

    @Test
    fun `LaunchEntity toDomainModel prefers regular patch when small is null`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = FLIGHT_2,
            missionName = MISSION_WITH_REGULAR_PATCH,
            launchYear = YEAR_2021,
            launchDateUnix = UNIX_2021_MAR_20,
            launchDateUtc = UTC_2021_MAR_20,
            launchDateLocal = LOCAL_2021_MAR_20,
            rocketId = FALCON_HEAVY_ID,
            rocketName = FALCON_HEAVY_NAME,
            rocketType = ROCKET_TYPE_FH,
            launchSiteName = VAFB_SITE_NAME,
            launchSiteNameLong = VAFB_SITE_LONG,
            launchSuccess = false,
            upcoming = false,
            details = null,
            missionPatchUrl = PATCH_URL,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = null,
            wikipediaUrl = null,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.missionPatchUrl).isEqualTo(PATCH_URL)
        assertThat(domainModel.success).isFalse()
        assertThat(domainModel.wikipediaUrl).isNull()
        assertThat(domainModel.videoUrl).isNull()
    }

    @Test
    fun `formatDateTime handles standard UTC format correctly`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = FLIGHT_3,
            missionName = DATETIME_TEST_MISSION,
            launchYear = YEAR_2022,
            launchDateUnix = UNIX_2022_JAN_01,
            launchDateUtc = UTC_2022_JAN_01,
            launchDateLocal = LOCAL_2021_DEC_31,
            rocketId = STARSHIP_ID,
            rocketName = STARSHIP_NAME,
            rocketType = ROCKET_TYPE_SN,
            launchSiteName = STARBASE_SITE_NAME,
            launchSiteNameLong = STARBASE_SITE_LONG,
            launchSuccess = null,
            upcoming = true,
            details = null,
            missionPatchUrl = null,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = null,
            wikipediaUrl = null,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.launchDate).isEqualTo(FORMATTED_JAN_01_2022)
        assertThat(domainModel.launchTime).isEqualTo(FORMATTED_TIME_00_00)
    }

    @Test
    fun `formatDateTime handles alternative UTC format without milliseconds`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = FLIGHT_4,
            missionName = ALT_FORMAT_TEST_MISSION,
            launchYear = YEAR_2023,
            launchDateUnix = UNIX_2023_JAN_01,
            launchDateUtc = UTC_2023_JAN_01_NO_MS, // No milliseconds
            launchDateLocal = LOCAL_2022_DEC_31,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_BLOCK5,
            launchSiteName = KSC_SITE_NAME,
            launchSiteNameLong = KSC_SITE_LONG,
            launchSuccess = true,
            upcoming = false,
            details = null,
            missionPatchUrl = null,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = null,
            wikipediaUrl = null,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.launchDate).isEqualTo(FORMATTED_JAN_01_2023)
        assertThat(domainModel.launchTime).isEqualTo(FORMATTED_TIME_00_00)
    }

    @Test
    fun `formatDateTime handles malformed date gracefully`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = FLIGHT_5,
            missionName = MALFORMED_DATE_TEST_MISSION,
            launchYear = YEAR_2024,
            launchDateUnix = UNIX_2024_JAN_01,
            launchDateUtc = INVALID_DATE_FORMAT,
            launchDateLocal = LOCAL_2024_JAN_01,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_BLOCK5,
            launchSiteName = KSC_SITE_NAME,
            launchSiteNameLong = KSC_SITE_LONG,
            launchSuccess = null,
            upcoming = true,
            details = null,
            missionPatchUrl = null,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = null,
            wikipediaUrl = null,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.launchDate).isEqualTo(INVALID_DATE_FORMAT)
        assertThat(domainModel.launchTime).isEqualTo(EMPTY_TIME)
    }

    @Test
    fun `LaunchDto toEntity handles complex payload with multiple customers`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = FLIGHT_6,
            missionName = MULTI_CUSTOMER_MISSION,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_2020_JAN_15,
            launchDateUtc = UTC_2020_JAN_15,
            launchDateLocal = LOCAL_2020_JAN_15,
            rocket = RocketDto(
                rocketId = FALCON9_ID,
                rocketName = FALCON9_NAME,
                rocketType = ROCKET_TYPE_FT,
                secondStage = SecondStageDto(
                    payloads = listOf(
                        PayloadDto(
                            payloadId = PAYLOAD_ID_1,
                            customers = MULTI_CUSTOMERS,
                            payloadType = MULTI_SATELLITE_TYPE,
                            payloadMassKg = PAYLOAD_MASS_15000_5,
                            orbit = ORBIT_GTO
                        )
                    )
                )
            ),
            launchSite = LaunchSiteDto(
                siteId = KSC_SITE_ID,
                siteName = KSC_SITE_NAME,
                siteNameLong = KSC_SITE_LONG
            ),
            launchSuccess = true,
            links = LinksDto()
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.customers).isEqualTo(CUSTOMERS_MULTI)
        assertThat(entity.payloadType).isEqualTo(MULTI_SATELLITE_TYPE)
        assertThat(entity.payloadMassKg).isEqualTo(PAYLOAD_MASS_15000_5)
        assertThat(entity.orbit).isEqualTo(ORBIT_GTO)
    }

    @Test
    fun `LaunchEntity toDomainModel handles null success status`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = FLIGHT_7,
            missionName = UNKNOWN_SUCCESS_MISSION,
            launchYear = YEAR_2025,
            launchDateUnix = UNIX_2025_JAN_01,
            launchDateUtc = UTC_2025_JAN_01,
            launchDateLocal = LOCAL_2024_DEC_31,
            rocketId = STARSHIP_ID,
            rocketName = STARSHIP_NAME,
            rocketType = ROCKET_TYPE_SN,
            launchSiteName = STARBASE_SITE_NAME,
            launchSiteNameLong = STARBASE_SITE_LONG,
            launchSuccess = null,
            upcoming = true,
            details = FUTURE_TEST_MISSION,
            missionPatchUrl = null,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = null,
            wikipediaUrl = null,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.success).isNull()
        assertThat(domainModel.id).isEqualTo(EXPECTED_ID_7)
    }

    @Test
    fun `formatDateTime preserves specific time formats correctly`() {
        // Given - Test various times of day
        val morningEntity = LaunchEntity(
            flightNumber = FLIGHT_8,
            missionName = MORNING_LAUNCH_MISSION,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_2020_JAN_15,
            launchDateUtc = UTC_MORNING_LAUNCH,
            launchDateLocal = LOCAL_MORNING_LAUNCH,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_FT,
            launchSiteName = KSC_SITE_NAME,
            launchSiteNameLong = KSC_SITE_LONG,
            launchSuccess = true,
            upcoming = false,
            details = null,
            missionPatchUrl = null,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = null,
            wikipediaUrl = null,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )

        // When
        val domainModel = morningEntity.toDomainModel()

        // Then
        assertThat(domainModel.launchTime).isEqualTo(FORMATTED_TIME_06_15)
    }

    @Test
    fun `LaunchDto toEntity prioritizes first payload when multiple payloads exist`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = FLIGHT_9,
            missionName = MULTI_PAYLOAD_MISSION,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_2020_JAN_15,
            launchDateUtc = UTC_2020_JAN_15,
            launchDateLocal = LOCAL_2020_JAN_15,
            rocket = RocketDto(
                rocketId = FALCON9_ID,
                rocketName = FALCON9_NAME,
                rocketType = ROCKET_TYPE_FT,
                secondStage = SecondStageDto(
                    payloads = listOf(
                        PayloadDto(
                            payloadId = PRIMARY_PAYLOAD_ID,
                            customers = PRIMARY_CUSTOMER,
                            payloadType = PRIMARY_SATELLITE_TYPE,
                            payloadMassKg = PAYLOAD_MASS_3000,
                            orbit = ORBIT_LEO
                        ),
                        PayloadDto(
                            payloadId = SECONDARY_PAYLOAD_ID,
                            customers = SECONDARY_CUSTOMER,
                            payloadType = SECONDARY_SATELLITE_TYPE,
                            payloadMassKg = PAYLOAD_MASS_1000,
                            orbit = ORBIT_GTO
                        )
                    )
                )
            ),
            launchSite = LaunchSiteDto(
                siteId = KSC_SITE_ID,
                siteName = KSC_SITE_NAME,
                siteNameLong = KSC_SITE_LONG
            ),
            launchSuccess = true,
            links = LinksDto()
        )

        // When
        val entity = launchDto.toEntity()

        // Then - Should use first payload data
        assertThat(entity.customers).isEqualTo(CUSTOMERS_PRIMARY)
        assertThat(entity.payloadType).isEqualTo(PRIMARY_SATELLITE_TYPE)
        assertThat(entity.payloadMassKg).isEqualTo(PAYLOAD_MASS_3000)
        assertThat(entity.orbit).isEqualTo(ORBIT_LEO)
    }
}