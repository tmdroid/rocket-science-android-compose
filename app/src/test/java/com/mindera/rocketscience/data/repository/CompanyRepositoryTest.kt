package com.mindera.rocketscience.data.repository

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.local.datasource.LocalDataSource
import com.mindera.rocketscience.data.local.entity.CompanyEntity
import com.mindera.rocketscience.data.remote.datasource.CompanyRemoteDataSource
import com.mindera.rocketscience.data.remote.dto.CompanyDto
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
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
    private lateinit var localDataSource: LocalDataSource
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
        localDataSource = mockk(relaxed = true)
        repository = CompanyRepositoryImpl(remoteDataSource, localDataSource)
    }

    @Test
    fun `getCompanyInfo returns cached data when available and not stale`() = runTest {
        // Given
        val cachedEntity = CompanyEntity(
            name = SPACEX_NAME,
            founder = ELON_MUSK,
            founded = SPACEX_FOUNDED,
            employees = SPACEX_EMPLOYEES,
            launchSites = SPACEX_LAUNCH_SITES,
            valuation = SPACEX_VALUATION
        )
        coEvery { localDataSource.getCompanyInfoSync() } returns cachedEntity
        coEvery { localDataSource.isCompanyDataStale() } returns false

        // When
        val result = repository.getCompanyInfo().first()

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
    fun `getCompanyInfo fetches from remote when no cache exists`() = runTest {
        // Given
        coEvery { localDataSource.getCompanyInfoSync() } returns null
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(companyDto)

        // When
        val results = repository.getCompanyInfo().toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        
        assertThat(company.name).isEqualTo(SPACEX_NAME)
        assertThat(company.valuation).isEqualTo(SPACEX_VALUATION)
    }

    @Test
    fun `getCompanyInfo refreshes cache when data is stale`() = runTest {
        // Given
        val staleCachedEntity = CompanyEntity(
            name = "Old Company",
            founder = "Old Founder",
            founded = 2000,
            employees = 1000,
            launchSites = 1,
            valuation = 1_000_000_000L
        )
        coEvery { localDataSource.getCompanyInfoSync() } returns staleCachedEntity
        coEvery { localDataSource.isCompanyDataStale() } returns true
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.success(companyDto)

        // When
        val results = repository.getCompanyInfo().toList()

        // Then
        assertThat(results).hasSize(2) // First cached, then fresh data
        
        // First emission should be cached data
        val cachedResult = results[0]
        assertThat(cachedResult.isSuccess).isTrue()
        assertThat(cachedResult.getOrNull()!!.name).isEqualTo("Old Company")
        
        // Second emission should be fresh data
        val freshResult = results[1]
        assertThat(freshResult.isSuccess).isTrue()
        assertThat(freshResult.getOrNull()!!.name).isEqualTo(SPACEX_NAME)
    }

    @Test
    fun `getCompanyInfo keeps cached data when remote fails`() = runTest {
        // Given
        val cachedEntity = CompanyEntity(
            name = SPACEX_NAME,
            founder = ELON_MUSK,
            founded = SPACEX_FOUNDED,
            employees = SPACEX_EMPLOYEES,
            launchSites = SPACEX_LAUNCH_SITES,
            valuation = SPACEX_VALUATION
        )
        coEvery { localDataSource.getCompanyInfoSync() } returns cachedEntity
        coEvery { localDataSource.isCompanyDataStale() } returns true
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.failure(Exception(NETWORK_ERROR))

        // When
        val results = repository.getCompanyInfo().toList()

        // Then
        assertThat(results).hasSize(1) // Only cached data, no error emission
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        assertThat(result.getOrNull()!!.name).isEqualTo(SPACEX_NAME)
    }
    
    @Test
    fun `getCompanyInfo propagates error when no cache exists and remote fails`() = runTest {
        // Given
        coEvery { localDataSource.getCompanyInfoSync() } returns null
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.failure(Exception(NETWORK_ERROR))

        // When
        val results = repository.getCompanyInfo().toList()

        // Then
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(NETWORK_ERROR)
    }

    @Test
    fun `getCompanyInfo keeps stale data when network exception occurs`() = runTest {
        // Given - stale cached data exists
        val staleEntity = CompanyEntity(
            name = BLUE_ORIGIN_NAME,
            founder = JEFF_BEZOS,
            founded = BLUE_ORIGIN_FOUNDED,
            employees = BLUE_ORIGIN_EMPLOYEES,
            launchSites = BLUE_ORIGIN_LAUNCH_SITES,
            valuation = BLUE_ORIGIN_VALUATION
        )
        coEvery { localDataSource.getCompanyInfoSync() } returns staleEntity
        coEvery { localDataSource.isCompanyDataStale() } returns true
        coEvery { remoteDataSource.getCompanyInfo() } throws RuntimeException("Network timeout")

        // When
        val results = repository.getCompanyInfo().toList()

        // Then - should only emit cached data, no error
        assertThat(results).hasSize(1) 
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        assertThat(company.name).isEqualTo(BLUE_ORIGIN_NAME)
        assertThat(company.founder).isEqualTo(JEFF_BEZOS)
        assertThat(company.valuation).isEqualTo(BLUE_ORIGIN_VALUATION)
    }

    @Test
    fun `getCompanyInfo keeps very old cached data when remote is unavailable`() = runTest {
        // Given - cached data that's weeks old but still available
        val veryOldEntity = CompanyEntity(
            name = TEST_COMPANY_NAME,
            founder = TEST_FOUNDER,
            founded = ZERO_VALUE,
            employees = ZERO_VALUE,
            launchSites = ZERO_VALUE,
            valuation = ZERO_LONG_VALUE,
            lastUpdated = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L) // 30 days ago
        )
        coEvery { localDataSource.getCompanyInfoSync() } returns veryOldEntity
        coEvery { localDataSource.isCompanyDataStale() } returns true
        coEvery { remoteDataSource.getCompanyInfo() } returns Result.failure(Exception("Service unavailable"))

        // When
        val results = repository.getCompanyInfo().toList()

        // Then - should still show the very old cached data
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        assertThat(company.name).isEqualTo(TEST_COMPANY_NAME)
        assertThat(company.founder).isEqualTo(TEST_FOUNDER)
    }

    @Test
    fun `getCompanyInfo handles IOException while preserving cached data`() = runTest {
        // Given
        val cachedEntity = CompanyEntity(
            name = SPACEX_NAME,
            founder = ELON_MUSK,
            founded = SPACEX_FOUNDED,
            employees = SPACEX_EMPLOYEES,
            launchSites = SPACEX_LAUNCH_SITES,
            valuation = SPACEX_VALUATION
        )
        coEvery { localDataSource.getCompanyInfoSync() } returns cachedEntity
        coEvery { localDataSource.isCompanyDataStale() } returns true
        coEvery { remoteDataSource.getCompanyInfo() } throws java.io.IOException("No internet connection")

        // When
        val results = repository.getCompanyInfo().toList()

        // Then - should preserve cached data despite IOException
        assertThat(results).hasSize(1)
        val result = results[0]
        assertThat(result.isSuccess).isTrue()
        val company = result.getOrNull()!!
        assertThat(company.name).isEqualTo(SPACEX_NAME)
    }
}