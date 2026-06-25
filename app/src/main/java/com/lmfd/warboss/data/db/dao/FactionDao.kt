package com.lmfd.warboss.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmfd.warboss.data.db.entity.FactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<FactionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entity: FactionEntity)

    /** Returns only non-library factions for the user-facing faction browser */
    @Query("SELECT * FROM faction WHERE isLibrary = 0 ORDER BY name ASC")
    fun listAll(): Flow<List<FactionEntity>>

    @Query("DELETE FROM faction")
    suspend fun deleteAll()
}
