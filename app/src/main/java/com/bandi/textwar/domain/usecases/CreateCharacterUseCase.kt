package com.bandi.textwar.domain.usecases

import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.data.models.CharacterInsert
import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 새로운 캐릭터를 생성하는 유즈케이스입니다.
 */
class CreateCharacterUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(characterName: String, description: String): Flow<CharacterInsert> {
        // 여기서 추가적인 비즈니스 로직(예: 이름/설명 유효성 검사 심화)을 수행할 수 있습니다.
        // 현재는 Repository에 그대로 위임합니다.
        return characterRepository.createCharacter(characterName, description)
    }
} 