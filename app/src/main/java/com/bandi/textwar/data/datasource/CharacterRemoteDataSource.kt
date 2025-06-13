package com.bandi.textwar.data.datasource

import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.data.models.CharacterInsert
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.data.models.UserProfile
import com.bandi.textwar.data.models.LeaderboardItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import javax.inject.Inject
import timber.log.Timber

/**
 * Supabase를 사용하여, 캐릭터 데이터를 조작하는 Remote DataSource
 */
class CharacterRemoteDataSource @Inject constructor(
    private val supabaseClient: SupabaseClient,
) {

    /**
     * 캐릭터를 삭제하는 메서드
     * @param characterId 삭제할 캐릭터의 ID(uuid)
     * @return 성공 시 Result.success(Unit), 실패 시 Result.failure(exception)
     *
     * 현재 로그인된 사용자의 캐릭터만 삭제할 수 있도록 user_id 조건을 추가
     * 캐릭터 삭제 시 battle_records에서 해당 캐릭터가 참조된 모든 row를 먼저 삭제한 후 캐릭터를 삭제
     */
    suspend fun deleteCharacter(characterId: String): Result<Unit> {
        return try {
            val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
                ?: return Result.failure(IllegalStateException("사용자가 로그인되어 있지 않습니다."))

            // 1. battle_records에서 해당 캐릭터가 참조된 모든 row를 먼저 삭제
            // character_a_id, character_b_id, winner_id 중 하나라도 일치하는 경우 모두 삭제
            supabaseClient.postgrest.from("battle_records")
                .delete {
                    filter {
                        or {
                            eq("character_a_id", characterId)
                            eq("character_b_id", characterId)
                            eq("winner_id", characterId)
                        }
                    }
                }

            // 2. 캐릭터 삭제
            supabaseClient.postgrest.from("characters")
                .delete {
                    filter {
                        eq("id", characterId)
                        eq("user_id", currentUserId)
                    }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "캐릭터 삭제 중 오류 발생")
            Result.failure(e)
        }
    }


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
     * 현재 로그인한 사용자가 소유하지 않은 캐릭터 목록을 Supabase에서 가져온다.
     * @param count 가져올 상대 캐릭터 후보 수
     * @return 상대 캐릭터 후보 목록
     */
    fun getOpponentCandidateCharacters(count: Int): Flow<List<CharacterDetail>> = flow {
        val currentUserId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("사용자가 로그인되어 있지 않습니다.")
        
        // MVP에서는 상대 선택 로직을 일부러 3초간 지연
        delay(3000)

        val result = supabaseClient.postgrest.from("characters")
            .select {
                filter {
                    neq("user_id", currentUserId) // 현재 사용자 ID와 다른 캐릭터만 선택
                }
                limit(count.toLong()) // 지정된 수만큼 제한
            }
            .decodeList<CharacterDetail>()
        emit(result)
    }

    /**
     * 특정 ID의 캐릭터 상세 정보를 Supabase에서 가져온다.
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
     * 새로운 캐릭터를 Supabase에 생성
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
     * 특정 사용자의 프로필 정보를 가져온다.
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
     * 특정 사용자가 생성한 캐릭터의 수를 가져온다.
     * @param userId 사용자 ID
     * @return 해당 사용자의 캐릭터 수
     */
    suspend fun getCharacterCount(userId: String): Int {
        val response = supabaseClient.postgrest.from("characters")
            .select { filter { eq("user_id", userId) } }
            .decodeList<JsonElement>()
        return response.size
    }

    /**
     * 캐릭터의 전투 통계를 업데이트 (승리 또는 패배)
     * Supabase RPC 함수를 호출하여 원자적으로 카운트를 업데이트
     * @param characterId 업데이트할 캐릭터의 ID
     * @param isWin 승리했는지 여부 (true면 승리, false면 패배)
     */
    suspend fun updateCharacterBattleStats(characterId: String, isWin: Boolean) {
        val rpcFunction = if (isWin) "increment_wins" else "increment_losses"
        supabaseClient.postgrest.rpc(
            function = rpcFunction,
            parameters = buildJsonObject {
                put("character_id_param", JsonPrimitive(characterId))
            }
        )
        // RPC 호출은 보통 반환값이 없거나, 있더라도 이 함수에서는 사용하지 않으므로 별도의 decode는 필요 없습니다.
    }

    /**
     * 특정 캐릭터의 마지막 전투 시간을 가져온다.
     * @param characterId 확인할 캐릭터의 ID
     * @return 마지막 전투 시간 (timestamp with time zone) 또는 null (정보가 없는 경우)
     */
    suspend fun getCharacterLastBattleTimestamp(characterId: String): String? {
        val result = supabaseClient.postgrest.from("characters")
            // 특정 컬럼만 선택
            .select(columns = Columns.list("last_battle_timestamp")) {
                filter { eq("id", characterId) }
            }
            .decodeSingleOrNull<Map<String, String?>>() // 단일 객체 또는 null로 디코딩

        return result?.get("last_battle_timestamp")
    }

    /**
     * 특정 캐릭터의 마지막 전투 시간을 현재 시간으로 업데이트
     * Supabase RPC 함수 'update_character_last_battle_timestamp'를 호출
     * @param characterId 업데이트할 캐릭터의 ID
     */
    suspend fun updateCharacterLastBattleTimestamp(characterId: String) {
        supabaseClient.postgrest.rpc(
            function = "update_character_last_battle_timestamp", // Supabase에 생성된 RPC 함수 이름
            parameters = buildJsonObject {
                put("character_id_param", JsonPrimitive(characterId))
            }
        )
    }

    /**
     * 모든 캐릭터 정보를 리더보드용으로 가져온다.
     * users 테이블과 조인하여 유저 닉네임을 함께 가져오고,
     * rating 높은 순 -> wins 높은 순 -> character_name 문자열 순으로 정렬
     */
    suspend fun getLeaderboardData(): Result<List<LeaderboardItem>> {
        return try {
            val result = supabaseClient.postgrest.from("characters")
                .select(columns = Columns.raw("""
                    user_id,
                    character_name,
                    wins,
                    losses,
                    rating,
                    users ( display_name )
                """.trimIndent())) {
                    order(column = "rating", order = Order.DESCENDING)
                    order(column = "wins", order = Order.DESCENDING)
                    order(column = "character_name", order = Order.ASCENDING)
                }

            val dtoList = result.decodeList<CharacterWithUserDto>() // 그 다음 디코딩

            val leaderboardItems = dtoList.map { dto ->
                LeaderboardItem(
                    userDisplayName = dto.users?.display_name ?: "알 수 없는 유저",
                    characterName = dto.character_name,
                    wins = dto.wins,
                    losses = dto.losses,
                    rating = dto.rating
                )
            }
            Result.success(leaderboardItems)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching leaderboard data")
            Result.failure(e)
        }
    }

    // Supabase 응답을 매핑하기 위한 내부 DTO
    @Serializable
    private data class CharacterWithUserDto(
        val user_id: String,
        val character_name: String,
        val wins: Int,
        val losses: Int,
        val rating: Int,
        val users: UserDto?,
    )

    @Serializable
    private data class UserDto(
        val display_name: String?,
    )
}