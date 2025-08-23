package com.mindera.rocketscience.features.launches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindera.rocketscience.di.IoDispatcher
import com.mindera.rocketscience.domain.usecase.GetAvailableYearsUseCase
import com.mindera.rocketscience.domain.usecase.GetCompanyInfoUseCase
import com.mindera.rocketscience.domain.usecase.GetLaunchesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.plus
import javax.inject.Inject

@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val getLaunchesUseCase: GetLaunchesUseCase,
    private val getCompanyInfoUseCase: GetCompanyInfoUseCase,
    private val getAvailableYearsUseCase: GetAvailableYearsUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {

    private val _uiState = MutableStateFlow(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState.asStateFlow()

    private var initialDataJob: Job? = null
    private var launchesDataJob: Job? = null

    init {
        loadInitialData()
        loadLaunches(_uiState.value.filterState)
    }

    fun applyFilter(filterState: LaunchFilterState) {
        _uiState.value = _uiState.value.copy(filterState = filterState)
        loadLaunches(filterState)
    }

    private fun loadInitialData() {
        initialDataJob?.cancel()

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        initialDataJob = combine(
            getCompanyInfoUseCase(),
            getAvailableYearsUseCase()
        ) { companyResult, yearsResult ->
            when {
                companyResult.isFailure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = companyResult.exceptionOrNull()?.message
                            ?: "Failed to load company info"
                    )
                }

                yearsResult.isFailure -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = yearsResult.exceptionOrNull()?.message ?: "Failed to load years"
                    )
                }

                else -> {
                    val company = companyResult.getOrNull()
                    val years = yearsResult.getOrNull() ?: emptyList()

                    _uiState.value = _uiState.value.copy(
                        company = company,
                        availableYears = years
                    )
                }
            }
        }.launchIn(viewModelScope + ioDispatcher)
    }

    private fun loadLaunches(filterState: LaunchFilterState) {
        launchesDataJob?.cancel()

        _uiState.value = _uiState.value.copy(isLoading = true, error = null)

        launchesDataJob = getLaunchesUseCase(filterState)
            .onEach { result ->
                when {
                    result.isFailure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Failed to load launches"
                        )
                    }

                    else -> {
                        val launches = result.getOrNull() ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            launches = launches,
                            error = null
                        )
                    }
                }
            }.launchIn(viewModelScope + ioDispatcher)
    }
}