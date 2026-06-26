package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "army_list_unit",
    foreignKeys = [
        ForeignKey(
            entity = ArmyListEntity::class,
            parentColumns = ["id"],
            childColumns = ["armyListId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("armyListId")],
)
data class ArmyListUnitEntity(
    @PrimaryKey val id: String,
    val armyListId: String,
    val unitId: String,
    val quantity: Int = 1,
    val importedName: String? = null,
    val importedPoints: Int? = null,
)
