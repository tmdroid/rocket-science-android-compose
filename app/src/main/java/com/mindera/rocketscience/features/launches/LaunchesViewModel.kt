package com.mindera.rocketscience.features.launches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindera.rocketscience.domain.usecase.GetCompanyInfoUseCase
import com.mindera.rocketscience.domain.usecase.GetLaunchesUseCase
import com.mindera.rocketscience.di.IoDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchesViewModel @Inject constructor(
    private val getLaunchesUseCase: GetLaunchesUseCase,
    private val getCompanyInfoUseCase: GetCompanyInfoUseCase,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState.asStateFlow()
    
    init {
        loadData()
    }
    
    private fun loadData() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            // Load company info
            val companyResult = getCompanyInfoUseCase()
            when {
                companyResult.isSuccess -> {
                    _uiState.value = _uiState.value.copy(
                        company = companyResult.getOrNull()
                    )
                }
                companyResult.isFailure -> {
                    _uiState.value = _uiState.value.copy(
                        error = companyResult.exceptionOrNull()?.message ?: "Failed to load company info"
                    )
                }
            }
            
            // Load launches
            getLaunchesUseCase().collect { result ->
                when {
                    result.isSuccess -> {
                        val launches = result.getOrNull() ?: emptyList()
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            launches = launches,
                            error = null
                        )
                    }
                    result.isFailure -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.exceptionOrNull()?.message ?: "Unknown error occurred"
                        )
                    }
                }
            }
        }
    }
}