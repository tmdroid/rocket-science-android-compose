package com.mindera.rocketscience.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.remote.dto.CompanyDto
import org.junit.Test

class CompanyMapperTest {

    @Test
    fun `CompanyDto toDomainModel maps all fields correctly`() {
        // Given
        val companyDto = CompanyDto(
            name = "SpaceX",
            founder = "Elon Musk",
            founded = 2002,
            employees = 12000,
            launchSites = 3,
            valuation = 180_000_000_000L
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo("SpaceX")
        assertThat(domainModel.founder).isEqualTo("Elon Musk")
        assertThat(domainModel.founded).isEqualTo(2002)
        assertThat(domainModel.employees).isEqualTo(12000)
        assertThat(domainModel.launchSites).isEqualTo(3)
        assertThat(domainModel.valuation).isEqualTo(180_000_000_000L)
    }

    @Test
    fun `CompanyDto toDomainModel handles different company data`() {
        // Given
        val companyDto = CompanyDto(
            name = "Blue Origin",
            founder = "Jeff Bezos",
            founded = 2000,
            employees = 10000,
            launchSites = 1,
            valuation = 50_000_000_000L
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo("Blue Origin")
        assertThat(domainModel.founder).isEqualTo("Jeff Bezos")
        assertThat(domainModel.founded).isEqualTo(2000)
        assertThat(domainModel.employees).isEqualTo(10000)
        assertThat(domainModel.launchSites).isEqualTo(1)
        assertThat(domainModel.valuation).isEqualTo(50_000_000_000L)
    }

    @Test
    fun `CompanyDto toDomainModel handles zero and negative values`() {
        // Given
        val companyDto = CompanyDto(
            name = "Test Company",
            founder = "Test Founder",
            founded = 0,
            employees = 0,
            launchSites = 0,
            valuation = 0L
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo("Test Company")
        assertThat(domainModel.founder).isEqualTo("Test Founder")
        assertThat(domainModel.founded).isEqualTo(0)
        assertThat(domainModel.employees).isEqualTo(0)
        assertThat(domainModel.launchSites).isEqualTo(0)
        assertThat(domainModel.valuation).isEqualTo(0L)
    }

    @Test
    fun `CompanyDto toDomainModel handles large values correctly`() {
        // Given
        val companyDto = CompanyDto(
            name = "Mega Corp",
            founder = "Mega Founder",
            founded = 1950,
            employees = 500000,
            launchSites = 50,
            valuation = 999_999_999_999L
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.employees).isEqualTo(500000)
        assertThat(domainModel.launchSites).isEqualTo(50)
        assertThat(domainModel.valuation).isEqualTo(999_999_999_999L)
    }

    @Test
    fun `CompanyDto toDomainModel handles empty strings`() {
        // Given
        val companyDto = CompanyDto(
            name = "",
            founder = "",
            founded = 2020,
            employees = 1,
            launchSites = 1,
            valuation = 1_000_000L
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo("")
        assertThat(domainModel.founder).isEqualTo("")
        assertThat(domainModel.founded).isEqualTo(2020)
    }

    @Test
    fun `CompanyDto toDomainModel handles special characters in strings`() {
        // Given
        val companyDto = CompanyDto(
            name = "Company & Co. Ltd.",
            founder = "José María García-López",
            founded = 2015,
            employees = 1000,
            launchSites = 2,
            valuation = 5_000_000_000L
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo("Company & Co. Ltd.")
        assertThat(domainModel.founder).isEqualTo("José María García-López")
    }
}