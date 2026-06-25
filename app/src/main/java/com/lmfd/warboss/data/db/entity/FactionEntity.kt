package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "faction",
    foreignKeys = [
        ForeignKey(
            entity = GameSystemEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameSystemId"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("gameSystemId")],
)
data class FactionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val revision: Int,
    val gameSystemId: String,
    /** true for Library .cat files (library="true" in XML); excluded from user-facing faction list */
    val isLibrary: Boolean,
)
