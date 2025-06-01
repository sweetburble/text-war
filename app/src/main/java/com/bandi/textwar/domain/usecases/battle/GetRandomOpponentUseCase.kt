package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * 전투 상대를 무작위로 선택하는 UseCase
 */
class GetRandomOpponentUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(): Result<CharacterDetail> {
        return try {
            // Repository를 통해 상대 캐릭터를 가져옵니다.
            val opponent = characterRepository.getRandomOpponentCharacter().firstOrNull()
            if (opponent != null) {
                Result.success(opponent)
            } else {
                Result.failure(Exception("상대 캐릭터를 찾을 수 없습니다."))
            }
        } catch (e: Exception) {
            Result.failure(Exception("상대 캐릭터를 가져오는 중 오류가 발생했습니다: ${e.message}", e))
        }
    }
}