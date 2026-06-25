package com.lmfd.warboss.data.bsdata

/**
 * Accumulates cross-file symbol data across the three-pass BSData parse.
 *
 * Pass 1 populates ptsTypeId + profileTypeNames + categoryNames from the .gst.
 * Pass 2 adds sharedProfiles + sharedEntryProfiles from ALL .cat files.
 * Pass 3 resolves infoLink/entryLink ids against sharedProfiles and sharedEntryProfiles.
 */
class GameSystemTypeRegistry {

    /** typeId of the "pts" cost type — used to filter points from multi-cost-type lists */
    var ptsTypeId: String = ""

    /** Profile type name by typeId: e.g. "Unit", "Ranged Weapons", "Melee Weapons", "Abilities" */
    val profileTypeNames: MutableMap<String, String> = mutableMapOf()

    /** Category name by id: e.g. "Battleline", "Character", "Vehicle" */
    val categoryNames: MutableMap<String, String> = mutableMapOf()

    /**
     * Shared profiles indexed by profile id.
     * Populated from .gst <sharedProfiles> and ALL .cat <sharedProfiles>.
     * Used to resolve <infoLink type="profile"> targetId references.
     */
    val sharedProfiles: MutableMap<String, RegistryProfile> = mutableMapOf()

    /**
     * Profiles grouped by their parent selectionEntry id.
     * Populated from .gst and ALL .cat <sharedSelectionEntries>.
     * Used to resolve <entryLink type="selectionEntry"> targetId references
     * (e.g. unit → model → model's profiles).
     */
    val sharedEntryProfiles: MutableMap<String, MutableList<RegistryProfile>> = mutableMapOf()

    /**
     * Full unit data from <sharedSelectionEntries> entries with type="unit".
     * Populated during Pass 1 (.gst) and Pass 2 (.cat library files).
     * Used to resolve catalogue-level <entryLinks> in Pass 3 (wh40k-10e pattern).
     */
    val sharedUnits: MutableMap<String, ParsedUnit> = mutableMapOf()

    fun isNonEmpty(): Boolean = ptsTypeId.isNotEmpty() || profileTypeNames.isNotEmpty()

    fun addSharedProfile(profile: RegistryProfile) {
        sharedProfiles[profile.id] = profile
    }

    fun addSharedEntryProfile(entryId: String, profile: RegistryProfile) {
        sharedEntryProfiles.getOrPut(entryId) { mutableListOf() }.add(profile)
    }

    fun addSharedUnit(unit: ParsedUnit) {
        sharedUnits[unit.id] = unit
    }

    data class RegistryProfile(
        val id: String,
        val name: String,
        val typeName: String,
        val characteristics: Map<String, String>,
    )
}
