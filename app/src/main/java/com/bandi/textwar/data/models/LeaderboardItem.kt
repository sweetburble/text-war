package com.bandi.textwar.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 리더보드에 표시될 항목을 나타내는 데이터 클래스
 */
@Serializable // 필요에 따라 Serializable 추가 (예: API 응답으로 직접 받는 경우)
data class LeaderboardItem(
    @SerialName("display_name")
    val userDisplayName: String,

    @SerialName("character_name")
    val characterName: String,

    val wins: Int,
    val losses: Int,
    val rating: Int
) 