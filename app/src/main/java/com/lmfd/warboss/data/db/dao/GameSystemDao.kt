package com.lmfd.warboss.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmfd.warboss.data.db.entity.GameSystemEntity

@Dao
interface GameSystemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: GameSystemEntity)

    @Query("SELECT * FROM game_system WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): GameSystemEntity?
}
