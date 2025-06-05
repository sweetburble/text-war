package com.bandi.textwar.domain.repository

import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.domain.models.BattleRecord

interface BattleRecordsRepository {
    suspend fun saveBattleRecord(record: BattleRecordInput): Result<String>

    suspend fun getBattleRecordsForCharacter(characterId: String? = null, limit: Int = 20): Result<List<BattleRecord>>

    suspend fun getBattleRecord(recordId: String): Result<BattleRecord?>

    suspend fun updateImageUrl(recordId: String, imageUrl: String): Result<Unit>
} 