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
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LaunchesRepositoryTest {

    companion object {
        private const val FLIGHT_NUMBER_1 = 1
        private const val MISSION_1 = "Mission 1"
        private const val YEAR_2020 = "2020"
        private const val UNIX_TIMESTAMP = 1579082400L
        private const val UTC_DATE = "2020-01-15T10:30:00.000Z"
        private const val LOCAL_DATE = "2020-01-15T10:30:00-05:00"
        private const val FALCON9_ID = "falcon9"
        private const val FALCON9_NAME = "Falcon 9"
        private const val ROCKET_TYPE_FT = "FT"
        private const val SITE_ID = "ksc_lc_39a"
        private const val SITE_NAME = "KSC LC 39A"
        private const val SITE_LONG_NAME = "Kennedy Space Center Historic Launch Complex 39A"
        private const val PATCH_URL = "https://example.com/patch1.png"
        private const val WIKIPEDIA_URL = "https://wikipedia.com/mission1"
        private const val VIDEO_URL = "https://youtube.com/watch1"
        private const val API_ERROR = "API error"
        private const val DB_DELETE_ERROR = "Database delete failed"
        private const val DB_INSERT_ERROR = "Database insert failed"
        private const val LARGE_DATASET_SIZE = 100
        private const val MISSION_PREFIX = "Mission "
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
}