package com.example.iptvplayertv.di

import android.content.Context
import androidx.room.Room
import com.example.iptvplayertv.data.local.AppDatabase
import com.example.iptvplayertv.data.local.dao.*
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
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "iptv_database"
        )
            .fallbackToDestructiveMigration(false) // ⚠️ Solo para desarrollo
            .build()
    }

    @Provides
    @Singleton
    fun provideContentCountsDao(db: AppDatabase): ContentCountsDao {
        return db.contentCountsDao()
    }

    @Provides
    @Singleton
    fun provideAccountInfoDao(db: AppDatabase): AccountInfoDao {
        return db.accountInfoDao()
    }

    @Provides
    @Singleton
    fun provideLiveCategoriesDao(db: AppDatabase): LiveCategoriesDao {
        return db.liveCategoriesDao()
    }

    @Provides
    @Singleton
    fun provideLiveChannelsDao(db: AppDatabase): LiveChannelsDao {
        return db.liveChannelsDao()
    }

    @Provides
    @Singleton
    fun provideSessionDao(db: AppDatabase): SessionDao {
        return db.sessionDao()
    }
}