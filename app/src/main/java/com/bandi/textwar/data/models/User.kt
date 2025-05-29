package com.bandi.textwar.data.models

import kotlinx.serialization.Serializable

/**
 * 사용자 프로필 정보를 담는 데이터 모델
 * CharacterCreationViewModel에서 슬롯 확인 등에 사용됩니다.
 */
@Serializable
data class UserProfile(
    val id: String,
    val character_slots: Int? = null
    // 필요한 다른 사용자 관련 필드 추가 가능
) 