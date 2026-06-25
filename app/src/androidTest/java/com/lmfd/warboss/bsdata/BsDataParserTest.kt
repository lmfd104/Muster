package com.lmfd.warboss.bsdata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmfd.warboss.data.bsdata.BsDataParser
import com.lmfd.warboss.data.bsdata.GameSystemTypeRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BsDataParserTest {

    private lateinit var parser: BsDataParser
    private lateinit var registry: GameSystemTypeRegistry

    @Before
    fun setup() {
        parser = BsDataParser()
        registry = GameSystemTypeRegistry()
    }

    // ─── GST parsing ─────────────────────────────────────────────────────────

    @Test
    fun parseGst_populatesPtsTypeId() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        assertEquals(PTS_TYPE_ID, registry.ptsTypeId)
    }

    @Test
    fun parseGst_populatesProfileTypeNames() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        assertEquals("Unit", registry.profileTypeNames[PROFILE_TYPE_ID])
    }

    @Test
    fun parseGst_populatesCategoryNames() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        assertEquals("Battleline", registry.categoryNames[CATEGORY_ID])
    }

    @Test
    fun parseGst_returnsGameSystem() {
        val gs = parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        assertNotNull(gs)
        assertEquals("sys-001", gs!!.id)
        assertEquals("Test System", gs.name)
        assertEquals(1, gs.revision)
    }

    // ─── Shared sections parsing ──────────────────────────────────────────────

    @Test
    fun parseSharedSections_addsProfileToRegistry() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        parser.parseSharedSections(CAT_WITH_SHARED_PROFILE.byteInputStream(), registry)
        assertNotNull(registry.sharedProfiles[SHARED_PROFILE_ID])
        assertEquals("Bolter", registry.sharedProfiles[SHARED_PROFILE_ID]?.name)
    }

    @Test
    fun parseSharedSections_addsSharedEntryProfilesToRegistry() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        parser.parseSharedSections(CAT_WITH_SHARED_ENTRY.byteInputStream(), registry)
        val profiles = registry.sharedEntryProfiles[SHARED_ENTRY_ID]
        assertNotNull(profiles)
        assertTrue(profiles!!.isNotEmpty())
        assertEquals("Battlewagon", profiles.first().name)
    }

    // ─── Faction / unit parsing ───────────────────────────────────────────────

    @Test
    fun parseFaction_returnsCorrectMetadata() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        val faction = parser.parseFaction(CAT_WITH_UNIT.byteInputStream(), registry)
        assertNotNull(faction)
        assertEquals("cat-faction-001", faction!!.catalogueId)
        assertEquals("Test Faction", faction.catalogueName)
        assertEquals(1, faction.revision)
        assertFalse(faction.isLibrary)
    }

    @Test
    fun parseFaction_parsesUnit_withCorrectPoints() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        val faction = parser.parseFaction(CAT_WITH_UNIT.byteInputStream(), registry)
        val unit = faction?.units?.firstOrNull()
        assertNotNull(unit)
        assertEquals("Test Unit", unit!!.name)
        assertEquals(80, unit.points)
        assertEquals("unit", unit.type)
    }

    @Test
    fun parseFaction_parsesUnit_withInlineProfile() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        val faction = parser.parseFaction(CAT_WITH_UNIT.byteInputStream(), registry)
        val profiles = faction?.units?.firstOrNull()?.profiles ?: emptyList()
        assertEquals(1, profiles.size)
        assertEquals("Unit", profiles[0].typeName)
        assertEquals("6\"", profiles[0].characteristics["M"])
        assertEquals("4", profiles[0].characteristics["T"])
    }

    @Test
    fun parseFaction_parsesUnit_withCategoryLink() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        val faction = parser.parseFaction(CAT_WITH_UNIT.byteInputStream(), registry)
        val links = faction?.units?.firstOrNull()?.categoryLinks ?: emptyList()
        assertEquals(1, links.size)
        assertEquals("Battleline", links[0].categoryName)
        assertTrue(links[0].isPrimary)
    }

    @Test
    fun parseFaction_libraryFlag_setCorrectly() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        val faction = parser.parseFaction(CAT_LIBRARY.byteInputStream(), registry)
        assertNotNull(faction)
        assertTrue(faction!!.isLibrary)
        assertTrue(faction.units.isEmpty())
    }

    @Test
    fun parseFaction_skipsNonUnitEntries() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        val faction = parser.parseFaction(CAT_WITH_MODEL_ENTRY.byteInputStream(), registry)
        // model-type entry should be skipped; only unit-type is imported
        assertTrue(faction?.units?.isEmpty() == true)
    }

    @Test
    fun parseFaction_infoLinkResolved_fromRegistry() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        // Populate registry with the shared profile
        parser.parseSharedSections(CAT_WITH_SHARED_PROFILE.byteInputStream(), registry)
        // Parse a unit that has an infoLink to that shared profile
        val faction = parser.parseFaction(CAT_UNIT_WITH_INFOLINK.byteInputStream(), registry)
        val profiles = faction?.units?.firstOrNull()?.profiles ?: emptyList()
        assertEquals(1, profiles.size)
        assertEquals("Bolter", profiles[0].name)
    }

    @Test
    fun parseFaction_unresolvableInfoLink_flagged() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        // Do NOT add the shared profile to registry → infoLink unresolvable
        val faction = parser.parseFaction(CAT_UNIT_WITH_INFOLINK.byteInputStream(), registry)
        val unit = faction?.units?.firstOrNull()
        assertNotNull(unit)
        assertTrue(unit!!.hasUnresolvableLinks)
        assertTrue(unit.profiles.isEmpty())
    }

    @Test
    fun parseFaction_entryLinkResolved_fromRegistry() {
        parser.parseGst(MINIMAL_GST.byteInputStream(), registry)
        // Pass 2: register the shared selection entry
        parser.parseSharedSections(CAT_WITH_SHARED_ENTRY.byteInputStream(), registry)
        assertNotNull(registry.sharedEntryProfiles[SHARED_ENTRY_ID])

        // Pass 3: parse a unit that entryLinks to the shared entry
        val faction = parser.parseFaction(CAT_UNIT_WITH_ENTRYLINK.byteInputStream(), registry)
        val unit = faction?.units?.firstOrNull()
        assertNotNull(unit)
        assertEquals(150, unit!!.points)
        assertEquals(1, unit.profiles.size)
        assertEquals("Battlewagon", unit.profiles[0].name)
        assertEquals("Unit", unit.profiles[0].typeName)
        assertEquals("9\"", unit.profiles[0].characteristics["M"])
    }

    // ─── XML fixtures ─────────────────────────────────────────────────────────

    companion object {
        const val PTS_TYPE_ID = "pts-001"
        const val PROFILE_TYPE_ID = "pt-001"
        const val CATEGORY_ID = "cat-001"
        const val SHARED_PROFILE_ID = "shared-prof-001"
        const val SHARED_ENTRY_ID = "shared-entry-001"

        val MINIMAL_GST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gameSystem id="sys-001" name="Test System" revision="1" battleScribeVersion="2.03">
              <costTypes>
                <costType id="$PTS_TYPE_ID" name="pts" defaultCostLimit="-1.0" hidden="false"/>
              </costTypes>
              <profileTypes>
                <profileType id="$PROFILE_TYPE_ID" name="Unit" hidden="false">
                  <characteristicTypes>
                    <characteristicType id="ct-001" name="M" hidden="false"/>
                    <characteristicType id="ct-002" name="T" hidden="false"/>
                  </characteristicTypes>
                </profileType>
              </profileTypes>
              <categoryEntries>
                <categoryEntry id="$CATEGORY_ID" name="Battleline" hidden="false"/>
              </categoryEntries>
            </gameSystem>
        """.trimIndent()

        val CAT_WITH_SHARED_PROFILE = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="lib-001" name="Test Library" gameSystemId="sys-001" revision="1" library="true" battleScribeVersion="2.03">
              <sharedProfiles>
                <profile id="$SHARED_PROFILE_ID" name="Bolter" typeName="Ranged Weapons" hidden="false">
                  <characteristics>
                    <characteristic name="Range" typeId="rng-001">24&quot;</characteristic>
                    <characteristic name="A" typeId="a-001">2</characteristic>
                  </characteristics>
                </profile>
              </sharedProfiles>
            </catalogue>
        """.trimIndent()

        val CAT_WITH_SHARED_ENTRY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="lib-002" name="Vehicle Library" gameSystemId="sys-001" revision="1" library="true" battleScribeVersion="2.03">
              <sharedSelectionEntries>
                <selectionEntry id="$SHARED_ENTRY_ID" name="Battlewagon" type="model" hidden="false" import="true">
                  <profiles>
                    <profile id="bw-prof-001" name="Battlewagon" typeName="Unit" hidden="false">
                      <characteristics>
                        <characteristic name="M" typeId="ct-001">9&quot;</characteristic>
                        <characteristic name="T" typeId="ct-002">10</characteristic>
                      </characteristics>
                    </profile>
                  </profiles>
                </selectionEntry>
              </sharedSelectionEntries>
            </catalogue>
        """.trimIndent()

        val CAT_WITH_UNIT = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="cat-faction-001" name="Test Faction" gameSystemId="sys-001" revision="1" library="false" battleScribeVersion="2.03">
              <selectionEntries>
                <selectionEntry id="unit-001" name="Test Unit" type="unit" hidden="false" import="true">
                  <costs>
                    <cost name="pts" typeId="$PTS_TYPE_ID" value="80.0"/>
                  </costs>
                  <profiles>
                    <profile id="prof-001" name="Test Unit" typeName="Unit" hidden="false">
                      <characteristics>
                        <characteristic name="M" typeId="ct-001">6&quot;</characteristic>
                        <characteristic name="T" typeId="ct-002">4</characteristic>
                      </characteristics>
                    </profile>
                  </profiles>
                  <categoryLinks>
                    <categoryLink id="cl-001" name="Battleline" targetId="$CATEGORY_ID" primary="true"/>
                  </categoryLinks>
                </selectionEntry>
              </selectionEntries>
            </catalogue>
        """.trimIndent()

        val CAT_LIBRARY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="lib-003" name="Some Library" gameSystemId="sys-001" revision="1" library="true" battleScribeVersion="2.03">
            </catalogue>
        """.trimIndent()

        val CAT_WITH_MODEL_ENTRY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="cat-002" name="Model Faction" gameSystemId="sys-001" revision="1" library="false" battleScribeVersion="2.03">
              <selectionEntries>
                <selectionEntry id="model-001" name="A Model" type="model" hidden="false" import="true">
                  <costs>
                    <cost name="pts" typeId="$PTS_TYPE_ID" value="20.0"/>
                  </costs>
                </selectionEntry>
              </selectionEntries>
            </catalogue>
        """.trimIndent()

        val CAT_UNIT_WITH_INFOLINK = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="cat-003" name="Infolink Faction" gameSystemId="sys-001" revision="1" library="false" battleScribeVersion="2.03">
              <selectionEntries>
                <selectionEntry id="unit-002" name="Bolter Marine" type="unit" hidden="false" import="true">
                  <costs>
                    <cost name="pts" typeId="$PTS_TYPE_ID" value="20.0"/>
                  </costs>
                  <infoLinks>
                    <infoLink id="il-001" name="Bolter" hidden="false" type="profile" targetId="$SHARED_PROFILE_ID"/>
                  </infoLinks>
                </selectionEntry>
              </selectionEntries>
            </catalogue>
        """.trimIndent()

        val CAT_UNIT_WITH_ENTRYLINK = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="cat-004" name="Vehicle Faction" gameSystemId="sys-001" revision="1" library="false" battleScribeVersion="2.03">
              <selectionEntries>
                <selectionEntry id="unit-003" name="Battlewagon Unit" type="unit" hidden="false" import="true">
                  <costs>
                    <cost name="pts" typeId="$PTS_TYPE_ID" value="150.0"/>
                  </costs>
                  <entryLinks>
                    <entryLink id="el-001" name="Battlewagon" hidden="false" type="selectionEntry" targetId="$SHARED_ENTRY_ID"/>
                  </entryLinks>
                </selectionEntry>
              </selectionEntries>
            </catalogue>
        """.trimIndent()
    }
}
