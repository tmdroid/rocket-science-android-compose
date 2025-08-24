package com.mindera.rocketscience.data.repository

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.local.datasource.LocalDataSource
import com.mindera.rocketscience.data.remote.datasource.RemoteDataSource
import com.mindera.rocketscience.data.remote.dto.LaunchDto
import com.mindera.rocketscience.data.remote.dto.LaunchSiteDto
import com.mindera.rocketscience.data.remote.dto.LinksDto
import com.mindera.rocketscience.data.remote.dto.RocketDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import com.mindera.rocketscience.data.local.entity.LaunchEntity

class LaunchesRepositoryTest {

    companion object {
        private const val FLIGHT_NUMBER_1 = 1
        private const val FLIGHT_NUMBER_2 = 2
        private const val MISSION_1 = "Mission 1"
        private const val MISSION_2 = "Mission 2"
        private const val YEAR_2020 = "2020"
        private const val YEAR_2019 = "2019"
        private const val UNIX_TIMESTAMP = 1579082400L
        private const val UTC_DATE = "2020-01-15T10:30:00.000Z"
        private const val UTC_DATE_2 = "2020-01-16T10:30:00.000Z"
        private const val UTC_DATE_OLD = "2019-12-16T10:30:00.000Z"
        private const val LOCAL_DATE = "2020-01-15T10:30:00-05:00"
        private const val LOCAL_DATE_2 = "2020-01-16T10:30:00-05:00"
        private const val LOCAL_DATE_OLD = "2019-12-16T10:30:00-05:00"
        private const val FALCON9_ID = "falcon9"
        private const val FALCON9_NAME = "Falcon 9"
        private const val FALCON_HEAVY_ID = "falconheavy"
        private const val FALCON_HEAVY_NAME = "Falcon Heavy"
        private const val FALCON_HEAVY_TYPE = "FH"
        private const val ROCKET_TYPE_FT = "FT"
        private const val OLD_ROCKET_ID = "oldrocket"
        private const val OLD_ROCKET_NAME = "Old Rocket"
        private const val OLD_ROCKET_TYPE = "V1"
        private const val SITE_ID = "ksc_lc_39a"
        private const val SITE_NAME = "KSC LC 39A"
        private const val SITE_LONG_NAME = "Kennedy Space Center Historic Launch Complex 39A"
        private const val OLD_SITE_NAME = "Old Site"
        private const val OLD_SITE_LONG_NAME = "Old Launch Site"
        private const val PATCH_URL = "https://example.com/patch1.png"
        private const val WIKIPEDIA_URL = "https://wikipedia.com/mission1"
        private const val VIDEO_URL = "https://youtube.com/watch1"
        private const val API_ERROR = "API error"
        private const val DB_DELETE_ERROR = "Database delete failed"
        private const val DB_INSERT_ERROR = "Database insert failed"
        private const val LARGE_DATASET_SIZE = 100
        private const val MISSION_PREFIX = "Mission "
        private const val OLD_MISSION = "Old Mission"
        private const val NETWORK_TIMEOUT_ERROR = "Network timeout"
        private const val SERVICE_UNAVAILABLE_ERROR = "Service unavailable"
        private const val NO_INTERNET_ERROR = "No internet connection"
        private const val DAYS_30_IN_SECONDS = 30 * 24 * 60 * 60
        private const val DAYS_30_IN_MILLIS = 30 * 24 * 60 * 60 * 1000L
        private const val DAY_IN_SECONDS = 86400
    }

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: LaunchesRepository

    // Test data
    private val launchDto1 = LaunchDto(
        flightNumber = FLIGHT_NUMBER_1,
        missionName = MISSION_1,
        launchYear = YEAR_2020,
        launchDateUnix = UNIX_TIMESTAMP,
        launchDateUtc = UTC_DATE,
        launchDateLocal = LOCAL_DATE,
        rocket = RocketDto(
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_FT
        ),
        launchSite = LaunchSiteDto(
            siteId = SITE_ID,
            siteName = SITE_NAME,
            siteNameLong = SITE_LONG_NAME
        ),
        launchSuccess = true,
        links = LinksDto(
            missionPatch = PATCH_URL,
            wikipedia = WIKIPEDIA_URL,
            videoLink = VIDEO_URL
        )
    )

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        localDataSource = mockk(relaxed = true)
        repository = LaunchesRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Test
    fun `refreshLaunches successfully updates local data`() = runTest {
        // Given
        val remoteDtos = listOf(launchDto1)
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(remoteDtos)

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify { localDataSource.deleteAllLaunches() }
        coVerify { localDataSource.insertLaunches(any()) }
    }

    @Test
    fun `refreshLaunches handles remote data source failure`() = runTest {
        // Given
        val errorMessage = API_ERROR
        coEvery { remoteDataSource.getAllLaunches() } returns Result.failure(Exception(errorMessage))

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)

        // Should not modify local data on failure
        coVerify(exactly = 0) { localDataSource.deleteAllLaunches() }
        coVerify(exactly = 0) { localDataSource.insertLaunches(any()) }
    }

    @Test
    fun `refreshLaunches handles local data source failure during delete`() = runTest {
        // Given
        val remoteDtos = listOf(launchDto1)
        val errorMessage = DB_DELETE_ERROR
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(remoteDtos)
        coEvery { localDataSource.deleteAllLaunches() } throws Exception(errorMessage)

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `refreshLaunches handles local data source failure during insert`() = runTest {
        // Given
        val remoteDtos = listOf(launchDto1)
        val errorMessage = DB_INSERT_ERROR
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(remoteDtos)
        coEvery { localDataSource.insertLaunches(any()) } throws Exception(errorMessage)

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `refreshLaunches handles empty remote data`() = runTest {
        // Given
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(emptyList())

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isSuccess).isTrue()

        // Should handle empty list correctly
        coVerify { localDataSource.deleteAllLaunches() }
        coVerify { localDataSource.insertLaunches(emptyList()) }
    }

    @Test
    fun `refreshLaunches properly maps DTOs to entities`() = runTest {
        // Given
        val remoteDtos = listOf(launchDto1)
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(remoteDtos)

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isSuccess).isTrue()

        // Verify the mapping occurred by checking the insert call
        coVerify {
            localDataSource.insertLaunches(match { entities ->
                entities.size == 1 &&
                        entities[0].flightNumber == FLIGHT_NUMBER_1 &&
                        entities[0].missionName == MISSION_1 &&
                        entities[0].rocketName == FALCON9_NAME
            })
        }
    }

    @Test
    fun `refreshLaunches handles large datasets correctly`() = runTest {
        // Given
        val largeDtoList = (1..LARGE_DATASET_SIZE).map { index ->
            launchDto1.copy(flightNumber = index, missionName = MISSION_PREFIX + index)
        }
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(largeDtoList)

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isSuccess).isTrue()

        coVerify {
            localDataSource.insertLaunches(match { entities ->
                entities.size == LARGE_DATASET_SIZE
            })
        }
    }

    // getLaunches() method tests for offline-first behavior and stale data preservation

    @Test
    fun `getLaunches preserves stale data when refresh fails`() = runTest {
        // Given - stale cached data exists
        val cachedEntity = LaunchEntity(
            flightNumber = FLIGHT_NUMBER_1,
            missionName = MISSION_1,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_TIMESTAMP,
            launchDateUtc = UTC_DATE,
            launchDateLocal = LOCAL_DATE,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_FT,
            launchSiteName = SITE_NAME,
            launchSiteNameLong = SITE_LONG_NAME,
            launchSuccess = true,
            upcoming = false,
            details = null,
            missionPatchUrl = PATCH_URL,
            missionPatchSmallUrl = null,
            articleUrl = null,
            videoUrl = VIDEO_URL,
            wikipediaUrl = WIKIPEDIA_URL,
            customers = null,
            payloadType = null,
            orbit = null,
            payloadMassKg = null
        )
        
        coEvery { localDataSource.getAllLaunches() } returns flowOf(listOf(cachedEntity))
        coEvery { localDataSource.isDataStale() } returns true
        coEvery { remoteDataSource.getAllLaunches() } returns Result.failure(Exception(API_ERROR))

        // When
        val results = repository.getLaunches().toList()

        // Then - should emit cached data only, no error
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].missionName).isEqualTo(MISSION_1)
        assertThat(launches[0].rocketName).isEqualTo(FALCON9_NAME)
    }

    @Test
    fun `getLaunches keeps stale data when network exception occurs`() = runTest {
        // Given - multiple stale cached launches
        val cachedEntities = listOf(
            LaunchEntity(
                flightNumber = FLIGHT_NUMBER_1,
                missionName = MISSION_1,
                launchYear = YEAR_2020,
                launchDateUnix = UNIX_TIMESTAMP,
                launchDateUtc = UTC_DATE,
                launchDateLocal = LOCAL_DATE,
                rocketId = FALCON9_ID,
                rocketName = FALCON9_NAME,
                rocketType = ROCKET_TYPE_FT,
                launchSiteName = SITE_NAME,
                launchSiteNameLong = SITE_LONG_NAME,
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
            ),
            LaunchEntity(
                flightNumber = FLIGHT_NUMBER_2,
                missionName = MISSION_2,
                launchYear = YEAR_2020,
                launchDateUnix = UNIX_TIMESTAMP + DAY_IN_SECONDS,
                launchDateUtc = UTC_DATE_2,
                launchDateLocal = LOCAL_DATE_2,
                rocketId = FALCON_HEAVY_ID,
                rocketName = FALCON_HEAVY_NAME,
                rocketType = FALCON_HEAVY_TYPE,
                launchSiteName = SITE_NAME,
                launchSiteNameLong = SITE_LONG_NAME,
                launchSuccess = false,
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
        )
        
        coEvery { localDataSource.getAllLaunches() } returns flowOf(cachedEntities)
        coEvery { localDataSource.isDataStale() } returns true
        coEvery { remoteDataSource.getAllLaunches() } throws java.net.SocketTimeoutException(NETWORK_TIMEOUT_ERROR)

        // When
        val results = repository.getLaunches().toList()

        // Then - should preserve all cached launches despite network error
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(2)
        assertThat(launches[0].missionName).isEqualTo(MISSION_1)
        assertThat(launches[1].missionName).isEqualTo(MISSION_2)
    }

    @Test
    fun `getLaunches shows very old cached data when remote is unavailable`() = runTest {
        // Given - very old cached data (30 days old)
        val veryOldEntity = LaunchEntity(
            flightNumber = FLIGHT_NUMBER_1,
            missionName = OLD_MISSION,
            launchYear = YEAR_2019,
            launchDateUnix = UNIX_TIMESTAMP - DAYS_30_IN_SECONDS, // 30 days ago
            launchDateUtc = UTC_DATE_OLD,
            launchDateLocal = LOCAL_DATE_OLD,
            rocketId = OLD_ROCKET_ID,
            rocketName = OLD_ROCKET_NAME,
            rocketType = OLD_ROCKET_TYPE,
            launchSiteName = OLD_SITE_NAME,
            launchSiteNameLong = OLD_SITE_LONG_NAME,
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
            payloadMassKg = null,
            lastUpdated = System.currentTimeMillis() - DAYS_30_IN_MILLIS // 30 days ago
        )
        
        coEvery { localDataSource.getAllLaunches() } returns flowOf(listOf(veryOldEntity))
        coEvery { localDataSource.isDataStale() } returns true
        coEvery { remoteDataSource.getAllLaunches() } returns Result.failure(Exception(SERVICE_UNAVAILABLE_ERROR))

        // When
        val results = repository.getLaunches().toList()

        // Then - should still show very old cached data
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].missionName).isEqualTo(OLD_MISSION)
        assertThat(launches[0].rocketName).isEqualTo(OLD_ROCKET_NAME)
        assertThat(launches[0].rocketType).isEqualTo(OLD_ROCKET_TYPE)
    }

    @Test
    fun `getLaunches emits error only when no cached data exists and remote fails`() = runTest {
        // Given - no cached data and remote failure
        coEvery { localDataSource.getAllLaunches() } returns flowOf(emptyList())
        coEvery { localDataSource.isDataStale() } returns true // Will be true when no data exists
        coEvery { remoteDataSource.getAllLaunches() } returns Result.failure(Exception(API_ERROR))

        // When
        val results = repository.getLaunches().toList()

        // Then - should emit error since no cached data is available
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(API_ERROR)
    }

    @Test
    fun `getLaunches handles IOException while preserving cached launches`() = runTest {
        // Given - cached launches exist
        val cachedEntity = LaunchEntity(
            flightNumber = FLIGHT_NUMBER_1,
            missionName = MISSION_1,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_TIMESTAMP,
            launchDateUtc = UTC_DATE,
            launchDateLocal = LOCAL_DATE,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_FT,
            launchSiteName = SITE_NAME,
            launchSiteNameLong = SITE_LONG_NAME,
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
        
        coEvery { localDataSource.getAllLaunches() } returns flowOf(listOf(cachedEntity))
        coEvery { localDataSource.isDataStale() } returns true
        coEvery { remoteDataSource.getAllLaunches() } throws java.io.IOException(NO_INTERNET_ERROR)

        // When
        val results = repository.getLaunches().toList()

        // Then - should preserve cached data despite IOException
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].missionName).isEqualTo(MISSION_1)
    }

    @Test
    fun `getLaunches emits fresh data after successful refresh of stale cache`() = runTest {
        // Given - stale cached data and successful remote refresh
        val cachedEntity = LaunchEntity(
            flightNumber = FLIGHT_NUMBER_1,
            missionName = OLD_MISSION,
            launchYear = YEAR_2019,
            launchDateUnix = UNIX_TIMESTAMP - DAY_IN_SECONDS, // 1 day ago
            launchDateUtc = UTC_DATE_OLD,
            launchDateLocal = LOCAL_DATE_OLD,
            rocketId = OLD_ROCKET_ID,
            rocketName = OLD_ROCKET_NAME,
            rocketType = OLD_ROCKET_TYPE,
            launchSiteName = OLD_SITE_NAME,
            launchSiteNameLong = OLD_SITE_LONG_NAME,
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
        
        val freshEntity = LaunchEntity(
            flightNumber = FLIGHT_NUMBER_1,
            missionName = MISSION_1,
            launchYear = YEAR_2020,
            launchDateUnix = UNIX_TIMESTAMP,
            launchDateUtc = UTC_DATE,
            launchDateLocal = LOCAL_DATE,
            rocketId = FALCON9_ID,
            rocketName = FALCON9_NAME,
            rocketType = ROCKET_TYPE_FT,
            launchSiteName = SITE_NAME,
            launchSiteNameLong = SITE_LONG_NAME,
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
        
        // First return cached data, then after refresh return fresh data
        coEvery { localDataSource.getAllLaunches() } returnsMany listOf(
            flowOf(listOf(cachedEntity)),  // Initial cached data
            flowOf(listOf(freshEntity))    // After refresh
        )
        coEvery { localDataSource.isDataStale() } returns true
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(listOf(launchDto1))

        // When
        val results = repository.getLaunches().toList()

        // Then - should emit both cached data first, then fresh data
        assertThat(results).hasSize(2)
        
        // First emission: cached data
        val cachedResult = results[0]
        assertThat(cachedResult.isSuccess).isTrue()
        val cachedLaunches = cachedResult.getOrNull()!!
        assertThat(cachedLaunches[0].missionName).isEqualTo(OLD_MISSION)
        
        // Second emission: fresh data
        val freshResult = results[1]
        assertThat(freshResult.isSuccess).isTrue()
        val freshLaunches = freshResult.getOrNull()!!
        assertThat(freshLaunches[0].missionName).isEqualTo(MISSION_1)
    }
}