package com.lmfd.warboss.data.repository

import com.lmfd.warboss.data.db.dao.GameResultDao
import com.lmfd.warboss.data.db.entity.GameResultEntity
import com.lmfd.warboss.domain.model.GameResult
import com.lmfd.warboss.domain.repository.GameResultRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GameResultRepositoryImpl @Inject constructor(
    private val dao: GameResultDao,
) : GameResultRepository {

    override fun getAll(): Flow<List<GameResult>> =
        dao.getAll().map { list -> list.map { it.toDomain() } }

    override suspend fun save(result: GameResult) =
        dao.insert(result.toEntity())

    override suspend fun getRecent(limit: Int): List<GameResult> =
        dao.getRecent(limit).map { it.toDomain() }

    override suspend fun getRecentByOpponent(opponentFaction: String, limit: Int): List<GameResult> =
        dao.getRecentByOpponent(opponentFaction, limit).map { it.toDomain() }
}

private fun GameResultEntity.toDomain() = GameResult(
    id = id,
    armyListId = armyListId,
    myFactionName = myFactionName,
    opponentFactionName = opponentFactionName,
    playerScore = playerScore,
    opponentScore = opponentScore,
    didWin = didWin,
    playedAt = playedAt,
)

private fun GameResult.toEntity() = GameResultEntity(
    id = id,
    armyListId = armyListId,
    myFactionName = myFactionName,
    opponentFactionName = opponentFactionName,
    playerScore = playerScore,
    opponentScore = opponentScore,
    didWin = didWin,
    playedAt = playedAt,
)
