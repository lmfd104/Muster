package com.lmfd.warboss.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmfd.warboss.data.db.WarbossDatabase
import com.lmfd.warboss.data.db.entity.CategoryLinkEntity
import com.lmfd.warboss.data.db.entity.CharacteristicEntity
import com.lmfd.warboss.data.db.entity.FactionEntity
import com.lmfd.warboss.data.db.entity.GameSystemEntity
import com.lmfd.warboss.data.db.entity.KeywordEntity
import com.lmfd.warboss.data.db.entity.ProfileEntity
import com.lmfd.warboss.data.db.entity.UnitEntity
import com.lmfd.warboss.data.repository.UnitRepositoryImpl
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UnitRepositoryTest {

    private lateinit var db: WarbossDatabase
    private lateinit var repo: UnitRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WarbossDatabase::class.java).build()
        repo = UnitRepositoryImpl(db)
    }

    @After
    fun teardown() {
        db.close()
    }

    // ─── getFactions ─────────────────────────────────────────────────────────

    @Test
    fun getFactions_returnsOnlyNonLibraryFactions() = runBlocking {
        seedGameSystem()
        db.factionDao().insertOrReplace(listOf(
            FactionEntity("f-orks", "Orks", 162, "sys-1", isLibrary = false),
            FactionEntity("f-lib", "Ork Library", 10, "sys-1", isLibrary = true),
        ))

        val factions = repo.getFactions().first()

        assertEquals(1, factions.size)
        assertEquals("Orks", factions[0].name)
        assertEquals("f-orks", factions[0].id)
        assertEquals(162, factions[0].revision)
    }

    @Test
    fun getFactions_emptyDb_emitsEmptyList() = runBlocking {
        val factions = repo.getFactions().first()
        assertTrue(factions.isEmpty())
    }

    // ─── getUnitsForFaction ───────────────────────────────────────────────────

    @Test
    fun getUnitsForFaction_returnsOnlyUnitTypeEntries() = runBlocking {
        seedGameSystem()
        db.factionDao().insertOrReplace(
            FactionEntity("f-orks", "Orks", 162, "sys-1", isLibrary = false)
        )
        db.unitDao().insertOrReplace(listOf(
            UnitEntity("u-boyz", "f-orks", "Boyz", "unit", 95, 10, 20),
            UnitEntity("u-upgrade", "f-orks", "Shoota", "upgrade", 0, 0, 10),
        ))

        val units = repo.getUnitsForFaction("f-orks").first()

        // listByFaction DAo filters type="unit"
        assertEquals(1, units.size)
        assertEquals("Boyz", units[0].name)
        assertEquals(95, units[0].points)
        assertEquals("f-orks", units[0].factionId)
    }

    @Test
    fun getUnitsForFaction_unknownFaction_emitsEmptyList() = runBlocking {
        val units = repo.getUnitsForFaction("f-unknown").first()
        assertTrue(units.isEmpty())
    }

    // ─── getUnitDetail ────────────────────────────────────────────────────────

    @Test
    fun getUnitDetail_missingUnit_returnsNull() = runBlocking {
        assertNull(repo.getUnitDetail("u-missing"))
    }

    @Test
    fun getUnitDetail_populatesAllFields() = runBlocking {
        seedGameSystem()
        db.factionDao().insertOrReplace(
            FactionEntity("f-orks", "Orks", 162, "sys-1", isLibrary = false)
        )
        db.unitDao().insertOrReplace(listOf(
            UnitEntity("u-boss", "f-orks", "Warboss", "unit", 85, 1, 1)
        ))
        db.profileDao().insertProfiles(listOf(
            ProfileEntity("prof-boss_u-boss", "u-boss", "f-orks", "Warboss", "Infantry")
        ))
        db.profileDao().insertCharacteristics(listOf(
            CharacteristicEntity(profileId = "prof-boss_u-boss", name = "M", value = "5\""),
            CharacteristicEntity(profileId = "prof-boss_u-boss", name = "T", value = "6"),
        ))
        db.profileDao().insertKeywords(listOf(
            KeywordEntity(unitId = "u-boss", keyword = "Ork", isFactionKeyword = false),
            KeywordEntity(unitId = "u-boss", keyword = "WAAGH!", isFactionKeyword = true),
        ))
        db.profileDao().insertCategoryLinks(listOf(
            CategoryLinkEntity(unitId = "u-boss", categoryId = "cat-1", categoryName = "Characters", isPrimary = true),
        ))

        val detail = repo.getUnitDetail("u-boss")

        assertNotNull(detail)
        assertEquals("Warboss", detail!!.summary.name)
        assertEquals(85, detail.summary.points)

        assertEquals(1, detail.profiles.size)
        assertEquals("Infantry", detail.profiles[0].typeName)
        assertEquals("5\"", detail.profiles[0].characteristics["M"])
        assertEquals("6", detail.profiles[0].characteristics["T"])

        assertEquals(listOf("Ork"), detail.keywords)
        assertEquals(listOf("WAAGH!"), detail.factionKeywords)

        assertEquals(1, detail.categoryLinks.size)
        assertEquals("Characters", detail.categoryLinks[0].categoryName)
        assertTrue(detail.categoryLinks[0].isPrimary)
    }

    @Test
    fun getUnitDetail_unitWithNoProfiles_returnsEmptyLists() = runBlocking {
        seedGameSystem()
        db.factionDao().insertOrReplace(
            FactionEntity("f-orks", "Orks", 162, "sys-1", isLibrary = false)
        )
        db.unitDao().insertOrReplace(listOf(
            UnitEntity("u-plain", "f-orks", "Plain Unit", "unit", 50, 1, 1)
        ))

        val detail = repo.getUnitDetail("u-plain")

        assertNotNull(detail)
        assertTrue(detail!!.profiles.isEmpty())
        assertTrue(detail.keywords.isEmpty())
        assertTrue(detail.factionKeywords.isEmpty())
        assertTrue(detail.categoryLinks.isEmpty())
    }

    private suspend fun seedGameSystem() {
        db.gameSystemDao().insertOrReplace(
            GameSystemEntity("sys-1", "Warhammer 40,000", 1, "2.03")
        )
    }
}
