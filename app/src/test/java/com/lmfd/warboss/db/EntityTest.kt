package com.lmfd.warboss.db

import com.lmfd.warboss.data.db.entity.CharacteristicEntity
import com.lmfd.warboss.data.db.entity.FactionEntity
import com.lmfd.warboss.data.db.entity.GameSystemEntity
import com.lmfd.warboss.data.db.entity.KeywordEntity
import com.lmfd.warboss.data.db.entity.ProfileEntity
import com.lmfd.warboss.data.db.entity.UnitEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure Kotlin entity construction tests — no Android context needed. */
class EntityTest {

    @Test
    fun gameSystem_fields() {
        val gs = GameSystemEntity(id = "sys-1", name = "W40k", revision = 1, bsVersion = "2.03")
        assertEquals("sys-1", gs.id)
        assertEquals("W40k", gs.name)
        assertEquals(1, gs.revision)
        assertEquals("2.03", gs.bsVersion)
    }

    @Test
    fun faction_isLibrary_defaultsDistinct() {
        val lib = FactionEntity(id = "lib", name = "Aeldari Library", revision = 1, gameSystemId = "sys-1", isLibrary = true)
        val fac = FactionEntity(id = "fac", name = "Craftworlds", revision = 1, gameSystemId = "sys-1", isLibrary = false)
        assertTrue(lib.isLibrary)
        assertFalse(fac.isLibrary)
    }

    @Test
    fun unit_hasUnresolvableLinks_defaultsFalse() {
        val unit = UnitEntity(id = "u1", factionId = "f1", name = "Boyz", type = "unit", points = 95, minQuantity = 10, maxQuantity = 20)
        assertFalse(unit.hasUnresolvableLinks)
    }

    @Test
    fun unit_copy_setsUnresolvableLinks() {
        val unit = UnitEntity(id = "u1", factionId = "f1", name = "Boyz", type = "unit", points = 95, minQuantity = 10, maxQuantity = 20)
        val flagged = unit.copy(hasUnresolvableLinks = true)
        assertTrue(flagged.hasUnresolvableLinks)
        assertEquals(unit.id, flagged.id)
    }

    @Test
    fun profile_typeName_preserved() {
        val p = ProfileEntity(id = "p1", entryId = "u1", factionId = "f1", name = "Boyz", typeName = "Unit")
        assertEquals("Unit", p.typeName)
    }

    @Test
    fun characteristic_autoIdDefault_zero() {
        val c = CharacteristicEntity(profileId = "p1", name = "M", value = "6\"")
        assertEquals(0L, c.id)
    }

    @Test
    fun keyword_factionKeyword_flag() {
        val fk = KeywordEntity(unitId = "u1", keyword = "Orks", isFactionKeyword = true)
        val kw = KeywordEntity(unitId = "u1", keyword = "Infantry", isFactionKeyword = false)
        assertTrue(fk.isFactionKeyword)
        assertFalse(kw.isFactionKeyword)
    }

    @Test
    fun unit_pointsStoredExactly() {
        // Verify Int field doesn't overflow for typical points values
        val battlewagon = UnitEntity(id = "bw", factionId = "f1", name = "Battlewagon", type = "unit", points = 160, minQuantity = 1, maxQuantity = 3)
        assertEquals(160, battlewagon.points)
    }
}
