package com.lmfd.warboss.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmfd.warboss.data.db.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(entities: List<UnitEntity>)

    @Query("SELECT * FROM unit WHERE factionId = :factionId AND type = 'unit' ORDER BY name ASC")
    fun listByFaction(factionId: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM unit WHERE id = :id LIMIT 1")
    fun getById(id: String): Flow<UnitEntity?>

    @Query("DELETE FROM unit")
    suspend fun deleteAll()
}
