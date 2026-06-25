package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "keyword",
    indices = [Index("unitId")],
)
data class KeywordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val unitId: String,
    val keyword: String,
    val isFactionKeyword: Boolean,
)
