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
     * 현재 로그인한 사용자의 모든 캐릭터 목록을 가져온다.
     * Supabase RLS 정책에 따라 현재 사용자의 데이터만 반환될 것으로 예상합니다.
     */
    suspend fun getCurrentUserCharacters(): Flow<List<CharacterSummary>>

    /**
     * 현재 로그인한 사용자가 소유하지 않은 캐릭터 중에서 무작위로 하나를 가져온다.
     * @param count 가져올 상대 캐릭터 후보 수 (기본값 50)
     * @return 선택된 상대 캐릭터 또는 null (후보가 없을 경우)
     */
    suspend fun getRandomOpponentCharacter(count: Int = 50): Flow<CharacterDetail?>

    /**
     * 특정 ID의 캐릭터 상세 정보를 가져온다.
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
     * 현재 로그인한 사용자의 프로필 정보를 가져온다.
     * @return UserProfile 또는 null
     */
    suspend fun getCurrentUserProfile(): Flow<UserProfile?>

    /**
     * 현재 로그인한 사용자의 캐릭터 생성 개수를 가져온다.
     * @return 캐릭터 수
     */
    suspend fun getCurrentUserCharacterCount(): Flow<Int>

    /**
     * 캐릭터의 전투 통계를 업데이트합니다.
     * @param characterId 업데이트할 캐릭터의 ID
     * @param isWin 승리 여부 (true면 승리, false면 패배)
     */
    suspend fun updateCharacterBattleStats(characterId: String, isWin: Boolean)

    /**
     * 특정 캐릭터의 마지막 전투 시간을 가져옵니다.
     * @param characterId 확인할 캐릭터의 ID
     * @return 마지막 전투 시간 (ISO 8601 형식의 문자열) 또는 null
     */
    suspend fun getCharacterLastBattleTimestamp(characterId: String): Flow<String?>

    /**
     * 특정 캐릭터의 마지막 전투 시간을 현재 시간으로 업데이트합니다.
     * @param characterId 업데이트할 캐릭터의 ID
     */
    suspend fun updateCharacterLastBattleTimestamp(characterId: String)
} 