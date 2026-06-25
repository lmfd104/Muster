package com.lmfd.warboss.db

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * In-memory Room round-trip tests for all Layer 1A entities and DAOs.
 * Runs as an instrumented test — execute via connectedDebugAndroidTest in Phase 4.
 */
@RunWith(AndroidJUnit4::class)
class WarbossDatabaseTest {

    private lateinit var db: WarbossDatabase

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            WarbossDatabase::class.java,
        ).allowMainThreadQueries().build()
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun gameSystem_insertAndQuery() = runBlocking {
        val gs = GameSystemEntity(id = "sys-1", name = "Warhammer 40,000", revision = 1, bsVersion = "2.03")
        db.gameSystemDao().insertOrReplace(gs)
        val result = db.gameSystemDao().getById("sys-1")
        assertNotNull(result)
        assertEquals("Warhammer 40,000", result!!.name)
    }

    @Test
    fun faction_insertAndListAll_excludesLibraries() = runBlocking {
        db.gameSystemDao().insertOrReplace(
            GameSystemEntity(id = "sys-1", name = "W40k", revision = 1, bsVersion = "2.03")
        )
        db.factionDao().insertOrReplace(listOf(
            FactionEntity(id = "f-orks", name = "Orks", revision = 162, gameSystemId = "sys-1", isLibrary = false),
            FactionEntity(id = "f-lib", name = "Aeldari Library", revision = 10, gameSystemId = "sys-1", isLibrary = true),
        ))
        val factions = db.factionDao().listAll().first()
        assertEquals(1, factions.size)
        assertEquals("Orks", factions[0].name)
    }

    @Test
    fun unit_insertAndQueryByFaction() = runBlocking {
        db.gameSystemDao().insertOrReplace(
            GameSystemEntity(id = "sys-1", name = "W40k", revision = 1, bsVersion = "2.03")
        )
        db.factionDao().insertOrReplace(
            FactionEntity(id = "f-orks", name = "Orks", revision = 162, gameSystemId = "sys-1", isLibrary = false)
        )
        db.unitDao().insertOrReplace(listOf(
            UnitEntity(id = "u-boyz", factionId = "f-orks", name = "Boyz", type = "unit", points = 95, minQuantity = 10, maxQuantity = 20),
            UnitEntity(id = "u-upgrade", factionId = "f-orks", name = "Shoota", type = "upgrade", points = 0, minQuantity = 0, maxQuantity = 10),
        ))
        val units = db.unitDao().listByFaction("f-orks").first()
        // listByFaction filters to type="unit" only
        assertEquals(1, units.size)
        assertEquals("Boyz", units[0].name)
        assertEquals(95, units[0].points)
    }

    @Test
    fun unit_getById_returnsUnit() = runBlocking {
        db.gameSystemDao().insertOrReplace(
            GameSystemEntity(id = "sys-1", name = "W40k", revision = 1, bsVersion = "2.03")
        )
        db.factionDao().insertOrReplace(
            FactionEntity(id = "f-orks", name = "Orks", revision = 162, gameSystemId = "sys-1", isLibrary = false)
        )
        db.unitDao().insertOrReplace(listOf(
            UnitEntity(id = "u-boyz", factionId = "f-orks", name = "Boyz", type = "unit", points = 95, minQuantity = 10, maxQuantity = 20),
        ))
        val unit = db.unitDao().getById("u-boyz").first()
        assertNotNull(unit)
        assertEquals("u-boyz", unit!!.id)
    }

    @Test
    fun unit_getById_missingReturnsNull() = runBlocking {
        val unit = db.unitDao().getById("does-not-exist").first()
        assertNull(unit)
    }

    @Test
    fun profiles_insertAndQueryByEntryId() = runBlocking {
        db.profileDao().insertProfiles(listOf(
            ProfileEntity(id = "p-unit", entryId = "u-boyz", factionId = "f-orks", name = "Boyz", typeName = "Unit"),
            ProfileEntity(id = "p-weapon", entryId = "u-boyz", factionId = "f-orks", name = "Shoota", typeName = "Ranged Weapons"),
            ProfileEntity(id = "p-other", entryId = "u-other", factionId = "f-orks", name = "Other", typeName = "Unit"),
        ))
        db.profileDao().insertCharacteristics(listOf(
            CharacteristicEntity(profileId = "p-unit", name = "M", value = "6\""),
            CharacteristicEntity(profileId = "p-unit", name = "T", value = "4"),
            CharacteristicEntity(profileId = "p-unit", name = "SV", value = "6+"),
            CharacteristicEntity(profileId = "p-unit", name = "W", value = "1"),
            CharacteristicEntity(profileId = "p-unit", name = "LD", value = "7+"),
            CharacteristicEntity(profileId = "p-unit", name = "OC", value = "2"),
        ))

        val profiles = db.profileDao().listByEntryId("u-boyz")
        assertEquals(2, profiles.size)

        val unitProfile = profiles.first { it.typeName == "Unit" }
        val chars = db.profileDao().listCharacteristics(unitProfile.id)
        assertEquals(6, chars.size)
        assertEquals("6\"", chars.first { it.name == "M" }.value)
    }

    @Test
    fun keywords_insertAndQuery() = runBlocking {
        db.profileDao().insertKeywords(listOf(
            KeywordEntity(unitId = "u-boyz", keyword = "Orks", isFactionKeyword = true),
            KeywordEntity(unitId = "u-boyz", keyword = "Infantry", isFactionKeyword = false),
        ))
        val keywords = db.profileDao().listKeywords("u-boyz")
        assertEquals(2, keywords.size)
        // Faction keywords sorted first
        assertEquals("Orks", keywords[0].keyword)
    }

    @Test
    fun categoryLinks_insertAndQuery() = runBlocking {
        db.profileDao().insertCategoryLinks(listOf(
            CategoryLinkEntity(unitId = "u-boyz", categoryId = "cat-battleline", categoryName = "Battleline", isPrimary = true),
        ))
        val cats = db.profileDao().listCategoryLinks("u-boyz")
        assertEquals(1, cats.size)
        assertEquals("Battleline", cats[0].categoryName)
    }

    @Test
    fun fkViolation_unitWithNonExistentFaction_throwsOnInsert() = runBlocking {
        db.gameSystemDao().insertOrReplace(
            GameSystemEntity(id = "sys-1", name = "W40k", revision = 1, bsVersion = "2.03")
        )
        // No faction inserted — FK violation expected
        var threw = false
        try {
            db.unitDao().insertOrReplace(listOf(
                UnitEntity(id = "u-bad", factionId = "f-nonexistent", name = "Ghost", type = "unit", points = 100, minQuantity = 1, maxQuantity = 1),
            ))
        } catch (e: Exception) {
            threw = true
        }
        assert(threw) { "Expected FK violation exception" }
    }
}
