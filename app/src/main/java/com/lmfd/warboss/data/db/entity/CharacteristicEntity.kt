package com.lmfd.warboss.data.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** EAV row: one characteristic (e.g. M=6", T=4, SV=3+) within a profile. */
@Entity(
    tableName = "characteristic",
    indices = [Index("profileId")],
)
data class CharacteristicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: String,
    val name: String,
    val value: String,
)
