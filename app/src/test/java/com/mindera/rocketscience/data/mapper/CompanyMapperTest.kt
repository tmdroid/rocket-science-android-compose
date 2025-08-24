package com.mindera.rocketscience.data.mapper

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.remote.dto.CompanyDto
import org.junit.Test

class CompanyMapperTest {

    companion object {
        private const val SPACEX_NAME = "SpaceX"
        private const val ELON_MUSK = "Elon Musk"
        private const val SPACEX_FOUNDED = 2002
        private const val SPACEX_EMPLOYEES = 12000
        private const val SPACEX_LAUNCH_SITES = 3
        private const val SPACEX_VALUATION = 180_000_000_000L

        private const val BLUE_ORIGIN_NAME = "Blue Origin"
        private const val JEFF_BEZOS = "Jeff Bezos"
        private const val BLUE_ORIGIN_FOUNDED = 2000
        private const val BLUE_ORIGIN_EMPLOYEES = 10000
        private const val BLUE_ORIGIN_LAUNCH_SITES = 1
        private const val BLUE_ORIGIN_VALUATION = 50_000_000_000L

        private const val TEST_COMPANY_NAME = "Test Company"
        private const val TEST_FOUNDER = "Test Founder"
        private const val ZERO_VALUE = 0
        private const val ZERO_LONG_VALUE = 0L

        private const val MEGA_CORP_NAME = "Mega Corp"
        private const val MEGA_FOUNDER = "Mega Founder"
        private const val MEGA_FOUNDED = 1950
        private const val MEGA_EMPLOYEES = 500000
        private const val MEGA_LAUNCH_SITES = 50
        private const val MEGA_VALUATION = 999_999_999_999L

        private const val EMPTY_STRING = ""
        private const val YEAR_2020 = 2020
        private const val ONE_VALUE = 1
        private const val ONE_MILLION = 1_000_000L

        private const val COMPANY_WITH_SYMBOLS = "Company & Co. Ltd."
        private const val FOUNDER_WITH_ACCENTS = "José María García-López"
        private const val YEAR_2015 = 2015
        private const val THOUSAND_EMPLOYEES = 1000
        private const val TWO_LAUNCH_SITES = 2
        private const val FIVE_BILLION = 5_000_000_000L
    }

    @Test
    fun `CompanyDto toDomainModel maps all fields correctly`() {
        // Given
        val companyDto = CompanyDto(
            name = SPACEX_NAME,
            founder = ELON_MUSK,
            founded = SPACEX_FOUNDED,
            employees = SPACEX_EMPLOYEES,
            launchSites = SPACEX_LAUNCH_SITES,
            valuation = SPACEX_VALUATION
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo(SPACEX_NAME)
        assertThat(domainModel.founder).isEqualTo(ELON_MUSK)
        assertThat(domainModel.founded).isEqualTo(SPACEX_FOUNDED)
        assertThat(domainModel.employees).isEqualTo(SPACEX_EMPLOYEES)
        assertThat(domainModel.launchSites).isEqualTo(SPACEX_LAUNCH_SITES)
        assertThat(domainModel.valuation).isEqualTo(SPACEX_VALUATION)
    }

    @Test
    fun `CompanyDto toDomainModel handles different company data`() {
        // Given
        val companyDto = CompanyDto(
            name = BLUE_ORIGIN_NAME,
            founder = JEFF_BEZOS,
            founded = BLUE_ORIGIN_FOUNDED,
            employees = BLUE_ORIGIN_EMPLOYEES,
            launchSites = BLUE_ORIGIN_LAUNCH_SITES,
            valuation = BLUE_ORIGIN_VALUATION
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo(BLUE_ORIGIN_NAME)
        assertThat(domainModel.founder).isEqualTo(JEFF_BEZOS)
        assertThat(domainModel.founded).isEqualTo(BLUE_ORIGIN_FOUNDED)
        assertThat(domainModel.employees).isEqualTo(BLUE_ORIGIN_EMPLOYEES)
        assertThat(domainModel.launchSites).isEqualTo(BLUE_ORIGIN_LAUNCH_SITES)
        assertThat(domainModel.valuation).isEqualTo(BLUE_ORIGIN_VALUATION)
    }

    @Test
    fun `CompanyDto toDomainModel handles zero and negative values`() {
        // Given
        val companyDto = CompanyDto(
            name = TEST_COMPANY_NAME,
            founder = TEST_FOUNDER,
            founded = ZERO_VALUE,
            employees = ZERO_VALUE,
            launchSites = ZERO_VALUE,
            valuation = ZERO_LONG_VALUE
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo(TEST_COMPANY_NAME)
        assertThat(domainModel.founder).isEqualTo(TEST_FOUNDER)
        assertThat(domainModel.founded).isEqualTo(ZERO_VALUE)
        assertThat(domainModel.employees).isEqualTo(ZERO_VALUE)
        assertThat(domainModel.launchSites).isEqualTo(ZERO_VALUE)
        assertThat(domainModel.valuation).isEqualTo(ZERO_LONG_VALUE)
    }

    @Test
    fun `CompanyDto toDomainModel handles large values correctly`() {
        // Given
        val companyDto = CompanyDto(
            name = MEGA_CORP_NAME,
            founder = MEGA_FOUNDER,
            founded = MEGA_FOUNDED,
            employees = MEGA_EMPLOYEES,
            launchSites = MEGA_LAUNCH_SITES,
            valuation = MEGA_VALUATION
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.employees).isEqualTo(MEGA_EMPLOYEES)
        assertThat(domainModel.launchSites).isEqualTo(MEGA_LAUNCH_SITES)
        assertThat(domainModel.valuation).isEqualTo(MEGA_VALUATION)
    }

    @Test
    fun `CompanyDto toDomainModel handles empty strings`() {
        // Given
        val companyDto = CompanyDto(
            name = EMPTY_STRING,
            founder = EMPTY_STRING,
            founded = YEAR_2020,
            employees = ONE_VALUE,
            launchSites = ONE_VALUE,
            valuation = ONE_MILLION
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo(EMPTY_STRING)
        assertThat(domainModel.founder).isEqualTo(EMPTY_STRING)
        assertThat(domainModel.founded).isEqualTo(YEAR_2020)
    }

    @Test
    fun `CompanyDto toDomainModel handles special characters in strings`() {
        // Given
        val companyDto = CompanyDto(
            name = COMPANY_WITH_SYMBOLS,
            founder = FOUNDER_WITH_ACCENTS,
            founded = YEAR_2015,
            employees = THOUSAND_EMPLOYEES,
            launchSites = TWO_LAUNCH_SITES,
            valuation = FIVE_BILLION
        )

        // When
        val domainModel = companyDto.toDomainModel()

        // Then
        assertThat(domainModel.name).isEqualTo(COMPANY_WITH_SYMBOLS)
        assertThat(domainModel.founder).isEqualTo(FOUNDER_WITH_ACCENTS)
    }
}