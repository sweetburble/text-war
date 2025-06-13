package com.bandi.textwar.domain.usecases.character

import com.bandi.textwar.domain.repository.CharacterRepository
import javax.inject.Inject

/**
 * 캐릭터 삭제 유스케이스
 * @param repository 캐릭터 도메인 레포지토리
 * @constructor 의존성 주입을 통해 CharacterRepository를 받음
 */
class DeleteCharacterUseCase @Inject constructor(
    private val repository: CharacterRepository
) {
    /**
     * 캐릭터를 삭제
     * @param characterId 삭제할 캐릭터의 ID(uuid)
     * @return 성공 시 Result.success(Unit), 실패 시 Result.failure(exception)
     */
    suspend operator fun invoke(characterId: String): Result<Unit> {
        return repository.deleteCharacter(characterId)
    }
}
