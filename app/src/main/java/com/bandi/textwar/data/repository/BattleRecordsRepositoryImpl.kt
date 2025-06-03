package com.bandi.textwar.data.repository

import com.bandi.textwar.data.datasource.BattleRecordsRemoteDataSource
import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.data.models.BattleRecordSupabase
import com.bandi.textwar.domain.models.BattleRecord // Domain Model
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import javax.inject.Inject

class BattleRecordsRepositoryImpl @Inject constructor(
    private val remoteDataSource: BattleRecordsRemoteDataSource
) : BattleRecordsRepository {

    override suspend fun saveBattleRecord(record: BattleRecordInput): Result<String> {
        return remoteDataSource.saveBattleRecord(record)
    }

    override suspend fun getBattleRecords(characterId: String?, limit: Int): Result<List<BattleRecord>> {
        return remoteDataSource.getBattleRecords(characterId, limit).map { supabaseRecords ->
            supabaseRecords.map { it.toDomainModel() }
        }
    }

    override suspend fun getBattleRecord(recordId: String): Result<BattleRecord?> {
        return remoteDataSource.getBattleRecord(recordId).map { supabaseRecord ->
            supabaseRecord?.toDomainModel()
        }
    }

    override suspend fun updateImageUrl(recordId: String, imageUrl: String): Result<Unit> {
        return remoteDataSource.updateImageUrl(recordId, imageUrl)
    }
}


// Mapper function (BattleRecordSupabase -> BattleRecord)
// 이 함수는 BattleRecordSupabase 모델 내에 정의하거나, 별도의 Mapper 클래스/파일에 정의할 수 있습니다.
// 여기서는 편의상 확장 함수로 바로 작성합니다.
fun BattleRecordSupabase.toDomainModel(): BattleRecord {
    return BattleRecord(
        id = this.id,
        characterAId = this.characterAId,
        characterBId = this.characterBId,
        winnerId = this.winnerId,
        narrative = this.narrative,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt, // 날짜/시간 타입 변환이 필요할 수 있음 (String -> Date, ZonedDateTime 등)
    )
}