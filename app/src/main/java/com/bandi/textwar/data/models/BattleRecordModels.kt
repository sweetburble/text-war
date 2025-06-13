package com.bandi.textwar.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Supabase 'battle_records' 테이블에 새로운 데이터를 삽입할 때 사용되는 모델
 * id 및 createdAt은 서버에서 자동 생성되므로 포함하지 않는다.
 */
@Serializable
data class BattleRecordInput(
    @SerialName("character_a_id")
    val characterAId: String,

    @SerialName("character_b_id")
    val characterBId: String,

    @SerialName("winner_id")
    val winnerId: String? = null,

    val narrative: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null
)

/**
 * Supabase 'battle_records' 테이블에서 데이터를 읽어올 때 사용되는 모델
 */
@Serializable
data class BattleRecordSupabase(
    val id: String, // UUID

    @SerialName("character_a_id")
    val characterAId: String, // UUID

    @SerialName("character_b_id")
    val characterBId: String, // UUID

    @SerialName("winner_id")
    val winnerId: String? = null, // UUID

    val narrative: String? = null,

    @SerialName("image_url")
    val imageUrl: String? = null,

    @SerialName("created_at")
    val createdAt: String, // Timestamp string, EX) "2023-10-27T10:30:00+00:00"

    @SerialName("character_a_name")
    val characterAName: String? = null,

    @SerialName("character_b_name")
    val characterBName: String? = null,

    @SerialName("winner_name")
    val winnerName: String? = null
) 