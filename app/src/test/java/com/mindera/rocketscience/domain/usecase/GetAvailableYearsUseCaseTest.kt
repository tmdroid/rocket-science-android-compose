package com.mindera.rocketscience.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.repository.LaunchesRepository
import com.mindera.rocketscience.domain.model.Launch
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetAvailableYearsUseCaseTest {

    private lateinit var repository: LaunchesRepository
    private lateinit var useCase: GetAvailableYearsUseCase

    // Test data - create launches with different years
    private val launch2018 = Launch(
        id = "1",
        missionName = "Mission 2018",
        launchDate = "Feb 06, 2018",
        launchTime = "20:45",
        launchDateUnix = 1517950800L,
        rocketName = "Falcon Heavy",
        rocketType = "FH",
        missionPatchUrl = "https://example.com/patch1.png",
        success = true,
        wikipediaUrl = "https://wikipedia.com/mission1",
        videoUrl = "https://youtube.com/watch1"
    )

    private val launch2019A = Launch(
        id = "2",
        missionName = "Mission 2019 A",
        launchDate = "Mar 15, 2019",
        launchTime = "14:30",
        launchDateUnix = 1552658400L,
        rocketName = "Falcon 9",
        rocketType = "FT",
        missionPatchUrl = "https://example.com/patch2.png",
        success = false,
        wikipediaUrl = "https://wikipedia.com/mission2",
        videoUrl = null
    )

    private val launch2019B = Launch(
        id = "3",
        missionName = "Mission 2019 B",
        launchDate = "Nov 20, 2019",
        launchTime = "08:00",
        launchDateUnix = 1574251200L,
        rocketName = "Falcon 9",
        rocketType = "Block 5",
        missionPatchUrl = null,
        success = true,
        wikipediaUrl = "https://wikipedia.com/mission3",
        videoUrl = "https://youtube.com/watch3"
    )

    private val launch2020 = Launch(
        id = "4",
        missionName = "Mission 2020",
        launchDate = "Dec 01, 2020",
        launchTime = "16:25",
        launchDateUnix = 1606838700L,
        rocketName = "Starship",
        rocketType = "SN",
        missionPatchUrl = "https://example.com/patch4.png",
        success = null,
        wikipediaUrl = null,
        videoUrl = "https://youtube.com/watch4"
    )

    private val launch2022 = Launch(
        id = "5",
        missionName = "Mission 2022",
        launchDate = "Jul 10, 2022",
        launchTime = "12:15",
        launchDateUnix = 1657458900L,
        rocketName = "Super Heavy",
        rocketType = "BN",
        missionPatchUrl = "https://example.com/patch5.png",
        success = true,
        wikipediaUrl = "https://wikipedia.com/mission5",
        videoUrl = "https://youtube.com/watch5"
    )

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetAvailableYearsUseCase(repository)
    }

    @Test
    fun `invoke returns unique years sorted in ascending order`() = runTest {
        // Given
        val launches = listOf(launch2020, launch2018, launch2019A, launch2019B, launch2022)
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(4)
        assertThat(years).containsExactly("2018", "2019", "2020", "2022").inOrder()
    }

    @Test
    fun `invoke removes duplicate years`() = runTest {
        // Given - Multiple launches from same years
        val launches = listOf(launch2019A, launch2019B) // Both from 2019
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(1)
        assertThat(years).containsExactly("2019")
    }

    @Test
    fun `invoke returns empty list when no launches available`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(emptyList()))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        assertThat(years).isEmpty()
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        // Given
        val errorMessage = "Database connection failed"
        every { repository.getLaunches() } returns flowOf(Result.failure(Exception(errorMessage)))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `invoke extracts year correctly from various date formats`() = runTest {
        // Given - Create launches with different date formats but same year
        val launchesWithSameYear = listOf(
            launch2019A.copy(launchDate = "Jan 01, 2019"), // Different format
            launch2019B.copy(launchDate = "Dec 31, 2019"), // Different format
            launch2020.copy(launchDate = "Feb 29, 2020")   // Leap year
        )
        every { repository.getLaunches() } returns flowOf(Result.success(launchesWithSameYear))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(2)
        assertThat(years).containsExactly("2019", "2020").inOrder()
    }

    @Test
    fun `invoke handles single launch correctly`() = runTest {
        // Given
        val launches = listOf(launch2018)
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(1)
        assertThat(years).containsExactly("2018")
    }

    @Test
    fun `invoke sorts years correctly with mixed chronological order`() = runTest {
        // Given - Launches in random chronological order
        val launches = listOf(launch2022, launch2018, launch2020, launch2019A)
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(4)
        assertThat(years).containsExactly("2018", "2019", "2020", "2022").inOrder()
    }

    @Test
    fun `invoke extracts year from end of date string correctly`() = runTest {
        // Given - Test the extractYear logic directly
        val launchWithCustomDate = launch2018.copy(launchDate = "Custom format ending in 2025")
        val launches = listOf(launchWithCustomDate)
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(1)
        assertThat(years).containsExactly("2025") // Should extract last 4 characters as year
    }

    @Test
    fun `invoke handles malformed date strings gracefully`() = runTest {
        // Given - Launch with short date string
        val launchWithShortDate = launch2018.copy(launchDate = "2021") // Only 4 characters
        val launches = listOf(launchWithShortDate)
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(1)
        assertThat(years).containsExactly("2021") // Should still work for 4-character strings
    }

    @Test
    fun `invoke handles very large dataset with many duplicates`() = runTest {
        // Given - Large list with many duplicates
        val launches = (1..100).map { index ->
            when (index % 3) {
                0 -> launch2018.copy(id = "$index")
                1 -> launch2019A.copy(id = "$index") 
                else -> launch2020.copy(id = "$index")
            }
        }
        every { repository.getLaunches() } returns flowOf(Result.success(launches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val years = result.getOrNull()!!
        
        assertThat(years).hasSize(3)
        assertThat(years).containsExactly("2018", "2019", "2020").inOrder()
    }
}