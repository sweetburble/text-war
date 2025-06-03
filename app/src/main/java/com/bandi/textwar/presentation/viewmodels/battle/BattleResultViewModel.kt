package com.bandi.textwar.presentation.viewmodels.battle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.domain.usecases.battle.ProcessBattleUseCase
import com.bandi.textwar.data.remote.OpenAIService.BattleResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface BattleResultUiState {
    data object Loading : BattleResultUiState
    data class Success(val result: BattleResult, val myCharacterName: String) : BattleResultUiState
    data class Error(val message: String) : BattleResultUiState
}

@HiltViewModel
class BattleResultViewModel @Inject constructor(
    private val processBattleUseCase: ProcessBattleUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<BattleResultUiState>(BattleResultUiState.Loading)
    val uiState: StateFlow<BattleResultUiState> = _uiState.asStateFlow()

    // myCharacterId와 opponentId는 NavController를 통해 전달받는다
    private val myCharacterId: String = savedStateHandle.get<String>("myCharacterId") ?: ""
    private val opponentId: String = savedStateHandle.get<String>("opponentId") ?: ""

    init {
        if (myCharacterId.isNotBlank() && opponentId.isNotBlank()) {
            loadBattleResult(myCharacterId, opponentId)
        }
    }

    fun loadBattleResult(myCharId: String, oppId: String) {
        viewModelScope.launch {
            _uiState.value = BattleResultUiState.Loading
            try {
                processBattleUseCase(myCharId, oppId).collect { (battleResult, myCharacterName) ->
                    _uiState.value = BattleResultUiState.Success(battleResult, myCharacterName)
                }
            } catch (e: Exception) {
                _uiState.value = BattleResultUiState.Error(e.message ?: "알 수 없는 오류가 발생했습니다.")
            }
        }
    }
} 