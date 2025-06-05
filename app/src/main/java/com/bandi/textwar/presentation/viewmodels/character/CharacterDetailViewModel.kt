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
        object None : DeleteResult() // 초기 상태 또는 처리 완료 후 상태
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
            _error.value = null // 상세 정보 로드 시 이전 에러 상태 초기화
            // 상세 정보 로드 시 deleteResult 상태도 초기화할 수 있습니다.
            // _deleteResult.value = DeleteResult.None
            getCharacterDetailUseCase(id)
                .catch { e ->
                    _error.value = "캐릭터 상세 정보를 불러오는데 실패했습니다: ${e.message}"
                    _characterDetail.value = null // 실패 시 기존 데이터도 null 처리 고려
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
            // _deleteResult.value = DeleteResult.None // 삭제 시작 시 None으로 설정 (이미 되어있다면 생략 가능)
            try {
                val result = deleteCharacterUseCase(characterId) // UseCase가 Result<Unit> 등을 반환한다고 가정
                if (result.isSuccess) {
                    _deleteResult.value = DeleteResult.Success
                } else {
                    _deleteResult.value = DeleteResult.Error(result.exceptionOrNull()?.message ?: "캐릭터 삭제 중 알 수 없는 오류가 발생했습니다.")
                }
            } catch (e: Exception) {
                _deleteResult.value = DeleteResult.Error("캐릭터 삭제 중 예외가 발생했습니다: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * 삭제 결과 상태를 초기 상태(None)로 리셋하는 함수.
     * UI에서 삭제 성공/실패 처리가 완료된 후 호출하여,
     * 화면이 유지될 경우 의도치 않은 재처리를 방지합니다.
     */
    fun resetDeleteResultState() {
        _deleteResult.value = DeleteResult.None
    }

    /**
     * 화면이 사라질 때(ViewModel이 clear될 때) 호출될 수 있는 로직 (선택 사항)
     * HiltViewModel에서는 onCleared()를 오버라이드하여 사용합니다.
     */
    override fun onCleared() {
        super.onCleared()
        // 필요한 경우 여기서도 상태를 초기화하거나 리소스를 해제할 수 있습니다.
    }
}