package com.example.iptvplayertv.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.iptvplayertv.data.local.dao.*
import com.example.iptvplayertv.data.local.entities.*

@Database(
    entities = [
        ContentCountsEntity::class,
        AccountInfoEntity::class,
        LiveCategoryEntity::class,
        LiveChannelEntity::class,
        SessionEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentCountsDao(): ContentCountsDao
    abstract fun accountInfoDao(): AccountInfoDao
    abstract fun liveCategoriesDao(): LiveCategoriesDao
    abstract fun liveChannelsDao(): LiveChannelsDao
    abstract fun sessionDao(): SessionDao
}