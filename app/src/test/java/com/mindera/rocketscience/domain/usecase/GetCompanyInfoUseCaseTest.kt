package com.mindera.rocketscience.domain.usecase

import com.google.common.truth.Truth.assertThat
import com.mindera.rocketscience.data.repository.CompanyRepository
import com.mindera.rocketscience.domain.model.Company
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import java.util.Locale

class GetCompanyInfoUseCaseTest {

    private lateinit var repository: CompanyRepository
    private lateinit var useCase: GetCompanyInfoUseCase

    // Test data
    private val company = Company(
        name = "SpaceX",
        founder = "Elon Musk",
        founded = 2002,
        employees = 12000,
        launchSites = 3,
        valuation = 180_000_000_000L
    )

    @Before
    fun setUp() {
        Locale.setDefault(Locale.US)
        repository = mockk()
        useCase = GetCompanyInfoUseCase(repository)
    }

    @Test
    fun `invoke returns successful company info with formatted valuation in billions`() = runTest {
        // Given
        coEvery { repository.getCompanyInfo() } returns Result.success(company)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!

        assertThat(companyUiModel.name).isEqualTo("SpaceX")
        assertThat(companyUiModel.founder).isEqualTo("Elon Musk")
        assertThat(companyUiModel.founded).isEqualTo(2002)
        assertThat(companyUiModel.employees).isEqualTo(12000)
        assertThat(companyUiModel.launchSites).isEqualTo(3)
        assertThat(companyUiModel.formattedValuation).isEqualTo("180.0 billion")
    }

    @Test
    fun `invoke formats valuation in millions correctly`() = runTest {
        // Given
        val companyWithMillions = company.copy(valuation = 850_000_000L) // 850 million
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWithMillions)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("850.0 million")
    }

    @Test
    fun `invoke formats valuation in thousands correctly`() = runTest {
        // Given
        val companyWithThousands = company.copy(valuation = 500_000L) // 500 thousand
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWithThousands)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("500.0 thousand")
    }

    @Test
    fun `invoke formats small valuation as plain number`() = runTest {
        // Given
        val companyWithSmallValue = company.copy(valuation = 999L) // Less than 1000
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWithSmallValue)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("999")
    }

    @Test
    fun `invoke formats decimal billions correctly`() = runTest {
        // Given
        val companyWithDecimalBillions = company.copy(valuation = 1_250_000_000L) // 1.25 billion
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWithDecimalBillions)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("1.3 billion") // Rounded to 1 decimal
    }

    @Test
    fun `invoke formats decimal millions correctly`() = runTest {
        // Given
        val companyWithDecimalMillions = company.copy(valuation = 37_500_000L) // 37.5 million
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWithDecimalMillions)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("37.5 million")
    }

    @Test
    fun `invoke propagates repository failure`() = runTest {
        // Given
        val errorMessage = "Network connection failed"
        coEvery { repository.getCompanyInfo() } returns Result.failure(Exception(errorMessage))

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isFailure).isTrue()
        assertThat(result.exceptionOrNull()?.message).isEqualTo(errorMessage)
    }

    @Test
    fun `invoke handles zero valuation correctly`() = runTest {
        // Given
        val companyWithZeroValuation = company.copy(valuation = 0L)
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWithZeroValuation)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("0")
    }

    @Test
    fun `invoke handles exactly 1 billion valuation correctly`() = runTest {
        // Given
        val companyWith1Billion = company.copy(valuation = 1_000_000_000L)
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWith1Billion)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("1.0 billion")
    }

    @Test
    fun `invoke handles exactly 1 million valuation correctly`() = runTest {
        // Given
        val companyWith1Million = company.copy(valuation = 1_000_000L)
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWith1Million)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("1.0 million")
    }

    @Test
    fun `invoke handles exactly 1 thousand valuation correctly`() = runTest {
        // Given
        val companyWith1Thousand = company.copy(valuation = 1_000L)
        coEvery { repository.getCompanyInfo() } returns Result.success(companyWith1Thousand)

        // When
        val result = useCase().first()

        // Then
        assertThat(result.isSuccess).isTrue()
        val companyUiModel = result.getOrNull()!!
        assertThat(companyUiModel.formattedValuation).isEqualTo("1.0 thousand")
    }
}