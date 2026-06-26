package com.lmfd.warboss.di

import com.lmfd.warboss.data.bsdata.BsDataRepository
import com.lmfd.warboss.data.bsdata.BsDataRepositoryImpl
import com.lmfd.warboss.data.repository.AiAnalysisRepositoryImpl
import com.lmfd.warboss.data.repository.ArmyListRepositoryImpl
import com.lmfd.warboss.data.repository.GameResultRepositoryImpl
import com.lmfd.warboss.data.repository.UnitRepositoryImpl
import com.lmfd.warboss.domain.repository.AiAnalysisRepository
import com.lmfd.warboss.domain.repository.ArmyListRepository
import com.lmfd.warboss.domain.repository.GameResultRepository
import com.lmfd.warboss.domain.repository.UnitRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindBsDataRepository(impl: BsDataRepositoryImpl): BsDataRepository

    @Binds
    @Singleton
    abstract fun bindUnitRepository(impl: UnitRepositoryImpl): UnitRepository

    @Binds
    @Singleton
    abstract fun bindArmyListRepository(impl: ArmyListRepositoryImpl): ArmyListRepository

    @Binds
    @Singleton
    abstract fun bindAiAnalysisRepository(impl: AiAnalysisRepositoryImpl): AiAnalysisRepository

    @Binds
    @Singleton
    abstract fun bindGameResultRepository(impl: GameResultRepositoryImpl): GameResultRepository
}
