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
            flightNumber = 1,
            missionName = "Test Mission",
            launchYear = "2020",
            launchDateUnix = 1579082400L,
            launchDateUtc = "2020-01-15T10:30:00.000Z",
            launchDateLocal = "2020-01-15T10:30:00-05:00",
            rocket = RocketDto(
                rocketId = "falcon9",
                rocketName = "Falcon 9",
                rocketType = "FT",
                secondStage = SecondStageDto(
                    payloads = listOf(
                        PayloadDto(
                            payloadId = "payload1",
                            customers = listOf("NASA", "SpaceX"),
                            payloadType = "Satellite",
                            payloadMassKg = 5000.0,
                            orbit = "LEO"
                        )
                    )
                )
            ),
            launchSite = LaunchSiteDto(
                siteId = "ksc_lc_39a",
                siteName = "KSC LC 39A",
                siteNameLong = "Kennedy Space Center Historic Launch Complex 39A"
            ),
            launchSuccess = true,
            links = LinksDto(
                missionPatch = "https://example.com/patch.png",
                missionPatchSmall = "https://example.com/patch_small.png",
                articleLink = "https://example.com/article",
                wikipedia = "https://wikipedia.com/mission",
                videoLink = "https://youtube.com/watch"
            ),
            details = "Test mission details",
            upcoming = false
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.flightNumber).isEqualTo(1)
        assertThat(entity.missionName).isEqualTo("Test Mission")
        assertThat(entity.launchYear).isEqualTo("2020")
        assertThat(entity.launchDateUnix).isEqualTo(1579082400L)
        assertThat(entity.launchDateUtc).isEqualTo("2020-01-15T10:30:00.000Z")
        assertThat(entity.launchDateLocal).isEqualTo("2020-01-15T10:30:00-05:00")
        assertThat(entity.rocketId).isEqualTo("falcon9")
        assertThat(entity.rocketName).isEqualTo("Falcon 9")
        assertThat(entity.rocketType).isEqualTo("FT")
        assertThat(entity.launchSiteName).isEqualTo("KSC LC 39A")
        assertThat(entity.launchSiteNameLong).isEqualTo("Kennedy Space Center Historic Launch Complex 39A")
        assertThat(entity.launchSuccess).isTrue()
        assertThat(entity.upcoming).isFalse()
        assertThat(entity.details).isEqualTo("Test mission details")
        assertThat(entity.missionPatchUrl).isEqualTo("https://example.com/patch.png")
        assertThat(entity.missionPatchSmallUrl).isEqualTo("https://example.com/patch_small.png")
        assertThat(entity.articleUrl).isEqualTo("https://example.com/article")
        assertThat(entity.videoUrl).isEqualTo("https://youtube.com/watch")
        assertThat(entity.wikipediaUrl).isEqualTo("https://wikipedia.com/mission")
        assertThat(entity.customers).isEqualTo("[\"NASA\",\"SpaceX\"]")
        assertThat(entity.payloadType).isEqualTo("Satellite")
        assertThat(entity.orbit).isEqualTo("LEO")
        assertThat(entity.payloadMassKg).isEqualTo(5000.0)
    }

    @Test
    fun `LaunchDto toEntity handles null optional fields`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = 2,
            missionName = "Minimal Mission",
            launchYear = "2021",
            launchDateUnix = 1616249100L,
            launchDateUtc = "2021-03-20T14:45:00.000Z",
            launchDateLocal = "2021-03-20T14:45:00-05:00",
            rocket = RocketDto(
                rocketId = "falconheavy",
                rocketName = "Falcon Heavy",
                rocketType = "FH",
                secondStage = null
            ),
            launchSite = LaunchSiteDto(
                siteId = "vafb_slc_4e",
                siteName = "VAFB SLC 4E",
                siteNameLong = "Vandenberg Air Force Base Space Launch Complex 4E"
            ),
            launchSuccess = null,
            links = LinksDto(),
            details = null,
            upcoming = true
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.flightNumber).isEqualTo(2)
        assertThat(entity.missionName).isEqualTo("Minimal Mission")
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
            flightNumber = 3,
            missionName = "No Payload Mission",
            launchYear = "2022",
            launchDateUnix = 1640995200L,
            launchDateUtc = "2022-01-01T00:00:00.000Z",
            launchDateLocal = "2021-12-31T19:00:00-05:00",
            rocket = RocketDto(
                rocketId = "starship",
                rocketName = "Starship",
                rocketType = "SN",
                secondStage = SecondStageDto(payloads = emptyList())
            ),
            launchSite = LaunchSiteDto(
                siteId = "stls",
                siteName = "Starbase",
                siteNameLong = "SpaceX Starbase"
            ),
            launchSuccess = false,
            links = LinksDto(
                missionPatch = "https://example.com/patch.png"
            )
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.customers).isNull()
        assertThat(entity.payloadType).isNull()
        assertThat(entity.orbit).isNull()
        assertThat(entity.payloadMassKg).isNull()
        assertThat(entity.missionPatchUrl).isEqualTo("https://example.com/patch.png")
    }

    @Test
    fun `LaunchEntity toDomainModel maps all fields correctly`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = 1,
            missionName = "Test Mission",
            launchYear = "2020",
            launchDateUnix = 1579082400L,
            launchDateUtc = "2020-01-15T10:30:00.000Z",
            launchDateLocal = "2020-01-15T10:30:00-05:00",
            rocketId = "falcon9",
            rocketName = "Falcon 9",
            rocketType = "FT",
            launchSiteName = "KSC LC 39A",
            launchSiteNameLong = "Kennedy Space Center Historic Launch Complex 39A",
            launchSuccess = true,
            upcoming = false,
            details = "Test mission details",
            missionPatchUrl = "https://example.com/patch.png",
            missionPatchSmallUrl = "https://example.com/patch_small.png",
            articleUrl = "https://example.com/article",
            videoUrl = "https://youtube.com/watch",
            wikipediaUrl = "https://wikipedia.com/mission",
            customers = "[\"NASA\",\"SpaceX\"]",
            payloadType = "Satellite",
            orbit = "LEO",
            payloadMassKg = 5000.0
        )

        // When
        val domainModel = launchEntity.toDomainModel()

        // Then
        assertThat(domainModel.id).isEqualTo("1")
        assertThat(domainModel.missionName).isEqualTo("Test Mission")
        assertThat(domainModel.launchDate).isEqualTo("Jan 15, 2020")
        assertThat(domainModel.launchTime).isEqualTo("10:30")
        assertThat(domainModel.launchDateUnix).isEqualTo(1579082400L)
        assertThat(domainModel.rocketName).isEqualTo("Falcon 9")
        assertThat(domainModel.rocketType).isEqualTo("FT")
        assertThat(domainModel.missionPatchUrl).isEqualTo("https://example.com/patch_small.png") // Prefers small
        assertThat(domainModel.success).isTrue()
        assertThat(domainModel.wikipediaUrl).isEqualTo("https://wikipedia.com/mission")
        assertThat(domainModel.videoUrl).isEqualTo("https://youtube.com/watch")
    }

    @Test
    fun `LaunchEntity toDomainModel prefers regular patch when small is null`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = 2,
            missionName = "Mission with regular patch",
            launchYear = "2021",
            launchDateUnix = 1616249100L,
            launchDateUtc = "2021-03-20T14:45:00.000Z",
            launchDateLocal = "2021-03-20T14:45:00-05:00",
            rocketId = "falconheavy",
            rocketName = "Falcon Heavy",
            rocketType = "FH",
            launchSiteName = "VAFB SLC 4E",
            launchSiteNameLong = "Vandenberg Air Force Base Space Launch Complex 4E",
            launchSuccess = false,
            upcoming = false,
            details = null,
            missionPatchUrl = "https://example.com/patch.png",
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
        assertThat(domainModel.missionPatchUrl).isEqualTo("https://example.com/patch.png")
        assertThat(domainModel.success).isFalse()
        assertThat(domainModel.wikipediaUrl).isNull()
        assertThat(domainModel.videoUrl).isNull()
    }

    @Test
    fun `formatDateTime handles standard UTC format correctly`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = 3,
            missionName = "DateTime Test",
            launchYear = "2022",
            launchDateUnix = 1640995200L,
            launchDateUtc = "2022-01-01T00:00:00.000Z",
            launchDateLocal = "2021-12-31T19:00:00-05:00",
            rocketId = "starship",
            rocketName = "Starship",
            rocketType = "SN",
            launchSiteName = "Starbase",
            launchSiteNameLong = "SpaceX Starbase",
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
        assertThat(domainModel.launchDate).isEqualTo("Jan 01, 2022")
        assertThat(domainModel.launchTime).isEqualTo("00:00")
    }

    @Test
    fun `formatDateTime handles alternative UTC format without milliseconds`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = 4,
            missionName = "Alternative Format Test",
            launchYear = "2023",
            launchDateUnix = 1672531200L,
            launchDateUtc = "2023-01-01T00:00:00Z", // No milliseconds
            launchDateLocal = "2022-12-31T19:00:00-05:00",
            rocketId = "falcon9",
            rocketName = "Falcon 9",
            rocketType = "Block 5",
            launchSiteName = "KSC LC 39A",
            launchSiteNameLong = "Kennedy Space Center Historic Launch Complex 39A",
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
        assertThat(domainModel.launchDate).isEqualTo("Jan 01, 2023")
        assertThat(domainModel.launchTime).isEqualTo("00:00")
    }

    @Test
    fun `formatDateTime handles malformed date gracefully`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = 5,
            missionName = "Malformed Date Test",
            launchYear = "2024",
            launchDateUnix = 1704067200L,
            launchDateUtc = "invalid-date-format",
            launchDateLocal = "2024-01-01T00:00:00-05:00",
            rocketId = "falcon9",
            rocketName = "Falcon 9",
            rocketType = "Block 5",
            launchSiteName = "KSC LC 39A",
            launchSiteNameLong = "Kennedy Space Center Historic Launch Complex 39A",
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
        assertThat(domainModel.launchDate).isEqualTo("invalid-date-format")
        assertThat(domainModel.launchTime).isEqualTo("")
    }

    @Test
    fun `LaunchDto toEntity handles complex payload with multiple customers`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = 6,
            missionName = "Multi Customer Mission",
            launchYear = "2020",
            launchDateUnix = 1579082400L,
            launchDateUtc = "2020-01-15T10:30:00.000Z",
            launchDateLocal = "2020-01-15T10:30:00-05:00",
            rocket = RocketDto(
                rocketId = "falcon9",
                rocketName = "Falcon 9",
                rocketType = "FT",
                secondStage = SecondStageDto(
                    payloads = listOf(
                        PayloadDto(
                            payloadId = "payload1",
                            customers = listOf("NASA", "ESA", "JAXA", "Commercial Operator"),
                            payloadType = "Multi-Satellite Deployment",
                            payloadMassKg = 15000.5,
                            orbit = "GTO"
                        )
                    )
                )
            ),
            launchSite = LaunchSiteDto(
                siteId = "ksc_lc_39a",
                siteName = "KSC LC 39A",
                siteNameLong = "Kennedy Space Center Historic Launch Complex 39A"
            ),
            launchSuccess = true,
            links = LinksDto()
        )

        // When
        val entity = launchDto.toEntity()

        // Then
        assertThat(entity.customers).isEqualTo("[\"NASA\",\"ESA\",\"JAXA\",\"Commercial Operator\"]")
        assertThat(entity.payloadType).isEqualTo("Multi-Satellite Deployment")
        assertThat(entity.payloadMassKg).isEqualTo(15000.5)
        assertThat(entity.orbit).isEqualTo("GTO")
    }

    @Test
    fun `LaunchEntity toDomainModel handles null success status`() {
        // Given
        val launchEntity = LaunchEntity(
            flightNumber = 7,
            missionName = "Unknown Success Mission",
            launchYear = "2025",
            launchDateUnix = 1735689600L,
            launchDateUtc = "2025-01-01T00:00:00.000Z",
            launchDateLocal = "2024-12-31T19:00:00-05:00",
            rocketId = "starship",
            rocketName = "Starship",
            rocketType = "SN",
            launchSiteName = "Starbase",
            launchSiteNameLong = "SpaceX Starbase",
            launchSuccess = null,
            upcoming = true,
            details = "Future test mission",
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
        assertThat(domainModel.id).isEqualTo("7")
    }

    @Test
    fun `formatDateTime preserves specific time formats correctly`() {
        // Given - Test various times of day
        val morningEntity = LaunchEntity(
            flightNumber = 8,
            missionName = "Morning Launch",
            launchYear = "2020",
            launchDateUnix = 1579082400L,
            launchDateUtc = "2020-01-15T06:15:30.123Z",
            launchDateLocal = "2020-01-15T01:15:30-05:00",
            rocketId = "falcon9",
            rocketName = "Falcon 9",
            rocketType = "FT",
            launchSiteName = "KSC LC 39A",
            launchSiteNameLong = "Kennedy Space Center Historic Launch Complex 39A",
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
        assertThat(domainModel.launchTime).isEqualTo("06:15")
    }

    @Test
    fun `LaunchDto toEntity prioritizes first payload when multiple payloads exist`() {
        // Given
        val launchDto = LaunchDto(
            flightNumber = 9,
            missionName = "Multi Payload Mission",
            launchYear = "2020",
            launchDateUnix = 1579082400L,
            launchDateUtc = "2020-01-15T10:30:00.000Z",
            launchDateLocal = "2020-01-15T10:30:00-05:00",
            rocket = RocketDto(
                rocketId = "falcon9",
                rocketName = "Falcon 9",
                rocketType = "FT",
                secondStage = SecondStageDto(
                    payloads = listOf(
                        PayloadDto(
                            payloadId = "primary",
                            customers = listOf("Primary Customer"),
                            payloadType = "Primary Satellite",
                            payloadMassKg = 3000.0,
                            orbit = "LEO"
                        ),
                        PayloadDto(
                            payloadId = "secondary",
                            customers = listOf("Secondary Customer"),
                            payloadType = "Secondary Satellite",
                            payloadMassKg = 1000.0,
                            orbit = "GTO"
                        )
                    )
                )
            ),
            launchSite = LaunchSiteDto(
                siteId = "ksc_lc_39a",
                siteName = "KSC LC 39A",
                siteNameLong = "Kennedy Space Center Historic Launch Complex 39A"
            ),
            launchSuccess = true,
            links = LinksDto()
        )

        // When
        val entity = launchDto.toEntity()

        // Then - Should use first payload data
        assertThat(entity.customers).isEqualTo("[\"Primary Customer\"]")
        assertThat(entity.payloadType).isEqualTo("Primary Satellite")
        assertThat(entity.payloadMassKg).isEqualTo(3000.0)
        assertThat(entity.orbit).isEqualTo("LEO")
    }
}