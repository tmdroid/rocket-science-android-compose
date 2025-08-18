package com.mindera.rocketscience.features.launches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mindera.rocketscience.data.repository.LaunchesRepository
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
    private val launchesRepository: LaunchesRepository,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState.asStateFlow()
    
    init {
        loadLaunches()
    }
    
    private fun loadLaunches() {
        viewModelScope.launch(ioDispatcher) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            launchesRepository.getLaunches().collect { result ->
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