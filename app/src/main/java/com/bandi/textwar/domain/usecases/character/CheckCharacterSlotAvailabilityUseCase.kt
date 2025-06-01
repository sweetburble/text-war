package com.bandi.textwar.domain.usecases

import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

data class SlotAvailabilityResult(
    val canCreateCharacter: Boolean,
    val message: String,
    val currentSlots: Int = 0,
    val maxSlots: Int = 0
)

/**
 * 현재 사용자의 캐릭터 생성 슬롯 가용성을 확인하는 유즈케이스
 */
class CheckCharacterSlotAvailabilityUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(): Flow<SlotAvailabilityResult> {
        val userProfileFlow = characterRepository.getCurrentUserProfile()
        val characterCountFlow = characterRepository.getCurrentUserCharacterCount()

        return userProfileFlow.combine(characterCountFlow) { userProfile, characterCount ->
            if (userProfile == null) {
                SlotAvailabilityResult(false, "사용자 정보를 가져올 수 없습니다.")
            } else {
                val maxSlots = userProfile.character_slots ?: 0 // 기본 슬롯 0으로 가정
                val canCreate = characterCount < maxSlots
                val message = if (canCreate) {
                    "남은 캐릭터 슬롯 (${characterCount}/${maxSlots})"
                } else {
                    "캐릭터 슬롯을 전부 사용했습니다 : (${characterCount}/${maxSlots})"
                }
                SlotAvailabilityResult(canCreate, message, characterCount, maxSlots)
            }
        }
    }
} 