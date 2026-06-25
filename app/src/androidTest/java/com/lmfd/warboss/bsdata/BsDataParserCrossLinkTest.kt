package com.lmfd.warboss.bsdata

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmfd.warboss.data.bsdata.BsDataParser
import com.lmfd.warboss.data.bsdata.GameSystemTypeRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Verifies the three-pass architecture's cross-catalogue link resolution.
 *
 * Scenario: catA.cat (thin faction) contains a unit that infoLinks to a profile defined
 * in catB.cat (library). Pass 2 must have parsed catB's sharedProfiles before Pass 3
 * can resolve catA's unit's infoLink.
 */
@RunWith(AndroidJUnit4::class)
class BsDataParserCrossLinkTest {

    private lateinit var parser: BsDataParser
    private lateinit var registry: GameSystemTypeRegistry

    @Before
    fun setup() {
        parser = BsDataParser()
        registry = GameSystemTypeRegistry()
    }

    @Test
    fun crossCatalogueInfolinkResolves_afterPass2() {
        // Pass 1: initialise registry from GST
        parser.parseGst(GST.byteInputStream(), registry)
        assertEquals(PTS_TYPE_ID, registry.ptsTypeId)

        // Pass 2: add catB's shared profiles to registry
        parser.parseSharedSections(CAT_B_LIBRARY.byteInputStream(), registry)
        assertNotNull(registry.sharedProfiles[LIBRARY_PROFILE_ID])

        // Pass 3: parse catA units — infoLink should resolve
        val faction = parser.parseFaction(CAT_A_FACTION.byteInputStream(), registry)
        assertNotNull(faction)
        assertEquals("Aeldari Faction", faction!!.catalogueName)
        assertFalse(faction.isLibrary)
        assertEquals(1, faction.units.size)

        val unit = faction.units[0]
        assertEquals("Warlock", unit.name)
        assertFalse("Infolink should have resolved — hasUnresolvableLinks must be false", unit.hasUnresolvableLinks)
        assertEquals(1, unit.profiles.size)
        assertEquals(LIBRARY_PROFILE_ID, unit.profiles[0].id)
        assertEquals("Smite", unit.profiles[0].name)
        assertEquals("Abilities", unit.profiles[0].typeName)
    }

    @Test
    fun crossCatalogueInfolinkUnresolved_withoutPass2() {
        // Pass 1 only — no Pass 2
        parser.parseGst(GST.byteInputStream(), registry)

        // Pass 3: infoLink cannot be resolved → hasUnresolvableLinks = true
        val faction = parser.parseFaction(CAT_A_FACTION.byteInputStream(), registry)
        assertNotNull(faction)
        val unit = faction!!.units.firstOrNull()
        assertNotNull(unit)
        assertEquals(true, unit!!.hasUnresolvableLinks)
        assertEquals(0, unit.profiles.size)
    }

    companion object {
        const val PTS_TYPE_ID = "pts-cross-001"
        const val LIBRARY_PROFILE_ID = "ability-smite-001"

        val GST = """
            <?xml version="1.0" encoding="UTF-8"?>
            <gameSystem id="sys-cross" name="Cross System" revision="1" battleScribeVersion="2.03">
              <costTypes>
                <costType id="$PTS_TYPE_ID" name="pts" defaultCostLimit="-1.0" hidden="false"/>
              </costTypes>
            </gameSystem>
        """.trimIndent()

        /** catB: library .cat defining shared profiles */
        val CAT_B_LIBRARY = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="cat-aeldari-library" name="Aeldari Library" gameSystemId="sys-cross" revision="1" library="true" battleScribeVersion="2.03">
              <sharedProfiles>
                <profile id="$LIBRARY_PROFILE_ID" name="Smite" typeName="Abilities" hidden="false">
                  <characteristics>
                    <characteristic name="Description" typeId="desc-001">Psychic attack</characteristic>
                  </characteristics>
                </profile>
              </sharedProfiles>
            </catalogue>
        """.trimIndent()

        /** catA: thin faction .cat that links to catB for profiles */
        val CAT_A_FACTION = """
            <?xml version="1.0" encoding="UTF-8"?>
            <catalogue id="cat-craftworlds" name="Aeldari Faction" gameSystemId="sys-cross" revision="1" library="false" battleScribeVersion="2.03">
              <catalogueLinks>
                <catalogueLink id="cl-link-001" name="Aeldari Library" hidden="false"
                               targetId="cat-aeldari-library" type="catalogue" importRootEntries="false"/>
              </catalogueLinks>
              <selectionEntries>
                <selectionEntry id="unit-warlock" name="Warlock" type="unit" hidden="false" import="true">
                  <costs>
                    <cost name="pts" typeId="$PTS_TYPE_ID" value="65.0"/>
                  </costs>
                  <infoLinks>
                    <infoLink id="il-smite" name="Smite" hidden="false" type="profile" targetId="$LIBRARY_PROFILE_ID"/>
                  </infoLinks>
                </selectionEntry>
              </selectionEntries>
            </catalogue>
        """.trimIndent()
    }
}
