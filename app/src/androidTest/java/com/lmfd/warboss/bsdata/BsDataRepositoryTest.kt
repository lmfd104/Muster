package com.lmfd.warboss.bsdata

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmfd.warboss.data.bsdata.BsDataDownloader
import com.lmfd.warboss.data.bsdata.BsDataParser
import com.lmfd.warboss.data.bsdata.BsDataRepositoryImpl
import com.lmfd.warboss.data.bsdata.ParseArchiveResult
import com.lmfd.warboss.data.bsdata.ParsedCategoryLink
import com.lmfd.warboss.data.bsdata.ParsedFaction
import com.lmfd.warboss.data.bsdata.ParsedGameSystem
import com.lmfd.warboss.data.bsdata.ParsedProfile
import com.lmfd.warboss.data.bsdata.ParsedUnit
import com.lmfd.warboss.data.db.WarbossDatabase
import com.lmfd.warboss.data.prefs.ImportPrefs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Integration test for BsDataRepositoryImpl.commitArchive():
 * verifies faction/unit/profile insertion, synthetic profile IDs,
 * library faction filtering, and wipe-then-reimport behaviour.
 */
@RunWith(AndroidJUnit4::class)
class BsDataRepositoryTest {

    private lateinit var db: WarbossDatabase
    private lateinit var repo: BsDataRepositoryImpl

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, WarbossDatabase::class.java).build()
        val prefs = ImportPrefs(
            context.getSharedPreferences("test_import_${System.nanoTime()}", Context.MODE_PRIVATE)
        )
        // commitArchive() never calls the downloader/parser — construct them without network
        repo = BsDataRepositoryImpl(
            downloader = BsDataDownloader(OkHttpClient(), context),
            parser = BsDataParser(),
            db = db,
            prefs = prefs,
        )
    }

    @After
    fun teardown() {
        db.close()
    }

    @Test
    fun commitArchive_insertsFactionAndUnit() = runBlocking {
        val result = ParseArchiveResult(
            gameSystem = ParsedGameSystem("sys-1", "Warhammer 40K", 1, "2.03"),
            factions = listOf(
                ParsedFaction(
                    catalogueId = "fac-orks",
                    catalogueName = "Orks",
                    revision = 1,
                    gameSystemId = "sys-1",
                    isLibrary = false,
                    units = listOf(
                        ParsedUnit(
                            id = "unit-boss",
                            name = "Warboss",
                            type = "unit",
                            points = 85,
                            minQuantity = 1,
                            maxQuantity = 1,
                            profiles = listOf(
                                ParsedProfile("prof-boss", "Warboss", "Infantry", mapOf("M" to "5\"", "T" to "6"))
                            ),
                            keywords = listOf("Ork"),
                            factionKeywords = listOf("WAAGH!"),
                            categoryLinks = listOf(ParsedCategoryLink("cat-troops", "Troops", true)),
                            hasUnresolvableLinks = false,
                        )
                    )
                )
            )
        )

        val (committed, skipped) = repo.commitArchive(result)

        assertEquals(1, committed)
        assertEquals(0, skipped)

        val factions = db.factionDao().listAll().first()
        assertEquals(1, factions.size)
        assertEquals("Orks", factions[0].name)
        assertFalse(factions[0].isLibrary)

        val units = db.unitDao().listByFaction("fac-orks").first()
        assertEquals(1, units.size)
        assertEquals("Warboss", units[0].name)
        assertEquals(85, units[0].points)

        // Profile uses synthetic ID "<profileId>_<unitId>"
        val profiles = db.profileDao().listByEntryId("unit-boss")
        assertEquals(1, profiles.size)
        assertEquals("prof-boss_unit-boss", profiles[0].id)
        assertEquals("Infantry", profiles[0].typeName)

        val characteristics = db.profileDao().listCharacteristics("prof-boss_unit-boss")
        assertEquals(2, characteristics.size)
        assertTrue(characteristics.any { it.name == "M" && it.value == "5\"" })
        assertTrue(characteristics.any { it.name == "T" && it.value == "6" })

        val keywords = db.profileDao().listKeywords("unit-boss")
        assertEquals(2, keywords.size)
        assertTrue(keywords.any { it.keyword == "Ork" && !it.isFactionKeyword })
        assertTrue(keywords.any { it.keyword == "WAAGH!" && it.isFactionKeyword })

        val categoryLinks = db.profileDao().listCategoryLinks("unit-boss")
        assertEquals(1, categoryLinks.size)
        assertEquals("Troops", categoryLinks[0].categoryName)
        assertTrue(categoryLinks[0].isPrimary)
    }

    @Test
    fun commitArchive_syntheticProfileId_preventsCollision() = runBlocking {
        // Same shared profile ID referenced by two different units — typical after entryLink resolution
        val sharedProfileId = "shared-weapon"
        val result = ParseArchiveResult(
            gameSystem = ParsedGameSystem("sys-1", "Test", 1, "2.03"),
            factions = listOf(
                ParsedFaction(
                    catalogueId = "fac-marines",
                    catalogueName = "Space Marines",
                    revision = 1,
                    gameSystemId = "sys-1",
                    isLibrary = false,
                    units = listOf(
                        makeUnit("unit-a", "Intercessors", sharedProfileId, "Bolt Rifle"),
                        makeUnit("unit-b", "Eliminators", sharedProfileId, "Bolt Rifle"),
                    )
                )
            )
        )

        repo.commitArchive(result)

        val profilesA = db.profileDao().listByEntryId("unit-a")
        val profilesB = db.profileDao().listByEntryId("unit-b")
        assertEquals(1, profilesA.size)
        assertEquals(1, profilesB.size)
        assertEquals("${sharedProfileId}_unit-a", profilesA[0].id)
        assertEquals("${sharedProfileId}_unit-b", profilesB[0].id)
        assertNotEquals(profilesA[0].id, profilesB[0].id)
    }

    @Test
    fun commitArchive_libraryFaction_hiddenFromListAll() = runBlocking {
        val result = ParseArchiveResult(
            gameSystem = ParsedGameSystem("sys-1", "Test", 1, "2.03"),
            factions = listOf(
                ParsedFaction("lib-1", "Ork Library", 1, "sys-1", isLibrary = true, units = emptyList()),
                ParsedFaction("fac-1", "Orks", 1, "sys-1", isLibrary = false, units = emptyList()),
            )
        )

        val (committed, _) = repo.commitArchive(result)
        assertEquals(2, committed)

        // listAll() filters WHERE isLibrary = 0
        val visible = db.factionDao().listAll().first()
        assertEquals(1, visible.size)
        assertEquals("Orks", visible[0].name)
    }

    @Test
    fun commitArchive_wipesThenReimports() = runBlocking {
        // First import
        repo.commitArchive(
            ParseArchiveResult(
                gameSystem = ParsedGameSystem("sys-1", "Test", 1, "2.03"),
                factions = listOf(ParsedFaction("fac-old", "Old Faction", 1, "sys-1", false, emptyList()))
            )
        )
        assertEquals(1, db.factionDao().listAll().first().size)

        // Second import — different factions
        repo.commitArchive(
            ParseArchiveResult(
                gameSystem = ParsedGameSystem("sys-1", "Test", 1, "2.03"),
                factions = listOf(
                    ParsedFaction("fac-new-a", "Alpha", 1, "sys-1", false, emptyList()),
                    ParsedFaction("fac-new-b", "Beta", 1, "sys-1", false, emptyList()),
                )
            )
        )

        val factions = db.factionDao().listAll().first()
        assertEquals(2, factions.size)
        assertTrue(factions.none { it.id == "fac-old" })
        assertTrue(factions.any { it.name == "Alpha" })
        assertTrue(factions.any { it.name == "Beta" })
    }

    private fun makeUnit(
        unitId: String,
        unitName: String,
        profileId: String,
        profileName: String,
    ) = ParsedUnit(
        id = unitId,
        name = unitName,
        type = "unit",
        points = 100,
        minQuantity = 1,
        maxQuantity = 1,
        profiles = listOf(ParsedProfile(profileId, profileName, "Weapon", mapOf("Range" to "24\""))),
        keywords = emptyList(),
        factionKeywords = emptyList(),
        categoryLinks = emptyList(),
        hasUnresolvableLinks = false,
    )
}
