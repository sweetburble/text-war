package com.bandi.textwar.presentation.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import timber.log.Timber
import javax.inject.Inject

const val MAX_CHAR_DESCRIPTION_LENGTH_VM = 100

// For users table data (simplified)
@Serializable
data class UserProfile(val id: String, val character_slots: Int? = null)

@Serializable
data class CharacterInsert( // Data to be inserted into 'characters' table
    val user_id: String,
    val character_name: String, // User updated this field name
    val description: String
    // Add other default fields if necessary, e.g., level: Int = 1, health: Int = 100
)

sealed class SlotCheckState {
    object Idle : SlotCheckState()
    object Loading : SlotCheckState()
    data class Success(val canCreateCharacter: Boolean, val message: String? = null) : SlotCheckState()
    data class Error(val message: String) : SlotCheckState()
}

sealed class SaveCharacterState {
    object Idle : SaveCharacterState()
    object Loading : SaveCharacterState()
    data class Success(val message: String) : SaveCharacterState()
    data class Error(val message: String) : SaveCharacterState()
}

@HiltViewModel
class CharacterCreationViewModel @Inject constructor(
    private val supabaseClient: SupabaseClient
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
            val userId = supabaseClient.auth.currentUserOrNull()?.id

            if (userId == null) {
                _slotCheckState.value = SlotCheckState.Error("User not authenticated.")
                return@launch
            }

            try {
                val userProfile = supabaseClient.postgrest.from("users")
                    .select { filter { eq("id", userId) } }
                    .decodeSingleOrNull<UserProfile>()

                val maxSlots = userProfile?.character_slots ?: 0

                // User updated count query
                val characterCount = supabaseClient.postgrest.from("characters")
                    .select { filter { eq("user_id", userId) } }
                    .decodeList<JsonElement>().size

                Timber.d("characterCount: $characterCount")


                if (characterCount < maxSlots) {
                    _slotCheckState.value = SlotCheckState.Success(true, "남은 캐릭터 슬롯 ($characterCount/$maxSlots)")
                } else {
                    _slotCheckState.value = SlotCheckState.Success(false, "캐릭터 슬롯을 전부 사용했습니다 : ($characterCount/$maxSlots)")
                }

            } catch (e: Exception) {
                _slotCheckState.value = SlotCheckState.Error("Failed to check character slots: ${e.message}")
                e.printStackTrace() // For debugging
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
        characterName, characterDescription, slotCheckState, saveCharacterState // Also consider save state
    ) { name, description, slotState, saveState ->
        val baseChecks = name.isNotBlank() && description.isNotBlank() && description.length <= MAX_CHAR_DESCRIPTION_LENGTH_VM
        val canCreateFromSlot = if (slotState is SlotCheckState.Success) slotState.canCreateCharacter else false
        val isNotCurrentlySaving = saveState !is SaveCharacterState.Loading // Prevent multiple clicks while saving
        baseChecks && canCreateFromSlot && isNotCurrentlySaving
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    fun onCharacterNameChange(newName: String) {
        _characterName.value = newName
        _saveCharacterState.value = SaveCharacterState.Idle // Reset save state on input change
    }

    fun onCharacterDescriptionChange(newDescription: String) {
        if (newDescription.length <= MAX_CHAR_DESCRIPTION_LENGTH_VM) {
            _characterDescription.value = newDescription
        }
        _saveCharacterState.value = SaveCharacterState.Idle // Reset save state on input change
    }

    fun saveCharacter() {
        val currentSlotCheck = slotCheckState.value
        if (currentSlotCheck is SlotCheckState.Success && currentSlotCheck.canCreateCharacter) {
            viewModelScope.launch {
                _saveCharacterState.value = SaveCharacterState.Loading
                try {
                    val userId = supabaseClient.auth.currentUserOrNull()?.id
                    if (userId == null) {
                        _saveCharacterState.value = SaveCharacterState.Error("User not authenticated for saving.")
                        return@launch
                    }

                    val characterData = CharacterInsert( // Using the data class
                        user_id = userId,
                        character_name = characterName.value,
                        description = characterDescription.value
                    )
                    
                    supabaseClient.postgrest.from("characters").insert(characterData)
                    _saveCharacterState.value = SaveCharacterState.Success("'${characterName.value}'를 성공적으로 생성했습니다!")
                    // Optionally reset fields after successful save
                    // _characterName.value = ""
                    // _characterDescription.value = ""
                    // checkCharacterSlotAvailability() // Re-check slots after saving
                } catch (e: Exception) {
                    _saveCharacterState.value = SaveCharacterState.Error("Failed to save character: ${e.message}")
                    e.printStackTrace() // For debugging
                }
            }
        } else {
            val errorMsg = when (currentSlotCheck) {
                is SlotCheckState.Error -> currentSlotCheck.message
                is SlotCheckState.Success -> if (!currentSlotCheck.canCreateCharacter) "No character slots available." else "Unknown slot state."
                else -> "Cannot save character. Slot check pending or failed."
            }
            _saveCharacterState.value = SaveCharacterState.Error(errorMsg)
        }
    }

    // Call this if you want to allow retry or refresh of slot check from UI
    fun refreshSlotCheck() {
        checkCharacterSlotAvailability()
    }
} 