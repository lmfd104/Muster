package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * A stat block profile linked to a unit/model entry.
 * entryId is the ID of the owning selectionEntry (not a FK — cross-catalogue profiles
 * may belong to entries in a different .cat file from the unit that displays them).
 */
@Entity(
    tableName = "profile",
    indices = [Index("entryId")],
)
data class ProfileEntity(
    @PrimaryKey val id: String,
    /** The selectionEntry id this profile belongs to */
    val entryId: String,
    /** The faction this profile is stored under (the non-library faction for Library-sourced profiles) */
    val factionId: String,
    val name: String,
    /** e.g. "Unit", "Ranged Weapons", "Melee Weapons", "Abilities", "Transport" */
    val typeName: String,
)
