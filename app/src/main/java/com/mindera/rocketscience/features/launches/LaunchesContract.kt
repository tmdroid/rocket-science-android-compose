package com.mindera.rocketscience.features.launches

data class LaunchesUiState(
    val isLoading: Boolean = false,
    val launches: List<Launch> = emptyList(),
    val error: String? = null
)

data class Launch(
    val id: String,
    val name: String,
    val date: String,
    val rocket: String,
    val success: Boolean?
)