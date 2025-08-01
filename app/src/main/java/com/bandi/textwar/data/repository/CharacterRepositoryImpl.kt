package com.bandi.textwar.data.repository

import com.bandi.textwar.data.datasource.CharacterRemoteDataSource
import com.bandi.textwar.data.models.CharacterSummary
import com.bandi.textwar.data.models.CharacterDetail
import com.bandi.textwar.data.models.CharacterInsert
import com.bandi.textwar.data.models.UserProfile
import com.bandi.textwar.domain.repository.CharacterRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.random.Random
import com.bandi.textwar.data.models.LeaderboardItem

/**
 * CharacterRepository의 구현체
 * CharacterRemoteDataSource를 통해 데이터를 가져온다.
 */
class CharacterRepositoryImpl @Inject constructor(
    private val remoteDataSource: CharacterRemoteDataSource,
    private val supabaseClient: SupabaseClient
) : CharacterRepository {

    /**
     * 캐릭터를 삭제
     * @param characterId 삭제할 캐릭터의 ID(uuid)
     * @return 성공 시 Result.success(Unit), 실패 시 Result.failure(exception)
     */
    override suspend fun deleteCharacter(characterId: String): Result<Unit> {
        // RemoteDataSource의 deleteCharacter를 호출하여 실제 삭제를 수행
        return remoteDataSource.deleteCharacter(characterId)
    }

    override suspend fun getCurrentUserCharacters(): Flow<List<CharacterSummary>> {
        // 현재는 간단히 remoteDataSource를 직접 호출
        // 필요에 따라 여기에 캐싱 로직, 데이터 변환 로직 등을 추가할 수 있다.
        return remoteDataSource.getCurrentUserCharacters()
    }

    override suspend fun getRandomOpponentCharacter(count: Int): Flow<CharacterDetail?> {
        return remoteDataSource.getOpponentCandidateCharacters(count).map { candidates ->
            if (candidates.isEmpty()) {
                null
            } else {
                val randomIndex = Random.nextInt(candidates.size)
                candidates[randomIndex]
            }
        }
    }

    override suspend fun getCharacterDetail(characterId: String): Flow<CharacterDetail> {
        return remoteDataSource.getCharacterDetail(characterId)
    }

    override suspend fun createCharacter(characterName: String, description: String): Flow<CharacterInsert> {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated for creating character.")
        val characterData = CharacterInsert(
            userId = userId,
            characterName = characterName,
            description = description
        )
        return remoteDataSource.createCharacter(characterData)
    }

    override suspend fun getCurrentUserProfile(): Flow<UserProfile?> = flow {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
        if (userId == null) {
            emit(null)
            return@flow
        }
        val userProfile = remoteDataSource.getUserProfile(userId)
        emit(userProfile)
    }

    override suspend fun getCurrentUserCharacterCount(): Flow<Int> = flow {
        val userId = supabaseClient.auth.currentUserOrNull()?.id
            ?: throw IllegalStateException("User not authenticated for counting characters.")
        val count = remoteDataSource.getCharacterCount(userId)
        emit(count)
    }

    override suspend fun updateCharacterBattleStats(characterId: String, isWin: Boolean) {
        remoteDataSource.updateCharacterBattleStats(characterId, isWin)
    }

    override suspend fun getCharacterLastBattleTimestamp(characterId: String): Flow<String?> = flow {
        val timestamp = remoteDataSource.getCharacterLastBattleTimestamp(characterId)
        emit(timestamp)
    }

    override suspend fun updateCharacterLastBattleTimestamp(characterId: String) {
        remoteDataSource.updateCharacterLastBattleTimestamp(characterId)
    }

    override suspend fun getLeaderboardData(): Result<List<LeaderboardItem>> {
        return remoteDataSource.getLeaderboardData()
    }
} 