package com.lmfd.warboss.usecase

import com.lmfd.warboss.domain.model.Faction
import com.lmfd.warboss.domain.usecase.GetFactionsUseCase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetFactionsUseCaseTest {

    private lateinit var repo: FakeUnitRepository
    private lateinit var useCase: GetFactionsUseCase

    @Before
    fun setup() {
        repo = FakeUnitRepository()
        useCase = GetFactionsUseCase(repo)
    }

    @Test
    fun invoke_emitsFactionsFromRepository() = runBlocking {
        repo.setFactions(listOf(
            Faction("f-orks", "Orks", 162),
            Faction("f-marines", "Space Marines", 50),
        ))
        val result = useCase().first()
        assertEquals(2, result.size)
        assertEquals("Orks", result[0].name)
        assertEquals("Space Marines", result[1].name)
    }

    @Test
    fun invoke_emptyRepository_emitsEmptyList() = runBlocking {
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun invoke_preservesFactionFields() = runBlocking {
        repo.setFactions(listOf(Faction("f-1", "Aeldari", 99)))
        val faction = useCase().first().first()
        assertEquals("f-1", faction.id)
        assertEquals("Aeldari", faction.name)
        assertEquals(99, faction.revision)
    }
}
