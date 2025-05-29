package com.bandi.textwar.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 캐릭터 목록 표시에 사용될 간략한 캐릭터 정보 모델
 *
 * @property id 캐릭터의 고유 ID (목록에 표시되지 않음)
 * @property characterName 캐릭터 이름 (목록에 표시)
 * @property description 캐릭터 설명 (목록에 표시)
 */
@Serializable
data class CharacterSummary(
    val id: String, // Supabase에서는 uuid지만, Kotlin에서는 String으로 처리하는 것이 일반적

    @SerialName("character_name")
    val characterName: String,

    val description: String
)

/**
 * 캐릭터의 모든 상세 정보를 담는 데이터 모델
 *
 * @property id 캐릭터 고유 ID
 * @property userId 사용자 ID
 * @property characterName 캐릭터 이름
 * @property description 캐릭터 설명
 * @property wins 승리 횟수
 * @property losses 패배 횟수
 * @property rating 레이팅 점수
 * @property createdAt 생성 시각
 * @property lastBattleTimestamp 마지막 전투 시각 (nullable)
 */
@Serializable
data class CharacterDetail(
    val id: String,

    @SerialName("user_id")
    val userId: String,

    @SerialName("character_name")
    val characterName: String,

    val description: String,
    val wins: Int,
    val losses: Int,
    val rating: Int,

    @SerialName("created_at")
    val createdAt: String, // Supabase의 timestamptz는 String으로 받고 필요시 변환

    @SerialName("last_battle_timestamp")
    val lastBattleTimestamp: String? = null
)

/**
 * 'characters' 테이블에 새로운 캐릭터를 삽입할 때 사용될 데이터 모델
 */
@Serializable
data class CharacterInsert(
    @SerialName("user_id")
    val userId: String,

    @SerialName("character_name")
    val characterName: String,

    val description: String
) 