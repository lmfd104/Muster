package com.lmfd.warboss.usecase

import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.domain.repository.UnitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeUnitRepository : UnitRepository {
    private val factionsFlow = MutableStateFlow<List<Faction>>(emptyList())
    private val unitsFlow = MutableStateFlow<List<UnitSummary>>(emptyList())
    private var detail: UnitDetail? = null

    fun setFactions(factions: List<Faction>) { factionsFlow.value = factions }
    fun setUnits(units: List<UnitSummary>) { unitsFlow.value = units }
    fun setDetail(d: UnitDetail?) { detail = d }

    override fun getFactions(): Flow<List<Faction>> = factionsFlow
    override fun getUnitsForFaction(factionId: String): Flow<List<UnitSummary>> = unitsFlow
    override suspend fun getUnitDetail(unitId: String): UnitDetail? = detail
}
