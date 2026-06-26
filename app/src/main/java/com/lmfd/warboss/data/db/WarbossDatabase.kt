package com.lmfd.warboss.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.lmfd.warboss.data.db.dao.ArmyListDao
import com.lmfd.warboss.data.db.dao.FactionDao
import com.lmfd.warboss.data.db.dao.GameSystemDao
import com.lmfd.warboss.data.db.dao.ProfileDao
import com.lmfd.warboss.data.db.dao.UnitDao
import com.lmfd.warboss.data.db.entity.ArmyListEntity
import com.lmfd.warboss.data.db.entity.ArmyListUnitEntity
import com.lmfd.warboss.data.db.entity.CategoryLinkEntity
import com.lmfd.warboss.data.db.entity.CharacteristicEntity
import com.lmfd.warboss.data.db.entity.FactionEntity
import com.lmfd.warboss.data.db.entity.GameSystemEntity
import com.lmfd.warboss.data.db.entity.KeywordEntity
import com.lmfd.warboss.data.db.entity.ProfileEntity
import com.lmfd.warboss.data.db.entity.UnitEntity

@Database(
    version = 3,
    exportSchema = true,
    entities = [
        GameSystemEntity::class,
        FactionEntity::class,
        UnitEntity::class,
        ProfileEntity::class,
        CharacteristicEntity::class,
        KeywordEntity::class,
        CategoryLinkEntity::class,
        ArmyListEntity::class,
        ArmyListUnitEntity::class,
    ],
)
abstract class WarbossDatabase : RoomDatabase() {
    abstract fun gameSystemDao(): GameSystemDao
    abstract fun factionDao(): FactionDao
    abstract fun unitDao(): UnitDao
    abstract fun profileDao(): ProfileDao
    abstract fun armyListDao(): ArmyListDao

    companion object {
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE army_list_unit ADD COLUMN importedName TEXT")
                db.execSQL("ALTER TABLE army_list_unit ADD COLUMN importedPoints INTEGER")
            }
        }

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS army_list (
                        id TEXT NOT NULL PRIMARY KEY,
                        name TEXT NOT NULL,
                        factionId TEXT NOT NULL,
                        factionName TEXT NOT NULL,
                        pointsLimit INTEGER NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS army_list_unit (
                        id TEXT NOT NULL PRIMARY KEY,
                        armyListId TEXT NOT NULL,
                        unitId TEXT NOT NULL,
                        quantity INTEGER NOT NULL,
                        FOREIGN KEY (armyListId) REFERENCES army_list(id) ON DELETE CASCADE
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS index_army_list_unit_armyListId ON army_list_unit(armyListId)"
                )
            }
        }
    }
}
