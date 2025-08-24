package com.mindera.rocketscience.domain.usecase

import com.mindera.rocketscience.data.repository.CompanyRepository
import com.mindera.rocketscience.domain.model.Company
import com.mindera.rocketscience.features.launches.CompanyUiModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetCompanyInfoUseCase @Inject constructor(
    private val companyRepository: CompanyRepository
) {

    operator fun invoke(): Flow<Result<CompanyUiModel>> =
        companyRepository.getCompanyInfo()
            .map { result ->
                result.map { company -> company.toUiModel() }
            }

    private fun Company.toUiModel(): CompanyUiModel {
        val formattedValuation = formatValuation(valuation)

        return CompanyUiModel(
            name = name,
            founder = founder,
            founded = founded,
            employees = employees,
            launchSites = launchSites,
            formattedValuation = formattedValuation
        )
    }

    private fun formatValuation(value: Long): String {
        return when {
            value >= 1_000_000_000 -> {
                val billions = value / 1_000_000_000.0
                String.format("%.1f billion", billions)
            }

            value >= 1_000_000 -> {
                val millions = value / 1_000_000.0
                String.format("%.1f million", millions)
            }

            value >= 1_000 -> {
                val thousands = value / 1_000.0
                String.format("%.1f thousand", thousands)
            }

            else -> value.toString()
        }
    }
}