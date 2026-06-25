package com.lmfd.warboss.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lmfd.warboss.data.db.dao.FactionDao
import com.lmfd.warboss.data.db.dao.GameSystemDao
import com.lmfd.warboss.data.db.dao.ProfileDao
import com.lmfd.warboss.data.db.dao.UnitDao
import com.lmfd.warboss.data.db.entity.CategoryLinkEntity
import com.lmfd.warboss.data.db.entity.CharacteristicEntity
import com.lmfd.warboss.data.db.entity.FactionEntity
import com.lmfd.warboss.data.db.entity.GameSystemEntity
import com.lmfd.warboss.data.db.entity.KeywordEntity
import com.lmfd.warboss.data.db.entity.ProfileEntity
import com.lmfd.warboss.data.db.entity.UnitEntity

@Database(
    version = 1,
    exportSchema = true,
    entities = [
        GameSystemEntity::class,
        FactionEntity::class,
        UnitEntity::class,
        ProfileEntity::class,
        CharacteristicEntity::class,
        KeywordEntity::class,
        CategoryLinkEntity::class,
    ],
)
abstract class WarbossDatabase : RoomDatabase() {
    abstract fun gameSystemDao(): GameSystemDao
    abstract fun factionDao(): FactionDao
    abstract fun unitDao(): UnitDao
    abstract fun profileDao(): ProfileDao
}
