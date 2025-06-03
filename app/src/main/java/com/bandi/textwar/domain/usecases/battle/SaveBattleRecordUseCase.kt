package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.data.models.BattleRecordInput
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import javax.inject.Inject

class SaveBattleRecordUseCase @Inject constructor(
    private val battleRecordsRepository: BattleRecordsRepository
) {
    suspend operator fun invoke(record: BattleRecordInput): Result<String> {
        return battleRecordsRepository.saveBattleRecord(record)
    }
} 