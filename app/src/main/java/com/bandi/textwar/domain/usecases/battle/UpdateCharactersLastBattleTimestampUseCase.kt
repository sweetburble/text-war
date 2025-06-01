package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * 전투에 참여한 두 캐릭터의 마지막 전투 시간을 현재 시간으로 업데이트하는 UseCase
 */
class UpdateCharactersLastBattleTimestampUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(character1Id: String, character2Id: String) {
        // 각 캐릭터의 마지막 전투 시간을 업데이트
        characterRepository.updateCharacterLastBattleTimestamp(character1Id)
        characterRepository.updateCharacterLastBattleTimestamp(character2Id)
    }
}