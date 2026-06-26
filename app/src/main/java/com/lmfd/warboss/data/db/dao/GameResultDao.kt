package com.lmfd.warboss.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmfd.warboss.data.db.entity.GameResultEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GameResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(result: GameResultEntity)

    @Query("SELECT * FROM game_result ORDER BY playedAt DESC")
    fun getAll(): Flow<List<GameResultEntity>>

    @Query("SELECT * FROM game_result WHERE opponentFactionName = :faction ORDER BY playedAt DESC")
    fun getByOpponent(faction: String): Flow<List<GameResultEntity>>

    @Query("SELECT * FROM game_result ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecent(limit: Int): List<GameResultEntity>

    @Query("SELECT * FROM game_result WHERE opponentFactionName = :faction ORDER BY playedAt DESC LIMIT :limit")
    suspend fun getRecentByOpponent(faction: String, limit: Int): List<GameResultEntity>
}
