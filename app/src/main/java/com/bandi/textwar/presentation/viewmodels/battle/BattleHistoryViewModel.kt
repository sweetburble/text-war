package com.bandi.textwar.presentation.viewmodels.battle

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.domain.models.BattleRecord
import com.bandi.textwar.domain.usecases.battle.GetBattleRecordsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface BattleHistoryUiState {
    object Loading : BattleHistoryUiState
    data class Success(val records: List<BattleRecord>) : BattleHistoryUiState
    data class Error(val message: String) : BattleHistoryUiState
}

@HiltViewModel
class BattleHistoryViewModel @Inject constructor(
    private val getBattleRecordsUseCase: GetBattleRecordsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<BattleHistoryUiState>(BattleHistoryUiState.Loading)
    val uiState: StateFlow<BattleHistoryUiState> = _uiState.asStateFlow()

    // 모든 전투 기록 로드 (초기 로드 등)
    init {
        loadBattleHistory() // 기본적으로 모든 전투 기록을 로드
    }

    fun loadBattleHistory(characterId: String? = null, limit: Int = 20) {
        viewModelScope.launch {
            _uiState.value = BattleHistoryUiState.Loading
            Timber.d("Loading battle history for character: $characterId")
            getBattleRecordsUseCase(characterId, limit)
                .onSuccess {
                    records ->
                    Timber.i("Battle history loaded successfully: ${records.size} records")
                    _uiState.value = BattleHistoryUiState.Success(records)
                }
                .onFailure {
                    exception ->
                    Timber.e(exception, "Error loading battle history")
                    _uiState.value = BattleHistoryUiState.Error(exception.localizedMessage ?: "알 수 없는 오류가 발생했습니다.")
                }
        }
    }

    // 특정 캐릭터의 전투 기록만 로드하는 함수 (선택 사항)
    fun loadCharacterBattleHistory(characterId: String, limit: Int = 20) {
        loadBattleHistory(characterId = characterId, limit = limit)
    }
}