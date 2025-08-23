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

    private lateinit var remoteDataSource: CompanyRemoteDataSource
    private lateinit var repository: CompanyRepository

    // Test data
    private val companyDto = CompanyDto(
        name = "SpaceX",
        founder = "Elon Musk",
        founded = 2002,
        employees = 12000,
        launchSites = 3,
        valuation = 180_000_000_000L
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
        
        assertThat(company.name).isEqualTo("SpaceX")
        assertThat(company.founder).isEqualTo("Elon Musk")
        assertThat(company.founded).isEqualTo(2002)
        assertThat(company.employees).isEqualTo(12000)
        assertThat(company.launchSites).isEqualTo(3)
        assertThat(company.valuation).isEqualTo(180_000_000_000L)
    }

    @Test
    fun `getCompanyInfo propagates remote data source failure`() = runTest {
        // Given
        val errorMessage = "Network connection failed"
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
            name = "Blue Origin",
            founder = "Jeff Bezos",
            founded = 2000,
            employees = 10000,
            launchSites = 1,
            valuation = 50_000_000_000L
        )
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(differentCompany)

        // When
        val result = repository.getCompanyInfo()

        // Then
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        
        assertThat(company.name).isEqualTo("Blue Origin")
        assertThat(company.founder).isEqualTo("Jeff Bezos")
        assertThat(company.founded).isEqualTo(2000)
        assertThat(company.employees).isEqualTo(10000)
        assertThat(company.launchSites).isEqualTo(1)
        assertThat(company.valuation).isEqualTo(50_000_000_000L)
    }

    @Test
    fun `getCompanyInfo handles zero and negative values correctly`() = runTest {
        // Given
        val companyWithZeros = CompanyDto(
            name = "Test Company",
            founder = "Test Founder",
            founded = 0,
            employees = 0,
            launchSites = 0,
            valuation = 0L
        )
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(companyWithZeros)

        // When
        val result = repository.getCompanyInfo()

        // Then
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        
        assertThat(company.founded).isEqualTo(0)
        assertThat(company.employees).isEqualTo(0)
        assertThat(company.launchSites).isEqualTo(0)
        assertThat(company.valuation).isEqualTo(0L)
    }
}