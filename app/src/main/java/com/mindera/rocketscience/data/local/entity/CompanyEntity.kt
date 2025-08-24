package com.mindera.rocketscience.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "company")
data class CompanyEntity(
    @PrimaryKey
    val id: Int = 1, // Single company record, always use ID 1
    val name: String,
    val founder: String,
    val founded: Int,
    val employees: Int,
    val launchSites: Int,
    val valuation: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)