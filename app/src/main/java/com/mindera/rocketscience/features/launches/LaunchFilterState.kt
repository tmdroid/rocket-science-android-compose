package com.mindera.rocketscience.features.launches

data class LaunchFilterState(
    val selectedYear: String? = null,
    val launchSuccess: LaunchSuccessFilter = LaunchSuccessFilter.ALL,
    val sortOrder: SortOrder = SortOrder.DESC
)

enum class LaunchSuccessFilter {
    ALL, SUCCESS_ONLY, FAILED_ONLY
}

enum class SortOrder {
    ASC, DESC
}