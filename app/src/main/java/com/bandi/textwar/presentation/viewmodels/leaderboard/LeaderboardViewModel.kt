package com.bandi.textwar.presentation.viewmodels.leaderboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.domain.usecases.leaderboard.GetLeaderboardDataUseCase
import com.bandi.textwar.data.models.LeaderboardItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class LeaderboardUiState {
    object Loading : LeaderboardUiState()
    data class Success(val leaderboardData: List<LeaderboardItem>) : LeaderboardUiState()
    data class Error(val message: String) : LeaderboardUiState()
}

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val getLeaderboardDataUseCase: GetLeaderboardDataUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<LeaderboardUiState>(LeaderboardUiState.Loading)
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        fetchLeaderboardData()
    }

    fun fetchLeaderboardData() {
        viewModelScope.launch {
            _uiState.value = LeaderboardUiState.Loading
            try {
                val result = getLeaderboardDataUseCase()
                result.onSuccess {
                    _uiState.value = LeaderboardUiState.Success(it)
                }.onFailure {
                    _uiState.value = LeaderboardUiState.Error(it.message ?: "알 수 없는 오류가 발생했습니다.")
                }
            } catch (e: Exception) {
                _uiState.value = LeaderboardUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }
} 