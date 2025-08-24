package com.mindera.rocketscience.data.mapper

import com.mindera.rocketscience.data.local.entity.CompanyEntity
import com.mindera.rocketscience.data.remote.dto.CompanyDto
import com.mindera.rocketscience.domain.model.Company

fun CompanyDto.toDomainModel(): Company {
    return Company(
        name = name,
        founder = founder,
        founded = founded,
        employees = employees,
        launchSites = launchSites,
        valuation = valuation
    )
}

fun CompanyDto.toEntity(): CompanyEntity {
    return CompanyEntity(
        name = name,
        founder = founder,
        founded = founded,
        employees = employees,
        launchSites = launchSites,
        valuation = valuation,
        lastUpdated = System.currentTimeMillis()
    )
}

fun CompanyEntity.toDomainModel(): Company {
    return Company(
        name = name,
        founder = founder,
        founded = founded,
        employees = employees,
        launchSites = launchSites,
        valuation = valuation
    )
}