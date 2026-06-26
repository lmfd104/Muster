package com.lmfd.warboss.domain.repository

import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.ArmyListEntry
import kotlinx.coroutines.flow.Flow

interface ArmyListRepository {
    fun getArmyLists(): Flow<List<ArmyList>>
    fun getArmyList(listId: String): Flow<ArmyList?>
    fun getArmyListEntries(listId: String): Flow<List<ArmyListEntry>>
    suspend fun createArmyList(name: String, factionId: String, factionName: String, pointsLimit: Int): String
    suspend fun addUnitToList(listId: String, unitId: String): String
    suspend fun importUnitToList(listId: String, unitId: String, importedName: String, importedPoints: Int, quantity: Int)
    suspend fun removeEntry(entryId: String)
    suspend fun updateEntryQuantity(entryId: String, quantity: Int)
    suspend fun deleteArmyList(listId: String)
}
