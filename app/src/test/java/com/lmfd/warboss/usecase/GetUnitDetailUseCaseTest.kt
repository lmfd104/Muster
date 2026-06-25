package com.lmfd.warboss.usecase

import com.lmfd.warboss.domain.model.CategoryLink
import com.lmfd.warboss.domain.model.UnitDetail
import com.lmfd.warboss.domain.model.UnitProfile
import com.lmfd.warboss.domain.model.UnitSummary
import com.lmfd.warboss.domain.usecase.GetUnitDetailUseCase
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetUnitDetailUseCaseTest {

    private lateinit var repo: FakeUnitRepository
    private lateinit var useCase: GetUnitDetailUseCase

    @Before
    fun setup() {
        repo = FakeUnitRepository()
        useCase = GetUnitDetailUseCase(repo)
    }

    @Test
    fun invoke_returnsDetailFromRepository() = runBlocking {
        val expected = makeDetail("u-boss", "Warboss")
        repo.setDetail(expected)
        val result = useCase("u-boss")
        assertNotNull(result)
        assertEquals("Warboss", result!!.summary.name)
        assertEquals(85, result.summary.points)
    }

    @Test
    fun invoke_repositoryReturnsNull_propagatesNull() = runBlocking {
        repo.setDetail(null)
        assertNull(useCase("u-missing"))
    }

    @Test
    fun invoke_preservesProfilesAndKeywords() = runBlocking {
        repo.setDetail(makeDetail("u-boyz", "Boyz"))
        val result = useCase("u-boyz")!!
        assertEquals(1, result.profiles.size)
        assertEquals("Boyz", result.profiles[0].name)
        assertEquals("6\"", result.profiles[0].characteristics["M"])
        assertEquals(listOf("Infantry"), result.keywords)
        assertEquals(listOf("Orks"), result.factionKeywords)
        assertEquals(1, result.categoryLinks.size)
        assertTrue(result.categoryLinks[0].isPrimary)
    }

    private fun makeDetail(unitId: String, unitName: String) = UnitDetail(
        summary = UnitSummary(unitId, "f-orks", unitName, "unit", 85, 10, 20, false),
        profiles = listOf(
            UnitProfile("prof-1", unitName, "Infantry", mapOf("M" to "6\"", "T" to "4"))
        ),
        keywords = listOf("Infantry"),
        factionKeywords = listOf("Orks"),
        categoryLinks = listOf(CategoryLink("cat-battleline", "Battleline", true)),
    )
}
