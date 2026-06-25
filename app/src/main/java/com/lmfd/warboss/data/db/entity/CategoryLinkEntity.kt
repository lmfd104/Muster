package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Battlefield role or keyword category linked to a unit (Battleline, Character, etc.) */
@Entity(
    tableName = "category_link",
    indices = [Index("unitId")],
)
data class CategoryLinkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: String,
    val categoryId: String,
    val categoryName: String,
    val isPrimary: Boolean,
)
