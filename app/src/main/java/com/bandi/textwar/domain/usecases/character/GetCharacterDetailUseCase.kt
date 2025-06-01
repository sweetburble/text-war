package com.bandi.textwar.domain.usecases.character

import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * 특정 ID의 캐릭터 상세 정보를 가져오는 유즈케이스
 */
class GetCharacterDetailUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(characterId: String): Flow<CharacterDetail> {
        return characterRepository.getCharacterDetail(characterId)
    }
} 