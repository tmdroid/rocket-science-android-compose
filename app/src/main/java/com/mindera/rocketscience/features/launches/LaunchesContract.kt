package com.mindera.rocketscience.features.launches

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val company: CompanyUiModel? = null,
    val launches: List<LaunchUiModel> = emptyList(),
    val availableYears: List<String> = emptyList(),
    val filterState: LaunchFilterState = LaunchFilterState(),
    val error: String? = null
)

