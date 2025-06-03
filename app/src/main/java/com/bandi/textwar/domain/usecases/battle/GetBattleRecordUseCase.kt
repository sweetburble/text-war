package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.domain.models.BattleRecord
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import javax.inject.Inject

/**
 * 특정 전투 기록을 ID로 가져오는 UseCase
 */
class GetBattleRecordUseCase @Inject constructor(
    private val battleRecordsRepository: BattleRecordsRepository
) {
    suspend operator fun invoke(recordId: String): Result<BattleRecord?> {
        return battleRecordsRepository.getBattleRecord(recordId)
    }
} 