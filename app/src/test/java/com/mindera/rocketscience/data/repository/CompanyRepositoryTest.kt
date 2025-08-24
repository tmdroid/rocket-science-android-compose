package com.mindera.rocketscience.data.repository

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.remote.datasource.CompanyRemoteDataSource
import com.mindera.rocketscience.data.remote.dto.CompanyDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CompanyRepositoryTest {

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
        
        private const val NETWORK_ERROR = "Network connection failed"
    }

    private lateinit var remoteDataSource: CompanyRemoteDataSource
    private lateinit var repository: CompanyRepository

    // Test data
    private val companyDto = CompanyDto(
        name = SPACEX_NAME,
        founder = ELON_MUSK,
        founded = SPACEX_FOUNDED,
        employees = SPACEX_EMPLOYEES,
        launchSites = SPACEX_LAUNCH_SITES,
        valuation = SPACEX_VALUATION
    )

    @Before
    fun setUp() {
        remoteDataSource = mockk()
        repository = CompanyRepositoryImpl(remoteDataSource)
    }

    @Test
    fun `getCompanyInfo returns mapped domain model on success`() = runTest {
        // Given
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(companyDto)

        // When
        val result = repository.getCompanyInfo()

        // Then
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        
        assertThat(company.name).isEqualTo(SPACEX_NAME)
        assertThat(company.founder).isEqualTo(ELON_MUSK)
        assertThat(company.founded).isEqualTo(SPACEX_FOUNDED)
        assertThat(company.employees).isEqualTo(SPACEX_EMPLOYEES)
        assertThat(company.launchSites).isEqualTo(SPACEX_LAUNCH_SITES)
        assertThat(company.valuation).isEqualTo(SPACEX_VALUATION)
    }

    @Test
    fun `getCompanyInfo propagates remote data source failure`() = runTest {
        // Given
        val errorMessage = NETWORK_ERROR
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.failure(Exception(errorMessage))

        // When
        val result = repository.getCompanyInfo()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `getCompanyInfo handles mapping of different company data`() = runTest {
        // Given
        val differentCompany = CompanyDto(
            name = BLUE_ORIGIN_NAME,
            founder = JEFF_BEZOS,
            founded = BLUE_ORIGIN_FOUNDED,
            employees = BLUE_ORIGIN_EMPLOYEES,
            launchSites = BLUE_ORIGIN_LAUNCH_SITES,
            valuation = BLUE_ORIGIN_VALUATION
        )
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(differentCompany)

        // When
        val result = repository.getCompanyInfo()

        // Then
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        
        assertThat(company.name).isEqualTo(BLUE_ORIGIN_NAME)
        assertThat(company.founder).isEqualTo(JEFF_BEZOS)
        assertThat(company.founded).isEqualTo(BLUE_ORIGIN_FOUNDED)
        assertThat(company.employees).isEqualTo(BLUE_ORIGIN_EMPLOYEES)
        assertThat(company.launchSites).isEqualTo(BLUE_ORIGIN_LAUNCH_SITES)
        assertThat(company.valuation).isEqualTo(BLUE_ORIGIN_VALUATION)
    }

    @Test
    fun `getCompanyInfo handles zero and negative values correctly`() = runTest {
        // Given
        val companyWithZeros = CompanyDto(
            name = TEST_COMPANY_NAME,
            founder = TEST_FOUNDER,
            founded = ZERO_VALUE,
            employees = ZERO_VALUE,
            launchSites = ZERO_VALUE,
            valuation = ZERO_LONG_VALUE
        )
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(companyWithZeros)

        // When
        val result = repository.getCompanyInfo()

        // Then
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        
        assertThat(company.founded).isEqualTo(ZERO_VALUE)
        assertThat(company.employees).isEqualTo(ZERO_VALUE)
        assertThat(company.launchSites).isEqualTo(ZERO_VALUE)
        assertThat(company.valuation).isEqualTo(ZERO_LONG_VALUE)
    }
}