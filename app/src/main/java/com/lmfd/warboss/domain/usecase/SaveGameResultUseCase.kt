package com.lmfd.warboss.domain.usecase

import com.lmfd.warboss.domain.model.GameResult
import com.lmfd.warboss.domain.repository.GameResultRepository
import java.util.UUID
import javax.inject.Inject

class SaveGameResultUseCase @Inject constructor(
    private val repository: GameResultRepository,
) {
    suspend operator fun invoke(
        armyListId: String?,
        myFactionName: String,
        opponentFactionName: String,
        playerScore: Int,
        opponentScore: Int,
    ) {
        repository.save(
            GameResult(
                id = UUID.randomUUID().toString(),
                armyListId = armyListId,
                myFactionName = myFactionName,
                opponentFactionName = opponentFactionName,
                playerScore = playerScore,
                opponentScore = opponentScore,
                didWin = playerScore > opponentScore,
                playedAt = System.currentTimeMillis(),
            )
        )
    }
}
