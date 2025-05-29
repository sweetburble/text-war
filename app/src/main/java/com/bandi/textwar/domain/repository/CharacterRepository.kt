package com.bandi.textwar.domain.repository

import com.bandi.textwar.data.models.CharacterSummary // 생성한 모델 임포트
import com.bandi.textwar.data.models.CharacterDetail // 추가
import com.bandi.textwar.data.models.CharacterInsert // 추가
import com.bandi.textwar.data.models.UserProfile // 추가
import kotlinx.coroutines.flow.Flow

/**
 * 캐릭터 데이터에 접근하기 위한 Repository 인터페이스
 */
interface CharacterRepository {

    /**
     * 현재 로그인한 사용자의 모든 캐릭터 목록을 가져옵니다.
     * Supabase RLS 정책에 따라 현재 사용자의 데이터만 반환될 것으로 예상합니다.
     */
    suspend fun getCurrentUserCharacters(): Flow<List<CharacterSummary>>

    /**
     * 특정 ID의 캐릭터 상세 정보를 가져옵니다.
     * @param characterId 가져올 캐릭터의 ID
     */
    suspend fun getCharacterDetail(characterId: String): Flow<CharacterDetail>

    /**
     * 새로운 캐릭터를 생성합니다.
     * @param characterName 생성할 캐릭터의 이름
     * @param description 생성할 캐릭터의 설명
     * @return 생성 결과 (성공 시 생성된 CharacterInsert 객체, 실패 시 예외 발생)
     */
    suspend fun createCharacter(characterName: String, description: String): Flow<CharacterInsert>

    /**
     * 현재 로그인한 사용자의 프로필 정보를 가져옵니다.
     * @return UserProfile 또는 null
     */
    suspend fun getCurrentUserProfile(): Flow<UserProfile?>

    /**
     * 현재 로그인한 사용자의 캐릭터 생성 개수를 가져옵니다.
     * @return 캐릭터 수
     */
    suspend fun getCurrentUserCharacterCount(): Flow<Int>
} 