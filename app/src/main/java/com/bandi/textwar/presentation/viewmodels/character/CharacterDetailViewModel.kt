package com.bandi.textwar.presentation.viewmodels.character

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.domain.usecases.character.GetCharacterDetailUseCase
import com.bandi.textwar.domain.usecases.character.DeleteCharacterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CharacterDetailViewModel @Inject constructor(
    private val getCharacterDetailUseCase: GetCharacterDetailUseCase,
    private val deleteCharacterUseCase: DeleteCharacterUseCase,
    private val savedStateHandle: SavedStateHandle // NavArgs를 통해 characterId를 받기 위함
) : ViewModel() {

    /**
     * 삭제 결과를 나타내는 sealed class
     */
    sealed class DeleteResult {
        object Success : DeleteResult()
        data class Error(val message: String) : DeleteResult()
        object None : DeleteResult() // 초기 상태
    }

    private val _deleteResult = MutableStateFlow<DeleteResult>(DeleteResult.None)
    val deleteResult: StateFlow<DeleteResult> = _deleteResult.asStateFlow()

    private val _characterDetail = MutableStateFlow<CharacterDetail?>(null)
    val characterDetail: StateFlow<CharacterDetail?> = _characterDetail.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val characterId: String? = savedStateHandle.get<String>("characterId")

    init {
        characterId?.let {
            loadCharacterDetail(it)
        } ?: run {
            _error.value = "캐릭터 ID가 없습니다."
        }
    }

    private fun loadCharacterDetail(id: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            getCharacterDetailUseCase(id)
                .catch { e ->
                    _error.value = "캐릭터 상세 정보를 불러오는데 실패했습니다: ${e.message}"
                }
                .collect { detail ->
                    _characterDetail.value = detail
                }
            _isLoading.value = false
        }
    }

    /**
     * 캐릭터 삭제 요청 함수
     * @param characterId 삭제할 캐릭터의 ID (기본값: 현재 화면의 캐릭터)
     * 삭제 성공 시 DeleteResult.Success, 실패 시 DeleteResult.Error 상태로 변경
     */
    fun deleteCharacter(characterId: String? = this.characterId) {
        if (characterId == null) {
            _deleteResult.value = DeleteResult.Error("캐릭터 ID가 없습니다.")
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _deleteResult.value = DeleteResult.None
            val result = deleteCharacterUseCase(characterId)
            _isLoading.value = false
            _deleteResult.value = if (result.isSuccess) {
                DeleteResult.Success
            } else {
                DeleteResult.Error(result.exceptionOrNull()?.message ?: "알 수 없는 오류")
            }
        }
    }
}