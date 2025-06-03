package com.bandi.textwar.domain.usecases.leaderboard

import com.bandi.textwar.data.models.LeaderboardItem
import com.bandi.textwar.domain.repository.CharacterRepository
import javax.inject.Inject

class GetLeaderboardDataUseCase @Inject constructor(
    private val characterRepository: CharacterRepository
) {
    suspend operator fun invoke(): Result<List<LeaderboardItem>> {
        return characterRepository.getLeaderboardData()
    }
}