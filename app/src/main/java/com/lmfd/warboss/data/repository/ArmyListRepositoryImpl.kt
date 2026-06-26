package com.lmfd.warboss.data.repository

import com.lmfd.warboss.data.db.dao.ArmyListDao
import com.lmfd.warboss.data.db.dao.ArmyListEntryRow
import com.lmfd.warboss.data.db.dao.ArmyListRow
import com.lmfd.warboss.data.db.entity.ArmyListEntity
import com.lmfd.warboss.data.db.entity.ArmyListUnitEntity
import com.lmfd.warboss.domain.model.ArmyList
import com.lmfd.warboss.domain.model.ArmyListEntry
import com.lmfd.warboss.domain.repository.ArmyListRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject

class ArmyListRepositoryImpl @Inject constructor(
    private val dao: ArmyListDao,
) : ArmyListRepository {

    override fun getArmyLists(): Flow<List<ArmyList>> =
        dao.getAllLists().map { rows -> rows.map { it.toDomain() } }

    override fun getArmyList(listId: String): Flow<ArmyList?> =
        dao.getListById(listId).map { it?.toDomain() }

    override fun getArmyListEntries(listId: String): Flow<List<ArmyListEntry>> =
        dao.getEntriesForList(listId).map { rows -> rows.map { it.toDomain() } }

    override suspend fun createArmyList(
        name: String,
        factionId: String,
        factionName: String,
        pointsLimit: Int,
    ): String {
        val id = UUID.randomUUID().toString()
        dao.insertList(
            ArmyListEntity(
                id = id,
                name = name,
                factionId = factionId,
                factionName = factionName,
                pointsLimit = pointsLimit,
                createdAt = System.currentTimeMillis(),
            )
        )
        return id
    }

    override suspend fun addUnitToList(listId: String, unitId: String): String {
        val id = UUID.randomUUID().toString()
        dao.insertEntry(ArmyListUnitEntity(id = id, armyListId = listId, unitId = unitId))
        return id
    }

    override suspend fun importUnitToList(
        listId: String,
        unitId: String,
        importedName: String,
        importedPoints: Int,
        quantity: Int,
    ) {
        dao.insertEntry(
            ArmyListUnitEntity(
                id = UUID.randomUUID().toString(),
                armyListId = listId,
                unitId = unitId,
                quantity = quantity,
                importedName = importedName,
                importedPoints = importedPoints,
            )
        )
    }

    override suspend fun removeEntry(entryId: String) = dao.removeEntry(entryId)

    override suspend fun updateEntryQuantity(entryId: String, quantity: Int) =
        dao.updateEntryQuantity(entryId, quantity)

    override suspend fun deleteArmyList(listId: String) = dao.deleteList(listId)
}

private fun ArmyListRow.toDomain() = ArmyList(
    id = id,
    name = name,
    factionId = factionId,
    factionName = factionName,
    pointsTotal = pointsTotal,
    pointsLimit = pointsLimit,
    unitCount = unitCount,
)

private fun ArmyListEntryRow.toDomain() = ArmyListEntry(
    id = id,
    armyListId = armyListId,
    unitId = unitId,
    unitName = unitName,
    unitPoints = unitPoints,
    quantity = quantity,
)
