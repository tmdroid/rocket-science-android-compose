package com.mindera.rocketscience.domain.model

/**
 * Domain entity representing SpaceX company information
 */
data class Company(
    val name: String,
    val founder: String,
    val founded: Int,
    val employees: Int,
    val launchSites: Int,
    val valuation: Long
)