package com.bandi.textwar.presentation.viewmodels.character

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.domain.usecases.GetCharacterDetailUseCase
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
    private val savedStateHandle: SavedStateHandle // NavArgs를 통해 characterId를 받기 위함
) : ViewModel() {

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
} 