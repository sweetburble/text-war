package com.bandi.textwar.presentation.viewmodels.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.domain.usecases.GetCharacterListUseCase
import com.bandi.textwar.domain.usecases.GetRandomOpponentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * CharacterListScreen을 위한 ViewModel
 */
@HiltViewModel
class CharacterListViewModel @Inject constructor(
    private val charactersUseCase: GetCharacterListUseCase,
    private val getRandomOpponentUseCase: GetRandomOpponentUseCase
) : ViewModel() {

    // UI 상태: 캐릭터 목록
    private val _characters = MutableStateFlow<List<CharacterSummary>>(emptyList())
    val characters: StateFlow<List<CharacterSummary>> = _characters.asStateFlow()

    // 로딩 상태
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 오류 상태
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // 상대 캐릭터 상태
    private val _opponentCharacter = MutableStateFlow<CharacterDetail?>(null)
    val opponentCharacter: StateFlow<CharacterDetail?> = _opponentCharacter.asStateFlow()

    // 상대 캐릭터 검색 중 로딩 상태
    private val _isFindingOpponent = MutableStateFlow(false)
    val isFindingOpponent: StateFlow<Boolean> = _isFindingOpponent.asStateFlow()

    init {
        loadCharacters()
    }

    fun loadCharacters() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            charactersUseCase()
                .catch { e ->
                    _error.value = "캐릭터 목록을 불러오는데 실패했습니다: ${e.message}"
                    // _characters.value = emptyList() // 오류 발생 시 목록을 비울 수도 있음
                }
                .collect { characterList ->
                    _characters.value = characterList
                }
            _isLoading.value = false // collect가 완료되거나 catch 블록 실행 후 로딩 종료
        }
    }

    /**
     * 전투 상대를 찾습니다.
     * @param playerCharacterId 현재 플레이어의 캐릭터 ID - UseCase에서 더 이상 사용하지 않음
     */
    fun findOpponent(/* playerCharacterId: String */) {
        viewModelScope.launch {
            _isFindingOpponent.value = true
            _error.value = null
            _opponentCharacter.value = null

            getRandomOpponentUseCase()
                .onSuccess {
                    _opponentCharacter.value = it
                }
                .onFailure {
                    _error.value = "상대 찾기 실패: ${it.message}"
                }
            _isFindingOpponent.value = false
        }
    }

    /**
     * 검색된 상대 캐릭터 정보를 초기화합니다.
     * 화면 이동 후 호출하여 이전 상대 정보가 남아있지 않도록 합니다.
     */
    fun clearOpponent() {
        _opponentCharacter.value = null
    }
}