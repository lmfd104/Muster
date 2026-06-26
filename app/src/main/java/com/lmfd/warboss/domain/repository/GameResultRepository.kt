package com.lmfd.warboss.domain.repository

import com.lmfd.warboss.domain.model.GameResult
import kotlinx.coroutines.flow.Flow

interface GameResultRepository {
    fun getAll(): Flow<List<GameResult>>
    suspend fun save(result: GameResult)
    suspend fun getRecent(limit: Int = 20): List<GameResult>
    suspend fun getRecentByOpponent(opponentFaction: String, limit: Int = 10): List<GameResult>
}
