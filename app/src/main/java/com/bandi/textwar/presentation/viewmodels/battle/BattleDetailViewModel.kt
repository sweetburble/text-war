package com.bandi.textwar.presentation.viewmodels.battle

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.domain.models.BattleRecord
import com.bandi.textwar.domain.usecases.battle.GetBattleRecordUseCase
import com.bandi.textwar.ui.navigation.BattleDetailNav // 네비게이션 인자 이름을 위해 import
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed interface BattleDetailUiState {
    object Loading : BattleDetailUiState
    data class Success(val record: BattleRecord) : BattleDetailUiState
    data class Error(val message: String) : BattleDetailUiState
    object NotFound : BattleDetailUiState // 기록을 찾지 못한 경우
}

@HiltViewModel
class BattleDetailViewModel @Inject constructor(
    private val getBattleRecordUseCase: GetBattleRecordUseCase,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow<BattleDetailUiState>(BattleDetailUiState.Loading)
    val uiState: StateFlow<BattleDetailUiState> = _uiState.asStateFlow()

    private val recordId: String? = savedStateHandle[BattleDetailNav.argName] // 네비게이션 정의에서 인자 이름 가져오기

    init {
        loadBattleDetail()
    }

    fun loadBattleDetail() {
        if (recordId == null) {
            Timber.e("Record ID is null, cannot load detail.")
            _uiState.value = BattleDetailUiState.Error("전투 기록 ID가 없습니다.")
            return
        }

        viewModelScope.launch {
            _uiState.value = BattleDetailUiState.Loading
            Timber.d("Loading battle detail for record ID: $recordId")
            getBattleRecordUseCase(recordId)
                .onSuccess { record ->
                    if (record != null) {
                        Timber.i("Battle detail loaded successfully for record ID: $recordId")
                        _uiState.value = BattleDetailUiState.Success(record)
                    } else {
                        Timber.w("Battle record not found for ID: $recordId")
                        _uiState.value = BattleDetailUiState.NotFound
                    }
                }
                .onFailure { exception ->
                    Timber.e(exception, "Error loading battle detail for record ID: $recordId")
                    _uiState.value = BattleDetailUiState.Error(exception.localizedMessage ?: "전투 기록을 불러오는 중 오류가 발생했습니다.")
                }
        }
    }
} 