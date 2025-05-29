package com.bandi.textwar.domain.usecases

 import com.bandi.textwar.data.models.CharacterSummary
 import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
 import javax.inject.Inject

/**
 * 사용자의 캐릭터 목록을 가져오는 유즈케이스
 */
class GetCharacterListUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
     suspend operator fun invoke(): Flow<List<CharacterSummary>> {
         return characterRepository.getCurrentUserCharacters()
     }
} 