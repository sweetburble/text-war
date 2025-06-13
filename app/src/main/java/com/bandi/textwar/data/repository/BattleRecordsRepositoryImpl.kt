package com.bandi.textwar.data.repository

import com.bandi.textwar.data.datasource.BattleRecordsRemoteDataSource
import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.data.models.BattleRecordSupabase
import com.bandi.textwar.domain.models.BattleRecord
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import javax.inject.Inject

class BattleRecordsRepositoryImpl @Inject constructor(
    private val remoteDataSource: BattleRecordsRemoteDataSource
) : BattleRecordsRepository {

    override suspend fun saveBattleRecord(record: BattleRecordInput): Result<String> {
        return remoteDataSource.saveBattleRecord(record)
    }

    override suspend fun getBattleRecordsForCharacter(characterId: String?, limit: Int): Result<List<BattleRecord>> {
        return remoteDataSource.getBattleRecordsForCharacter(characterId, limit).map { supabaseRecords ->
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
// 이 함수는 BattleRecordSupabase 모델 내에 정의하거나, 별도의 Mapper 클래스/파일에 정의할 수 있다.
// 여기서는 편의상 확장 함수로 바로 작성
fun BattleRecordSupabase.toDomainModel(): BattleRecord {
    return BattleRecord(
        id = this.id,
        characterAId = this.characterAId,
        characterBId = this.characterBId,
        winnerId = this.winnerId,
        narrative = this.narrative,
        imageUrl = this.imageUrl,
        createdAt = this.createdAt, // 날짜 or 시간 타입 변환을 정의했다
        characterAName = this.characterAName,
        characterBName = this.characterBName,
        winnerName = this.winnerName
    )
}