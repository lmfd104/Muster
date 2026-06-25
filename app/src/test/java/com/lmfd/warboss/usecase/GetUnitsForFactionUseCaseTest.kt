package com.lmfd.warboss.usecase

import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.domain.usecase.GetUnitsForFactionUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUnitsForFactionUseCaseTest {

    private lateinit var repo: FakeUnitRepository
    private lateinit var useCase: GetUnitsForFactionUseCase

    @Before
    fun setup() {
        repo = FakeUnitRepository()
        useCase = GetUnitsForFactionUseCase(repo)
    }

    @Test
    fun invoke_emitsUnitsFromRepository() = runBlocking {
        repo.setUnits(listOf(
            makeUnit("u-boyz", "Boyz", 95),
            makeUnit("u-nobs", "Nobz", 130),
        ))
        val result = useCase("f-orks").first()
        assertEquals(2, result.size)
        assertEquals("Boyz", result[0].name)
        assertEquals(95, result[0].points)
    }

    @Test
    fun invoke_emptyFaction_emitsEmptyList() = runBlocking {
        val result = useCase("f-empty").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_preservesUnitFields() = runBlocking {
        repo.setUnits(listOf(makeUnit("u-1", "Intercessors", 80)))
        val unit = useCase("f-marines").first().first()
        assertEquals("u-1", unit.id)
        assertEquals("f-orks", unit.factionId)
        assertEquals("unit", unit.type)
        assertEquals(1, unit.minQuantity)
        assertEquals(10, unit.maxQuantity)
    }

    private fun makeUnit(id: String, name: String, points: Int) = UnitSummary(
        id = id,
        factionId = "f-orks",
        name = name,
        type = "unit",
        points = points,
        minQuantity = 1,
        maxQuantity = 10,
        hasUnresolvableLinks = false,
    )
}
