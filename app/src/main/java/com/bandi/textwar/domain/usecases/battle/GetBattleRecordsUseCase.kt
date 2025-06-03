package com.bandi.textwar.domain.usecases.battle

import com.bandi.textwar.domain.models.BattleRecord
import com.bandi.textwar.domain.repository.BattleRecordsRepository
import javax.inject.Inject

class GetBattleRecordsUseCase @Inject constructor(
    private val battleRecordsRepository: BattleRecordsRepository
) {
    suspend operator fun invoke(characterId: String? = null, limit: Int = 20): Result<List<BattleRecord>> {
        return battleRecordsRepository.getBattleRecords(characterId, limit)
    }
} 