package com.mindera.rocketscience.features.launches

data class CompanyUiModel(
    val name: String,
    val founder: String,
    val founded: Int,
    val employees: Int,
    val launchSites: Int,
    val formattedValuation: String
)