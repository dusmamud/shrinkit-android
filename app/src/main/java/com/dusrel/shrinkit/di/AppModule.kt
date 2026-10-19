package com.dusrel.shrinkit.di

import android.content.Context
import androidx.room.Room
import com.dusrel.shrinkit.data.local.HistoryDao
import com.dusrel.shrinkit.data.local.HistoryDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideHistoryDatabase(
        @ApplicationContext context: Context
    ): HistoryDatabase {
        return Room.databaseBuilder(
            context,
            HistoryDatabase::class.java,
            "shrinkit_history.db"
        ).build()
    }

    @Provides
    fun provideHistoryDao(
        database: HistoryDatabase
    ): HistoryDao = database.historyDao()
}
