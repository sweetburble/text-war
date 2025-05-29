package com.bandi.textwar.data.datasource

import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.data.models.CharacterInsert
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.data.models.UserProfile
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.JsonElement
import javax.inject.Inject

/**
 * Supabase를 사용하여 캐릭터 데이터를 가져오는 Remote DataSource
 */
class CharacterRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {

    /**
     * 현재 로그인된 사용자의 캐릭터 목록을 Supabase에서 가져온다.
     */
    fun getCurrentUserCharacters(): Flow<List<CharacterSummary>> = flow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("사용자가 로그인되어 있지 않습니다.")

        // postgrest를 사용하여 'characters' 테이블에서 데이터 조회
        val result = supabaseClient.postgrest.from("characters")
            .select {
                filter {
                    eq("user_id", currentUserId) // RLS가 있어도, 명시적으로 필터링 가능
                }
            }
            .decodeList<CharacterSummary>() // 결과를 CharacterSummary 리스트로 디코딩

        emit(result)
    }

    /**
     * 특정 ID의 캐릭터 상세 정보를 Supabase에서 가져옵니다.
     * @param characterId 가져올 캐릭터의 ID
     * @return 요청한 ID의 CharacterDetail. 해당 ID의 캐릭터가 없거나 접근 권한이 없으면 오류 발생 가능.
     */
    fun getCharacterDetail(characterId: String): Flow<CharacterDetail> = flow {
        val result = supabaseClient.postgrest.from("characters")
            .select {
                filter {
                    eq("id", characterId)
                }
            }
            .decodeSingle<CharacterDetail>() // 단일 객체로 디코딩
        emit(result)
    }

    /**
     * 새로운 캐릭터를 Supabase에 생성합니다.
     * @param characterData 생성할 캐릭터의 데이터 (user_id, character_name, description 포함)
     * @return 생성된 캐릭터 데이터 (Supabase에서 반환된 값 기준)
     */
    fun createCharacter(characterData: CharacterInsert): Flow<CharacterInsert> = flow {
        val result = supabaseClient.postgrest.from("characters").insert(characterData) {
             select() // 삽입 후 특정 컬럼만 반환받고 싶을 때.
        }.decodeSingle<CharacterInsert>()

        emit(result)
    }

    /**
     * 특정 사용자의 프로필 정보를 가져옵니다.
     * @param userId 가져올 사용자의 ID
     * @return UserProfile 또는 null (사용자가 없거나 RLS로 접근 불가 시)
     */
    suspend fun getUserProfile(userId: String): UserProfile? {
        return supabaseClient.postgrest.from("users")
            .select {
                filter { eq("id", userId) }
            }
            .decodeSingleOrNull<UserProfile>()
    }

    /**
     * 특정 사용자가 생성한 캐릭터의 수를 가져옵니다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 캐릭터 수
     */
    suspend fun getCharacterCount(userId: String): Int {
        val response = supabaseClient.postgrest.from("characters")
            .select { filter { eq("user_id", userId) } }
            .decodeList<JsonElement>()
        return response.size
    }

    // TODO: 캐릭터 수정, 삭제 등의 함수 추가 필요
}