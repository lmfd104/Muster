package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "unit",
    foreignKeys = [
        ForeignKey(
            entity = FactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["factionId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("factionId")],
)
data class UnitEntity(
    @PrimaryKey val id: String,
    val factionId: String,
    val name: String,
    /** "unit", "model", or "upgrade" */
    val type: String,
    val points: Int,
    val minQuantity: Int,
    val maxQuantity: Int,
    /** true when infoLink/entryLink targets could not be resolved during import */
    val hasUnresolvableLinks: Boolean = false,
)
