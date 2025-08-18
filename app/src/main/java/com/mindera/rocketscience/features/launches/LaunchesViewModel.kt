package com.mindera.rocketscience.features.launches

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LaunchesViewModel @Inject constructor() : ViewModel() {
    
    private val _uiState = MutableStateFlow(LaunchesUiState())
    val uiState: StateFlow<LaunchesUiState> = _uiState.asStateFlow()
    
    init {
        loadLaunches()
    }
    
    private fun loadLaunches() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                // TODO: Replace with actual API call
                val mockLaunches = listOf(
                    Launch("1", "Falcon Heavy", "2024-03-15", "Falcon Heavy", true),
                    Launch("2", "Starship IFT-3", "2024-03-14", "Starship", false),
                    Launch("3", "Crew Dragon", "2024-03-10", "Falcon 9", true)
                )
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    launches = mockLaunches
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
}