package com.bandi.textwar.presentation.viewmodels.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.domain.usecases.GetCharacterListUseCase
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
    private val charactersUseCase: GetCharacterListUseCase
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
}