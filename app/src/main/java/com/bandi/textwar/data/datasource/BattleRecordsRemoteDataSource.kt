package com.bandi.textwar.data.datasource

import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.data.models.BattleRecordSupabase
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
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
            val query = postgrest.from(TABLE_NAME)
                .select {
                    order(column = "created_at", Order.DESCENDING)
                    limit(limit.toLong())
                }

            characterId?.let {
                // 또는 RPC를 사용하여 복잡한 쿼리 실행
                 val result = postgrest.rpc(
                    function = "get_character_battle_records",
                    parameters = mapOf("p_character_id" to it, "p_limit" to limit)
                ).decodeList<BattleRecordSupabase>()
                Timber.i("Fetched ${result.size} battle records for character $it.")
                Result.success(result)

            } ?: run {
                val result = query.decodeList<BattleRecordSupabase>()
                Timber.i("Fetched ${result.size} battle records.")
                Result.success(result)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error fetching battle records")
            Result.failure(e)
        }
    }
     suspend fun getBattleRecord(recordId: String): Result<BattleRecordSupabase?> {
        return try {
            Timber.d("Fetching battle record with ID: $recordId")
            val result = postgrest.from(TABLE_NAME)
                .select {
                    filter {
                        eq("id", recordId) // "battle_records" 테이블의 ID
                    }
                }
                .decodeSingle<BattleRecordSupabase>()
            Timber.i("Battle record fetched successfully: $result")
            Result.success(result)
        } catch (e: Exception) {
            Timber.e(e, "Error fetching battle record with ID $recordId")
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