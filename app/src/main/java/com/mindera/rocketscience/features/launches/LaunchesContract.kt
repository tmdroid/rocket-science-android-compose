package com.mindera.rocketscience.features.launches

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val launches: List<LaunchUiModel> = emptyList(),
    val error: String? = null
)

