package com.mindera.rocketscience.domain.model

/**
 * Represents the temporal status of a launch relative to the current time
 * Business logic concern - no UI strings here
 */
sealed class LaunchStatus {
    data class DaysSinceLaunch(val days: Int) : LaunchStatus()
    data class DaysUntilLaunch(val days: Int) : LaunchStatus() 
    object LaunchingToday : LaunchStatus()
}