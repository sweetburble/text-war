package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 캐릭터의 마지막 전투 시간을 가져오는 UseCase
 */
class GetCharacterLastBattleTimestampUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(characterId: String): Flow<String?> {
        return characterRepository.getCharacterLastBattleTimestamp(characterId)
    }
}