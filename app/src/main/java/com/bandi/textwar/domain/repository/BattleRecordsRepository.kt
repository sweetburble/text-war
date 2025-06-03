package com.bandi.textwar.domain.repository

import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.domain.models.BattleRecord // Domain Model로 변경 예정

interface BattleRecordsRepository {
    suspend fun saveBattleRecord(record: BattleRecordInput): Result<String>

    suspend fun getBattleRecords(characterId: String? = null, limit: Int = 20): Result<List<BattleRecord>> // Domain Model 사용

    suspend fun getBattleRecord(recordId: String): Result<BattleRecord?> // Domain Model 사용

    suspend fun updateImageUrl(recordId: String, imageUrl: String): Result<Unit>
} 