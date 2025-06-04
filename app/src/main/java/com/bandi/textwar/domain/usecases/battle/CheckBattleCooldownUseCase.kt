package com.bandi.textwar.domain.usecases.battle

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Duration
import java.time.OffsetDateTime
import java.time.ZoneOffset
import javax.inject.Inject

/**
 * 캐릭터의 마지막 전투 시간과 현재 시간을 비교하여 쿨다운 여부 및 남은 시간을 반환하는 UseCase
*/
class CheckBattleCooldownUseCase @Inject constructor(
    private val getCharacterLastBattleTimestampUseCase: GetCharacterLastBattleTimestampUseCase
) {
    companion object {
        const val COOLDOWN_SECONDS = 30L // 쿨다운 시간 (초)
    }

    /**
    * @param characterId 확인할 캐릭터의 ID
    * @return Pair<Boolean, Long?> (쿨다운 중인지 여부, 마지막 전투 시각의 Unix timestamp (밀리초) 또는 null)
    * 마지막 전투 시각이 없거나 파싱 오류 시 (false, null) 반환.
    * 쿨다운이 아니라면 (false, 마지막 전투 시각 Unix timestamp) 반환.
    * 쿨다운 중이라면 (true, 마지막 전투 시각 Unix timestamp) 반환.
    */
   suspend operator fun invoke(characterId: String): Flow<Pair<Boolean, Long?>> {
       return getCharacterLastBattleTimestampUseCase(characterId).map { lastBattleTimestampString ->
           if (lastBattleTimestampString == null) {
               Pair(false, null)
           } else {
               try {
                   val lastBattleTime = OffsetDateTime.parse(lastBattleTimestampString)
                   val lastBattleMillis = lastBattleTime.toInstant().toEpochMilli()
                   val currentTimeMillis = OffsetDateTime.now(ZoneOffset.UTC).toInstant().toEpochMilli()
                   val cooldownMillis = COOLDOWN_SECONDS * 1000L

                   val timeSinceLastBattleMillis = currentTimeMillis - lastBattleMillis

                   if (timeSinceLastBattleMillis < cooldownMillis) {
                       Pair(true, lastBattleMillis) // 쿨다운 중, 마지막 전투 시간 반환
                   } else {
                       Pair(false, lastBattleMillis) // 쿨다운 아님, 마지막 전투 시간 반환
                   }
               } catch (e: Exception) {
                   Timber.Forest.e(e, "Error parsing last battle timestamp: $lastBattleTimestampString")
                   Pair(false, null) // 파싱 오류 시
               }
           }
       }
   }
}