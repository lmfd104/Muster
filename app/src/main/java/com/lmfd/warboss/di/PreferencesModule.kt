package com.lmfd.warboss.di

import android.content.Context
import com.lmfd.warboss.data.prefs.ImportPrefs
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {

    @Provides
    @Singleton
    fun provideImportPrefs(@ApplicationContext context: Context): ImportPrefs =
        ImportPrefs(
            context.getSharedPreferences("warboss_import_prefs", Context.MODE_PRIVATE)
        )
}
