package com.lmfd.warboss.di

import android.content.Context
import androidx.room.Room
import com.lmfd.warboss.data.db.WarbossDatabase
import com.lmfd.warboss.data.db.dao.ArmyListDao
import com.lmfd.warboss.data.db.dao.FactionDao
import com.lmfd.warboss.data.db.dao.GameSystemDao
import com.lmfd.warboss.data.db.dao.ProfileDao
import com.lmfd.warboss.data.db.dao.UnitDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): WarbossDatabase =
        Room.databaseBuilder(context, WarbossDatabase::class.java, "warboss.db")
            .addMigrations(WarbossDatabase.MIGRATION_1_2)
            .build()

    @Provides
    @Singleton
    fun provideGameSystemDao(db: WarbossDatabase): GameSystemDao = db.gameSystemDao()

    @Provides
    @Singleton
    fun provideFactionDao(db: WarbossDatabase): FactionDao = db.factionDao()

    @Provides
    @Singleton
    fun provideUnitDao(db: WarbossDatabase): UnitDao = db.unitDao()

    @Provides
    @Singleton
    fun provideProfileDao(db: WarbossDatabase): ProfileDao = db.profileDao()

    @Provides
    @Singleton
    fun provideArmyListDao(db: WarbossDatabase): ArmyListDao = db.armyListDao()
}
