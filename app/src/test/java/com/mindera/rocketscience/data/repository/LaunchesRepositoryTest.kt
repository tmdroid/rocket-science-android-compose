package com.mindera.rocketscience.data.repository

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.local.datasource.LocalDataSource
import com.mindera.rocketscience.data.local.entity.LaunchEntity
import com.mindera.rocketscience.data.remote.datasource.RemoteDataSource
import com.mindera.rocketscience.data.remote.dto.LaunchDto
import com.mindera.rocketscience.data.remote.dto.LaunchSiteDto
import com.mindera.rocketscience.data.remote.dto.LinksDto
import com.mindera.rocketscience.data.remote.dto.RocketDto
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class LaunchesRepositoryTest {

    private lateinit var remoteDataSource: RemoteDataSource
    private lateinit var localDataSource: LocalDataSource
    private lateinit var repository: LaunchesRepository

    // Test data
    private val launchEntity1 = LaunchEntity(
        flightNumber = 1,
        missionName = "Mission 1",
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
        details = "Mission 1 details",
        missionPatchUrl = "https://example.com/patch1.png",
        missionPatchSmallUrl = null,
        articleUrl = null,
        videoUrl = "https://youtube.com/watch1",
        wikipediaUrl = "https://wikipedia.com/mission1",
        customers = null,
        payloadType = null,
        orbit = null,
        payloadMassKg = null
    )

    private val launchDto1 = LaunchDto(
        flightNumber = 1,
        missionName = "Mission 1",
        launchYear = "2020",
        launchDateUnix = 1579082400L,
        launchDateUtc = "2020-01-15T10:30:00.000Z",
        launchDateLocal = "2020-01-15T10:30:00-05:00",
        rocket = RocketDto(
            rocketId = "falcon9",
            rocketName = "Falcon 9",
            rocketType = "FT"
        ),
        launchSite = LaunchSiteDto(
            siteId = "ksc_lc_39a",
            siteName = "KSC LC 39A",
            siteNameLong = "Kennedy Space Center Historic Launch Complex 39A"
        ),
        launchSuccess = true,
        links = LinksDto(
            missionPatch = "https://example.com/patch1.png",
            wikipedia = "https://wikipedia.com/mission1",
            videoLink = "https://youtube.com/watch1"
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
        val errorMessage = "API error"
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
        val errorMessage = "Database delete failed"
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
        val errorMessage = "Database insert failed"
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
                entities[0].flightNumber == 1 &&
                entities[0].missionName == "Mission 1" &&
                entities[0].rocketName == "Falcon 9"
            })
        }
    }

    @Test
    fun `refreshLaunches handles large datasets correctly`() = runTest {
        // Given
        val largeDtoList = (1..100).map { index ->
            launchDto1.copy(flightNumber = index, missionName = "Mission $index")
        }
        coEvery { remoteDataSource.getAllLaunches() } returns Result.success(largeDtoList)

        // When
        val result = repository.refreshLaunches()

        // Then
        assertThat(result.isSuccess).isTrue()
        
        coVerify { 
            localDataSource.insertLaunches(match { entities ->
                entities.size == 100
            })
        }
    }
}