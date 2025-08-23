package com.mindera.rocketscience.features.launches

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.domain.model.LaunchStatus
import com.mindera.rocketscience.domain.usecase.GetAvailableYearsUseCase
import com.mindera.rocketscience.domain.usecase.GetCompanyInfoUseCase
import com.mindera.rocketscience.domain.usecase.GetLaunchesUseCase
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class LaunchesViewModelTest {

    private lateinit var getLaunchesUseCase: GetLaunchesUseCase
    private lateinit var getCompanyInfoUseCase: GetCompanyInfoUseCase
    private lateinit var getAvailableYearsUseCase: GetAvailableYearsUseCase
    private lateinit var viewModel: LaunchesViewModel

    private val testDispatcher = StandardTestDispatcher()

    // Test data
    private val companyUiModel = CompanyUiModel(
        name = "SpaceX",
        founder = "Elon Musk",
        founded = 2002,
        employees = 12000,
        launchSites = 3,
        formattedValuation = "180.0 billion"
    )

    private val launchUiModel1 = LaunchUiModel(
        id = "1",
        name = "Mission 1",
        dateTime = "Jan 15, 2020 at 10:30",
        rocketInfo = "Falcon 9 / FT",
        missionPatchUrl = "https://example.com/patch1.png",
        success = true,
        launchStatus = LaunchStatus.DaysSinceLaunch(100),
        launchYear = "2020",
        launchDateUnix = 1579082400L,
        wikipediaUrl = "https://wikipedia.com/mission1",
        videoUrl = "https://youtube.com/watch1"
    )

    private val launchUiModel2 = LaunchUiModel(
        id = "2",
        name = "Mission 2",
        dateTime = "Mar 20, 2021 at 14:45",
        rocketInfo = "Falcon Heavy / FH",
        missionPatchUrl = "https://example.com/patch2.png",
        success = false,
        launchStatus = LaunchStatus.DaysSinceLaunch(50),
        launchYear = "2021",
        launchDateUnix = 1616249100L,
        wikipediaUrl = "https://wikipedia.com/mission2",
        videoUrl = null
    )

    private val availableYears = listOf("2018", "2019", "2020", "2021", "2022")

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        getLaunchesUseCase = mockk()
        getCompanyInfoUseCase = mockk()
        getAvailableYearsUseCase = mockk()

        // Setup default successful responses
        every { getCompanyInfoUseCase() } returns flowOf(Result.success(companyUiModel))
        every { getAvailableYearsUseCase() } returns flowOf(Result.success(availableYears))
        every { getLaunchesUseCase(any()) } returns flowOf(Result.success(listOf(launchUiModel1, launchUiModel2)))
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runTest {
        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)

        // Then - Initial state should show loading since init block starts data loading
        val initialState = viewModel.uiState.value
        assertThat(initialState.isLoading).isTrue()
        assertThat(initialState.company).isNull()
        assertThat(initialState.launches).isEmpty()
        assertThat(initialState.availableYears).isEmpty()
        assertThat(initialState.filterState).isEqualTo(LaunchFilterState())
        assertThat(initialState.error).isNull()
    }

    @Test
    fun `loadInitialData loads company and years successfully`() = runTest {
        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.company).isEqualTo(companyUiModel)
        assertThat(state.availableYears).isEqualTo(availableYears)
        assertThat(state.launches).isNotEmpty()
        assertThat(state.error).isNull()
    }

    @Test
    fun `loadInitialData handles company info failure`() = runTest {
        // Given
        val errorMessage = "Company info load failed"
        every { getCompanyInfoUseCase() } returns flowOf(Result.failure(Exception(errorMessage)))

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.company).isNull()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `loadInitialData handles years load failure`() = runTest {
        // Given
        val errorMessage = "Years load failed"
        every { getAvailableYearsUseCase() } returns flowOf(Result.failure(Exception(errorMessage)))

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.availableYears).isEmpty()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `loadInitialData shows loading state during execution`() = runTest {
        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        
        // Check initial loading state before advancing
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        
        advanceUntilIdle()
        
        // Then loading should be false after completion
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `loadLaunches loads launches successfully`() = runTest {
        // Given
        val launches = listOf(launchUiModel1, launchUiModel2)
        every { getLaunchesUseCase(any()) } returns flowOf(Result.success(launches))
        
        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.launches).isEqualTo(launches)
        assertThat(state.error).isNull()
    }

    @Test
    fun `loadLaunches handles failure`() = runTest {
        // Given
        val errorMessage = "Launches load failed"
        every { getLaunchesUseCase(any()) } returns flowOf(Result.failure(Exception(errorMessage)))

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.launches).isEmpty()
        assertThat(state.error).isEqualTo(errorMessage)
    }

    @Test
    fun `applyFilter updates filter state and reloads launches`() = runTest {
        // Given
        val newFilter = LaunchFilterState(
            selectedYear = "2021",
            launchSuccess = LaunchSuccessFilter.SUCCESS_ONLY,
            sortOrder = SortOrder.ASC
        )
        val filteredLaunches = listOf(launchUiModel1)
        every { getLaunchesUseCase(newFilter) } returns flowOf(Result.success(filteredLaunches))

        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // When
        viewModel.applyFilter(newFilter)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.filterState).isEqualTo(newFilter)
        assertThat(state.launches).isEqualTo(filteredLaunches)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isNull()
    }

    @Test
    fun `applyFilter shows loading state during execution`() = runTest {
        // Given
        val newFilter = LaunchFilterState(selectedYear = "2020")
        
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // When
        viewModel.applyFilter(newFilter)
        
        // Check loading state before advancing
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        
        advanceUntilIdle()
        
        // Then loading should be false after completion
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `applyFilter handles launch loading failure`() = runTest {
        // Given
        val errorMessage = "Filter application failed"
        val newFilter = LaunchFilterState(selectedYear = "2020")
        
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()
        
        // Store the launches from initial load
        val initialLaunches = viewModel.uiState.value.launches
        
        // Setup filter to fail
        every { getLaunchesUseCase(newFilter) } returns flowOf(Result.failure(Exception(errorMessage)))

        // When
        viewModel.applyFilter(newFilter)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.filterState).isEqualTo(newFilter)
        assertThat(state.isLoading).isFalse()
        assertThat(state.error).isEqualTo(errorMessage)
        // Note: ViewModel preserves previous launches on error, not clearing them
        assertThat(state.launches).isEqualTo(initialLaunches)
    }

    @Test
    fun `consecutive filter applications cancel previous jobs`() = runTest {
        // Given
        val filter1 = LaunchFilterState(selectedYear = "2020")
        val filter2 = LaunchFilterState(selectedYear = "2021")
        val launches1 = listOf(launchUiModel1)
        val launches2 = listOf(launchUiModel2)
        
        every { getLaunchesUseCase(filter1) } returns flowOf(Result.success(launches1))
        every { getLaunchesUseCase(filter2) } returns flowOf(Result.success(launches2))

        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // When - Apply filters rapidly
        viewModel.applyFilter(filter1)
        viewModel.applyFilter(filter2)
        advanceUntilIdle()

        // Then - Should only have results from the last filter
        val state = viewModel.uiState.value
        assertThat(state.filterState).isEqualTo(filter2)
        assertThat(state.launches).isEqualTo(launches2)
    }

    @Test
    fun `error state is cleared when new successful data loads`() = runTest {
        // Given
        val errorMessage = "Initial error"
        every { getCompanyInfoUseCase() } returns flowOf(Result.failure(Exception(errorMessage)))

        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Verify error state
        assertThat(viewModel.uiState.value.error).isEqualTo(errorMessage)

        // When - Apply filter with successful response
        val newFilter = LaunchFilterState(selectedYear = "2020")
        val successfulLaunches = listOf(launchUiModel1)
        every { getLaunchesUseCase(newFilter) } returns flowOf(Result.success(successfulLaunches))
        
        viewModel.applyFilter(newFilter)
        advanceUntilIdle()

        // Then - Error should be cleared
        val state = viewModel.uiState.value
        assertThat(state.error).isNull()
        assertThat(state.launches).isEqualTo(successfulLaunches)
    }

    @Test
    fun `multiple filter applications preserve company and years data`() = runTest {
        // Given
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        val originalCompany = viewModel.uiState.value.company
        val originalYears = viewModel.uiState.value.availableYears

        // When - Apply multiple filters
        viewModel.applyFilter(LaunchFilterState(selectedYear = "2020"))
        advanceUntilIdle()
        
        viewModel.applyFilter(LaunchFilterState(launchSuccess = LaunchSuccessFilter.SUCCESS_ONLY))
        advanceUntilIdle()

        // Then - Company and years should remain unchanged
        val state = viewModel.uiState.value
        assertThat(state.company).isEqualTo(originalCompany)
        assertThat(state.availableYears).isEqualTo(originalYears)
    }

    @Test
    fun `empty launches result is handled correctly`() = runTest {
        // Given
        every { getLaunchesUseCase(any()) } returns flowOf(Result.success(emptyList()))

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.isLoading).isFalse()
        assertThat(state.launches).isEmpty()
        assertThat(state.error).isNull()
    }

    @Test
    fun `default filter state is applied on initialization`() = runTest {
        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        
        // Then - Should call getLaunchesUseCase with default filter
        val expectedDefaultFilter = LaunchFilterState()
        
        // Verify the use case was called with default filter
        advanceUntilIdle()
        
        val state = viewModel.uiState.value
        assertThat(state.filterState).isEqualTo(expectedDefaultFilter)
    }

    @Test
    fun `concurrent data loading handles mixed success and failure states`() = runTest {
        // Given - Company succeeds, years fail, launches succeed
        every { getCompanyInfoUseCase() } returns flowOf(Result.success(companyUiModel))
        every { getAvailableYearsUseCase() } returns flowOf(Result.failure(Exception("Years failed")))
        every { getLaunchesUseCase(any()) } returns flowOf(Result.success(listOf(launchUiModel1)))

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then - Should show error from years failure
        val state = viewModel.uiState.value
        assertThat(state.error).isEqualTo("Years failed")
        assertThat(state.company).isNull() // Company not set due to combine() failure
        assertThat(state.availableYears).isEmpty()
    }

    @Test
    fun `loading state management is correct during operations`() = runTest {
        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        
        // Initial state should show loading
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        
        advanceUntilIdle()
        
        // After completion, loading should be false
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        
        // Apply filter - should show loading again
        viewModel.applyFilter(LaunchFilterState(selectedYear = "2020"))
        assertThat(viewModel.uiState.value.isLoading).isTrue()
        
        advanceUntilIdle()
        
        // After filter completion, loading should be false
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `filter application preserves existing company and years data`() = runTest {
        // Given
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        val existingCompany = viewModel.uiState.value.company
        val existingYears = viewModel.uiState.value.availableYears

        // When
        val newFilter = LaunchFilterState(launchSuccess = LaunchSuccessFilter.FAILED_ONLY)
        viewModel.applyFilter(newFilter)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.company).isEqualTo(existingCompany)
        assertThat(state.availableYears).isEqualTo(existingYears)
        assertThat(state.filterState).isEqualTo(newFilter)
    }

    @Test
    fun `empty results are handled correctly`() = runTest {
        // Given
        every { getCompanyInfoUseCase() } returns flowOf(Result.success(companyUiModel))
        every { getAvailableYearsUseCase() } returns flowOf(Result.success(emptyList())) // Empty list
        every { getLaunchesUseCase(any()) } returns flowOf(Result.success(emptyList())) // Empty list

        // When
        viewModel = LaunchesViewModel(getLaunchesUseCase, getCompanyInfoUseCase, getAvailableYearsUseCase, testDispatcher)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.availableYears).isEmpty()
        assertThat(state.launches).isEmpty()
        assertThat(state.company).isEqualTo(companyUiModel)
        assertThat(state.error).isNull()
    }
}