package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.data.remote.OpenAIService
import com.bandi.textwar.domain.repository.CharacterRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * 전투 결과를 가져오고, 캐릭터의 승패 정보를 업데이트하는 UseCase
 */
class ProcessBattleResultUseCase @Inject constructor(
    private val characterRepository: CharacterRepository,
    private val openAIService: OpenAIService
) {
    /**
     * UseCase를 실행
     * @param myCharacterId 내 캐릭터의 ID
     * @param opponentId 상대방 캐릭터의 ID
     * @return 전투 결과와 내 캐릭터 이름을 담은 Pair를 Flow로 반환
     *         오류 발생 시 예외를 발생시킵니다.
     */
    operator fun invoke(myCharacterId: String, opponentId: String): Flow<Pair<OpenAIService.BattleResult, String>> = flow {
        // 1. 내 캐릭터 정보 가져오기
        val myCharacter = characterRepository.getCharacterDetail(myCharacterId).firstOrNull()
            ?: throw IllegalStateException("내 캐릭터 정보를 가져올 수 없습니다: $myCharacterId")

        // 2. 상대방 캐릭터 정보 가져오기
        val opponentCharacter = characterRepository.getCharacterDetail(opponentId).firstOrNull()
            ?: throw IllegalStateException("상대방 캐릭터 정보를 가져올 수 없습니다: $opponentId")

        // 3. OpenAI API로 전투 결과 생성
        val battleResult = openAIService.generateBattleNarrative(myCharacter, opponentCharacter)
            ?: throw IllegalStateException("전투 결과를 생성하지 못했습니다.") // battleResult가 null인 경우를 처리

        if (battleResult.winnerName != null) {
            // 4. 전투 결과에 따라 승패 업데이트
            val winnerName = battleResult.winnerName
            val myCharacterWon = winnerName == myCharacter.characterName
            val opponentWon = winnerName == opponentCharacter.characterName

            if (myCharacterWon) {
                characterRepository.updateCharacterBattleStats(myCharacter.id, true)
                characterRepository.updateCharacterBattleStats(opponentCharacter.id, false)
            } else if (opponentWon) {
                characterRepository.updateCharacterBattleStats(myCharacter.id, false)
                characterRepository.updateCharacterBattleStats(opponentCharacter.id, true)
            }
            // 무승부 또는 승자 판독 불가 시 (예: winnerName이 있지만 myCharacter.characterName이나 opponentCharacter.characterName과 일치하지 않는 경우)
            // 현재 로직에서는 battleResult.winnerName이 null이 아니라면 둘 중 하나여야 하므로, 별도 처리는 하지 않습니다.
            // 만약 다른 케이스가 존재한다면 여기에 로직 추가가 필요합니다.

            emit(Pair(battleResult, myCharacter.characterName))
        } else {
            // winnerName이 null인 경우, narrative를 포함하여 예외를 발생시키거나 특정 오류 상태를 emit 할 수 있습니다.
            // 여기서는 narrative를 메시지로 포함하는 예외를 발생시킵니다.
            throw IllegalStateException(battleResult.narrative ?: "승자를 판별할 수 없습니다.")
        }
    }
} 