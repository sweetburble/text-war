package com.bandi.textwar.presentation.viewmodels.character

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bandi.textwar.domain.usecases.character.CheckCharacterSlotAvailabilityUseCase
import com.bandi.textwar.domain.usecases.character.CreateCharacterUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

const val MAX_CHAR_DESCRIPTION_LENGTH_VM = 100

sealed interface SlotCheckState {
    object Idle : SlotCheckState
    object Loading : SlotCheckState
    data class Success(val canCreateCharacter: Boolean, val message: String? = null) : SlotCheckState
    data class Error(val message: String) : SlotCheckState
}

sealed interface SaveCharacterState {
    object Idle : SaveCharacterState
    object Loading : SaveCharacterState
    data class Success(val message: String) : SaveCharacterState
    data class Error(val message: String) : SaveCharacterState
}

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val checkCharacterSlotAvailabilityUseCase: CheckCharacterSlotAvailabilityUseCase,
    private val createCharacterUseCase: CreateCharacterUseCase
) : ViewModel() {

    private val _characterName = MutableStateFlow("")
    val characterName: StateFlow<String> = _characterName.asStateFlow()

    private val _characterDescription = MutableStateFlow("")
    val characterDescription: StateFlow<String> = _characterDescription.asStateFlow()

    private val _slotCheckState = MutableStateFlow<SlotCheckState>(SlotCheckState.Idle)
    val slotCheckState: StateFlow<SlotCheckState> = _slotCheckState.asStateFlow()

    private val _saveCharacterState = MutableStateFlow<SaveCharacterState>(SaveCharacterState.Idle)
    val saveCharacterState: StateFlow<SaveCharacterState> = _saveCharacterState.asStateFlow()

    init {
        checkCharacterSlotAvailability()
    }

    private fun checkCharacterSlotAvailability() {
        viewModelScope.launch {
            _slotCheckState.value = SlotCheckState.Loading
            checkCharacterSlotAvailabilityUseCase()
                .catch { e ->
                    Timber.e(e, "Slot check failed with exception")
                    _slotCheckState.value = SlotCheckState.Error("슬롯 확인 중 오류: ${e.message}")
                }
                .collect { result ->
                    _slotCheckState.value = if (result.canCreateCharacter) {
                        SlotCheckState.Success(true, result.message)
                    } else {
                        SlotCheckState.Success(false, result.message)
                    }
                }
        }
    }

    val remainingChars: StateFlow<Int> = characterDescription
        .map { MAX_CHAR_DESCRIPTION_LENGTH_VM - it.length }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MAX_CHAR_DESCRIPTION_LENGTH_VM
        )

    val isSaveButtonEnabled: StateFlow<Boolean> = combine(
        characterName, characterDescription, slotCheckState, saveCharacterState
    ) { name, description, slotState, saveState ->
        val baseChecks = name.isNotBlank() && description.isNotBlank() && description.length <= MAX_CHAR_DESCRIPTION_LENGTH_VM
        val canCreateFromSlot = if (slotState is SlotCheckState.Success) slotState.canCreateCharacter else false
        val isNotCurrentlySaving = saveState !is SaveCharacterState.Loading
        baseChecks && canCreateFromSlot && isNotCurrentlySaving
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onCharacterNameChange(newName: String) {
        _characterName.value = newName
        _saveCharacterState.value = SaveCharacterState.Idle
    }

    fun onCharacterDescriptionChange(newDescription: String) {
        if (newDescription.length <= MAX_CHAR_DESCRIPTION_LENGTH_VM) {
            _characterDescription.value = newDescription
        }
        _saveCharacterState.value = SaveCharacterState.Idle
    }

    fun saveCharacter() {
        val currentSlotCheck = slotCheckState.value
        if (currentSlotCheck is SlotCheckState.Success && currentSlotCheck.canCreateCharacter) {
            viewModelScope.launch {
                _saveCharacterState.value = SaveCharacterState.Loading
                createCharacterUseCase(characterName.value, characterDescription.value)
                    .catch { e ->
                        Timber.e(e, "Save character failed with exception")
                        _saveCharacterState.value = SaveCharacterState.Error("캐릭터 저장 중 오류: ${e.message}")
                    }
                    .collect { createdCharacter ->
                        _saveCharacterState.value = SaveCharacterState.Success("'${createdCharacter.characterName}'를 성공적으로 생성했습니다!")
                        _characterName.value = ""
                        _characterDescription.value = ""
                        checkCharacterSlotAvailability()
                    }
            }
        } else {
            val errorMsg = when (currentSlotCheck) {
                is SlotCheckState.Error -> currentSlotCheck.message
                is SlotCheckState.Success -> "캐릭터 슬롯이 부족합니다."
                else -> "캐릭터를 저장할 수 없습니다. 슬롯 확인을 진행해주세요."
            }
            _saveCharacterState.value = SaveCharacterState.Error(errorMsg)
        }
    }

    fun refreshSlotCheck() {
        checkCharacterSlotAvailability()
    }
} 