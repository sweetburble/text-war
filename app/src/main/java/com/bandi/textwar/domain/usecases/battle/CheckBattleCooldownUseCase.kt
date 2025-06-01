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
        private const val COOLDOWN_SECONDS = 30L // 쿨다운 시간 (초)
    }

    /**
     * @param characterId 확인할 캐릭터의 ID
     * @return Pair<Boolean, Long> (쿨다운 중인지 여부, 남은 쿨다운 시간(초))
     * 쿨다운이 아니거나 타임스탬프 정보가 없으면 (false, 0L) 반환
     */
    suspend operator fun invoke(characterId: String): Flow<Pair<Boolean, Long>> {
        return getCharacterLastBattleTimestampUseCase(characterId).map { lastBattleTimestampString ->
            if (lastBattleTimestampString == null) {
            // 마지막 전투 시간이 없으면 쿨다운 아님
            Pair(false, 0L)
            } else {
                try {
                    // ISO 8601 형식의 문자열을 OffsetDateTime으로 파싱
                    val lastBattleTime = OffsetDateTime.parse(lastBattleTimestampString)
                    val currentTime = OffsetDateTime.now(ZoneOffset.UTC)
                    val cooldownDuration = Duration.ofSeconds(COOLDOWN_SECONDS)

                    val timeSinceLastBattle = Duration.between(lastBattleTime, currentTime)

                    if (timeSinceLastBattle < cooldownDuration) {
                        // 쿨다운 중
                        val remainingCooldown = cooldownDuration.minus(timeSinceLastBattle).seconds
                        Pair(true, remainingCooldown.coerceAtLeast(0L)) // 음수 방지
                    } else {
                        // 쿨다운 아님
                        Pair(false, 0L)
                    }
                } catch (e: Exception) {
                    // 타임스탬프 파싱 오류 등 예외 발생 시 쿨다운 아님으로 처리
                    Timber.Forest.e(e.toString())
                    Pair(false, 0L)
                }
            }
        }
    }
}