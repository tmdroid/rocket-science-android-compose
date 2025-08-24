package com.mindera.rocketscience.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.repository.LaunchesRepository
import com.mindera.rocketscience.domain.model.Launch
import com.mindera.rocketscience.domain.model.LaunchStatus
import com.mindera.rocketscience.features.launches.LaunchFilterState
import com.mindera.rocketscience.features.launches.LaunchSuccessFilter
import com.mindera.rocketscience.features.launches.SortOrder
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetLaunchesUseCaseTest {

    private lateinit var repository: LaunchesRepository
    private lateinit var useCase: GetLaunchesUseCase

    // Test data
    private val currentUnixTime = System.currentTimeMillis() / 1000
    private val oneDayInSeconds = 24 * 60 * 60

    private val launch2020 = Launch(
        id = "1",
        missionName = "Mission 2020",
        launchDate = "Jan 15, 2020",
        launchTime = "10:30",
        launchDateUnix = currentUnixTime - (365 * oneDayInSeconds), // 1 year ago
        rocketName = "Falcon 9",
        rocketType = "FT",
        missionPatchUrl = "https://example.com/patch1.png",
        success = true,
        wikipediaUrl = "https://wikipedia.com/mission1",
        videoUrl = "https://youtube.com/watch1"
    )

    private val launch2021 = Launch(
        id = "2",
        missionName = "Mission 2021",
        launchDate = "Mar 20, 2021",
        launchTime = "14:45",
        launchDateUnix = currentUnixTime - (200 * oneDayInSeconds), // 200 days ago
        rocketName = "Falcon Heavy",
        rocketType = "FH",
        missionPatchUrl = "https://example.com/patch2.png",
        success = false,
        wikipediaUrl = "https://wikipedia.com/mission2",
        videoUrl = null
    )

    private val launch2022 = Launch(
        id = "3",
        missionName = "Mission 2022",
        launchDate = "Dec 05, 2022",
        launchTime = "08:15",
        launchDateUnix = currentUnixTime - (30 * oneDayInSeconds), // 30 days ago
        rocketName = "Starship",
        rocketType = "SN",
        missionPatchUrl = null,
        success = null,
        wikipediaUrl = "https://wikipedia.com/mission3",
        videoUrl = "https://youtube.com/watch3"
    )

    private val futureLaunch = Launch(
        id = "4",
        missionName = "Future Mission",
        launchDate = "Jun 15, 2025",
        launchTime = "16:00",
        launchDateUnix = currentUnixTime + (180 * oneDayInSeconds), // 180 days from now
        rocketName = "Super Heavy",
        rocketType = "SH",
        missionPatchUrl = "https://example.com/patch4.png",
        success = null,
        wikipediaUrl = null,
        videoUrl = "https://youtube.com/watch4"
    )

    private val allLaunches = listOf(launch2020, launch2021, launch2022, futureLaunch)

    @Before
    fun setUp() {
        repository = mockk()
        useCase = GetLaunchesUseCase(repository)
    }

    @Test
    fun `invoke with default filter returns all launches sorted by newest first`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(4)

        // Should be sorted by newest first (DESC)
        assertThat(launches[0].id).isEqualTo("4") // Future launch (highest unix time)
        assertThat(launches[1].id).isEqualTo("3") // 2022 launch
        assertThat(launches[2].id).isEqualTo("2") // 2021 launch  
        assertThat(launches[3].id).isEqualTo("1") // 2020 launch
    }

    @Test
    fun `invoke with ASC sort order returns launches sorted by oldest first`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))
        val filterState = LaunchFilterState(sortOrder = SortOrder.ASC)

        // When
        val result = useCase(filterState).first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!

        // Should be sorted by oldest first (ASC)
        assertThat(launches[0].id).isEqualTo("1") // 2020 launch (lowest unix time)
        assertThat(launches[1].id).isEqualTo("2") // 2021 launch
        assertThat(launches[2].id).isEqualTo("3") // 2022 launch
        assertThat(launches[3].id).isEqualTo("4") // Future launch
    }

    @Test
    fun `invoke with year filter returns only launches from specified year`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))
        val filterState = LaunchFilterState(selectedYear = "2021")

        // When
        val result = useCase(filterState).first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].id).isEqualTo("2")
        assertThat(launches[0].launchYear).isEqualTo("2021")
    }

    @Test
    fun `invoke with success filter SUCCESS_ONLY returns only successful launches`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))
        val filterState = LaunchFilterState(launchSuccess = LaunchSuccessFilter.SUCCESS_ONLY)

        // When
        val result = useCase(filterState).first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].id).isEqualTo("1")
        assertThat(launches[0].success).isTrue()
    }

    @Test
    fun `invoke with success filter FAILED_ONLY returns only failed launches`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))
        val filterState = LaunchFilterState(launchSuccess = LaunchSuccessFilter.FAILED_ONLY)

        // When
        val result = useCase(filterState).first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].id).isEqualTo("2")
        assertThat(launches[0].success).isFalse()
    }

    @Test
    fun `invoke with combined filters applies all filters correctly`() = runTest {
        // Given - Add more 2021 launches for better testing
        val extraLaunches = allLaunches + listOf(
            launch2021.copy(id = "5", success = true), // successful 2021 launch
            launch2021.copy(id = "6", success = false) // another failed 2021 launch
        )
        every { repository.getLaunches() } returns flowOf(Result.success(extraLaunches))

        val filterState = LaunchFilterState(
            selectedYear = "2021",
            launchSuccess = LaunchSuccessFilter.SUCCESS_ONLY,
            sortOrder = SortOrder.ASC
        )

        // When
        val result = useCase(filterState).first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).hasSize(1)
        assertThat(launches[0].id).isEqualTo("5")
        assertThat(launches[0].launchYear).isEqualTo("2021")
        assertThat(launches[0].success).isTrue()
    }

    @Test
    fun `invoke returns empty list when no launches match filter`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))
        val filterState = LaunchFilterState(selectedYear = "1999") // Non-existent year

        // When
        val result = useCase(filterState).first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!
        assertThat(launches).isEmpty()
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        // Given
        val errorMessage = "Network error"
        every { repository.getLaunches() } returns flowOf(Result.failure(Exception(errorMessage)))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `launch status calculation returns correct values`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(allLaunches))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launches = result.getOrNull()!!

        // Check past launches have DaysSinceLaunch status
        val pastLaunches = launches.filter { it.launchDateUnix < currentUnixTime }
        pastLaunches.forEach { launch ->
            assertThat(launch.launchStatus).isInstanceOf(LaunchStatus.DaysSinceLaunch::class.java)
            val status = launch.launchStatus as LaunchStatus.DaysSinceLaunch
            assertThat(status.days).isGreaterThan(0)
        }

        // Check future launches have DaysUntilLaunch status
        val futureLaunches = launches.filter { it.launchDateUnix > currentUnixTime }
        futureLaunches.forEach { launch ->
            assertThat(launch.launchStatus).isInstanceOf(LaunchStatus.DaysUntilLaunch::class.java)
            val status = launch.launchStatus as LaunchStatus.DaysUntilLaunch
            assertThat(status.days).isGreaterThan(0)
        }
    }

    @Test
    fun `ui model mapping is correct`() = runTest {
        // Given
        every { repository.getLaunches() } returns flowOf(Result.success(listOf(launch2020)))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val launch = result.getOrNull()!!.first()

        assertThat(launch.id).isEqualTo("1")
        assertThat(launch.name).isEqualTo("Mission 2020")
        assertThat(launch.dateTime).isEqualTo("Jan 15, 2020 at 10:30")
        assertThat(launch.rocketInfo).isEqualTo("Falcon 9 / FT")
        assertThat(launch.missionPatchUrl).isEqualTo("https://example.com/patch1.png")
        assertThat(launch.success).isTrue()
        assertThat(launch.launchYear).isEqualTo("2020")
        assertThat(launch.launchDateUnix).isEqualTo(launch2020.launchDateUnix)
        assertThat(launch.wikipediaUrl).isEqualTo("https://wikipedia.com/mission1")
        assertThat(launch.videoUrl).isEqualTo("https://youtube.com/watch1")
    }
}