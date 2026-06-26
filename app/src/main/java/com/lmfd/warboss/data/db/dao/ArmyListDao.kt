package com.lmfd.warboss.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmfd.warboss.data.db.entity.ArmyListEntity
import com.lmfd.warboss.data.db.entity.ArmyListUnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ArmyListDao {

    // --- Army lists ---

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(list: ArmyListEntity)

    @Query("DELETE FROM army_list WHERE id = :listId")
    suspend fun deleteList(listId: String)

    @Query("""
        SELECT al.id, al.name, al.factionId, al.factionName, al.pointsLimit, al.createdAt,
               COALESCE(SUM(u.points * alu.quantity), 0) AS pointsTotal,
               COUNT(alu.id) AS unitCount
        FROM army_list al
        LEFT JOIN army_list_unit alu ON alu.armyListId = al.id
        LEFT JOIN unit u ON u.id = alu.unitId
        GROUP BY al.id
        ORDER BY al.createdAt DESC
    """)
    fun getAllLists(): Flow<List<ArmyListRow>>

    @Query("""
        SELECT al.id, al.name, al.factionId, al.factionName, al.pointsLimit, al.createdAt,
               COALESCE(SUM(u.points * alu.quantity), 0) AS pointsTotal,
               COUNT(alu.id) AS unitCount
        FROM army_list al
        LEFT JOIN army_list_unit alu ON alu.armyListId = al.id
        LEFT JOIN unit u ON u.id = alu.unitId
        WHERE al.id = :listId
        GROUP BY al.id
    """)
    fun getListById(listId: String): Flow<ArmyListRow?>

    // --- Unit entries ---

    @Insert
    suspend fun insertEntry(entry: ArmyListUnitEntity)

    @Query("DELETE FROM army_list_unit WHERE id = :entryId")
    suspend fun removeEntry(entryId: String)

    @Query("UPDATE army_list_unit SET quantity = :quantity WHERE id = :entryId")
    suspend fun updateEntryQuantity(entryId: String, quantity: Int)

    @Query("""
        SELECT alu.id, alu.armyListId, alu.unitId, alu.quantity,
               u.name AS unitName, u.points AS unitPoints
        FROM army_list_unit alu
        JOIN unit u ON u.id = alu.unitId
        WHERE alu.armyListId = :listId
        ORDER BY u.name ASC
    """)
    fun getEntriesForList(listId: String): Flow<List<ArmyListEntryRow>>
}

data class ArmyListRow(
    val id: String,
    val name: String,
    val factionId: String,
    val factionName: String,
    val pointsLimit: Int,
    val createdAt: Long,
    val pointsTotal: Int,
    val unitCount: Int,
)

data class ArmyListEntryRow(
    val id: String,
    val armyListId: String,
    val unitId: String,
    val quantity: Int,
    val unitName: String,
    val unitPoints: Int,
)
