package com.bandi.textwar.data.datasource

import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.data.models.BattleRecordSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import timber.log.Timber
import javax.inject.Inject

class BattleRecordsRemoteDataSource @Inject constructor(
    private val supabaseClinet: SupabaseClient
) {

    private val postgrest: Postgrest
        get() = supabaseClinet.postgrest

    companion object {
        private const val TABLE_NAME = "battle_records"
    }

    suspend fun saveBattleRecord(record: BattleRecordInput): Result<String> {
        return try {
            Timber.d("Saving battle record: $record")
            val result = postgrest.from(TABLE_NAME)
                .insert(record) {
                    select()
                }.decodeSingle<BattleRecordSupabase>()
            
            Timber.i("Battle record saved successfully. ID: ${result.id}")
            Result.success(result.id)
        } catch (e: Exception) {
            Timber.e(e, "Error saving battle record")
            Result.failure(e)
        }
    }

    suspend fun getBattleRecords(characterId: String?, limit: Int): Result<List<BattleRecordSupabase>> {
        return try {
            Timber.d("Fetching battle records. Character ID: $characterId, Limit: $limit")

            if (characterId != null) {
                // 특정 캐릭터의 전투 기록: RPC 호출 (RPC 함수가 이름들을 포함하여 반환한다고 가정)
                Timber.d("Using RPC for character-specific battle records with names.")
                val result = postgrest.rpc(
                    function = "get_character_battle_records",
                    parameters = mapOf("p_character_id" to characterId, "p_limit" to limit)
                ).decodeList<BattleRecordSupabase>()
                Timber.i("Fetched ${result.size} battle records for character $characterId via RPC.")
                Result.success(result)
            } else {
                // 모든 전투 기록: select 사용 및 foreign table join
                Timber.d("Fetching all battle records with character names using select.")
                val result = postgrest.rpc(
                    function = "get_battle_records_with_names", // 이 RPC는 characterId(nullable), limit을 받아 이름 포함 결과를 반환해야 함
                    parameters = mapOf("p_character_id_nullable" to characterId, "p_limit_val" to limit)
                ).decodeList<BattleRecordSupabase>()
                Timber.i("Fetched ${result.size} battle records with names.")
                Result.success(result)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching battle records with names")
            Result.failure(e)
        }
    }

    suspend fun getBattleRecord(recordId: String): Result<BattleRecordSupabase?> {
        return try {
            Timber.d("Fetching battle record with ID: $recordId including names via RPC")
            // RPC 호출로 변경
            val result = postgrest.rpc(
                function = "get_single_battle_record_with_names", // 새로운 RPC 함수
                parameters = mapOf("p_record_id" to recordId)
            )
            .decodeSingleOrNull<BattleRecordSupabase>() // 결과가 없을 수 있으므로 OrNull 사용

            Timber.i("Battle record fetched via RPC: $result")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching single battle record with ID $recordId via RPC")
            Result.failure(e)
        }
    }

    suspend fun updateImageUrl(recordId: String, imageUrl: String): Result<Unit> {
        return try {
            Timber.d("Updating image URL for record ID: $recordId, New URL: $imageUrl")
            postgrest.from(TABLE_NAME)
                .update(mapOf("image_url" to imageUrl)) {
                    filter {
                        eq("id", recordId)
                    }
                }
            Timber.i("Image URL updated successfully for record ID: $recordId")
            Result.success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Error updating image URL for record ID: $recordId")
            Result.failure(e)
        }
    }
}