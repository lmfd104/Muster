package com.lmfd.warboss.domain.repository

import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.model.UnitSummary
import kotlinx.coroutines.flow.Flow

interface UnitRepository {
    fun getFactions(): Flow<List<Faction>>
    fun getUnitsForFaction(factionId: String): Flow<List<UnitSummary>>
    suspend fun getUnitDetail(unitId: String): UnitDetail?
}
