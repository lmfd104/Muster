package com.lmfd.warboss.domain.usecase

import android.content.Context
import android.net.Uri
import com.lmfd.warboss.data.bsdata.RosterParser
import com.lmfd.warboss.domain.repository.ArmyListRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

data class RosterImportResult(val listId: String, val listName: String, val unitCount: Int)

class ImportRosterUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val repository: ArmyListRepository,
) {
    suspend operator fun invoke(uri: Uri): RosterImportResult = withContext(Dispatchers.IO) {
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            cursor.moveToFirst()
            if (nameIndex >= 0) cursor.getString(nameIndex) else null
        } ?: ""

        val isZipped = fileName.endsWith(".rosz", ignoreCase = true)

        val roster = context.contentResolver.openInputStream(uri)?.use { stream ->
            RosterParser.parse(stream, isZipped)
        } ?: throw IllegalStateException("Could not open file")

        val listId = repository.createArmyList(
            name = roster.name.ifBlank { "Imported List" },
            factionId = roster.factionId.ifBlank { "imported" },
            factionName = roster.factionName.ifBlank { "Unknown Faction" },
            pointsLimit = 0,
        )

        roster.units.forEach { unit ->
            repository.importUnitToList(
                listId = listId,
                unitId = unit.entryId,
                importedName = unit.name,
                importedPoints = unit.pointsPerUnit,
                quantity = unit.quantity,
            )
        }


        RosterImportResult(listId, roster.name, roster.units.size)
    }
}
