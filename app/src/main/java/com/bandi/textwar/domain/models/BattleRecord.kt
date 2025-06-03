package com.bandi.textwar.domain.models

/**
 * 전투 기록을 나타내는 Domain 모델입니다.
 */
data class BattleRecord(
    val id: String,
    val characterAId: String,
    val characterBId: String,
    val winnerId: String?,
    val narrative: String?,
    val imageUrl: String?,
    val createdAt: String, // UI에서 표시하기 편한 형태로 변환될 수 있음 (e.g., Date, ZonedDateTime)

    val characterAName: String? = null,
    val characterBName: String? = null,
    val winnerName: String? = null
)