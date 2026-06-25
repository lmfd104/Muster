package com.lmfd.warboss.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.lmfd.warboss.data.db.entity.CategoryLinkEntity
import com.lmfd.warboss.data.db.entity.CharacteristicEntity
import com.lmfd.warboss.data.db.entity.KeywordEntity
import com.lmfd.warboss.data.db.entity.ProfileEntity

@Dao
interface ProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfiles(entities: List<ProfileEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCharacteristics(entities: List<CharacteristicEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKeywords(entities: List<KeywordEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategoryLinks(entities: List<CategoryLinkEntity>)

    @Query("SELECT * FROM profile WHERE entryId = :entryId")
    suspend fun listByEntryId(entryId: String): List<ProfileEntity>

    @Query("SELECT * FROM characteristic WHERE profileId = :profileId")
    suspend fun listCharacteristics(profileId: String): List<CharacteristicEntity>

    @Query("SELECT * FROM keyword WHERE unitId = :unitId ORDER BY isFactionKeyword DESC, keyword ASC")
    suspend fun listKeywords(unitId: String): List<KeywordEntity>

    @Query("SELECT * FROM category_link WHERE unitId = :unitId")
    suspend fun listCategoryLinks(unitId: String): List<CategoryLinkEntity>

    @Query("DELETE FROM profile")
    suspend fun deleteAllProfiles()

    @Query("DELETE FROM characteristic")
    suspend fun deleteAllCharacteristics()

    @Query("DELETE FROM keyword")
    suspend fun deleteAllKeywords()

    @Query("DELETE FROM category_link")
    suspend fun deleteAllCategoryLinks()
}
