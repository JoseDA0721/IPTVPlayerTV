package com.example.iptvplayertv.data.local.dao

import androidx.room.*
import com.example.iptvplayertv.data.local.entities.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ContentCountsDao {
    @Query("SELECT * FROM content_counts WHERE id = 1")
    suspend fun getCounts(): ContentCountsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCounts(counts: ContentCountsEntity)

    @Query("DELETE FROM content_counts")
    suspend fun clearCounts()
}

@Dao
interface AccountInfoDao {
    @Query("SELECT * FROM account_info WHERE username = :username LIMIT 1")
    suspend fun getAccountInfo(username: String): AccountInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccountInfo(info: AccountInfoEntity)

    @Query("DELETE FROM account_info")
    suspend fun clearAccountInfo()
}

@Dao
interface LiveCategoriesDao {
    @Query("SELECT * FROM live_categories WHERE host = :host ORDER BY categoryName")
    suspend fun getCategories(host: String): List<LiveCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<LiveCategoryEntity>)

    @Query("DELETE FROM live_categories WHERE host = :host")
    suspend fun clearCategories(host: String)
}

@Dao
interface LiveChannelsDao {
    @Query("""
        SELECT * FROM live_channels 
        WHERE categoryId = :categoryId AND host = :host 
        ORDER BY num
    """)
    suspend fun getChannelsByCategory(categoryId: String, host: String): List<LiveChannelEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChannels(channels: List<LiveChannelEntity>)

    @Query("""
        UPDATE live_channels 
        SET accessCount = accessCount + 1 
        WHERE categoryId = :categoryId
    """)
    suspend fun incrementAccessCount(categoryId: String)

    // ✅ LRU: Eliminar categorías menos usadas cuando hay más de 10
    @Query("""
        DELETE FROM live_channels 
        WHERE categoryId IN (
            SELECT DISTINCT categoryId FROM live_channels 
            ORDER BY accessCount ASC, timestamp ASC 
            LIMIT (SELECT COUNT(DISTINCT categoryId) FROM live_channels) - 10
        )
    """)
    suspend fun pruneLeastUsedCategories()

    @Query("DELETE FROM live_channels WHERE host = :host")
    suspend fun clearChannels(host: String)
}

@Dao
interface SessionDao {
    @Query("SELECT * FROM session WHERE id = 1")
    fun getSession(): Flow<SessionEntity?>

    @Query("SELECT * FROM session WHERE id = 1")
    suspend fun getSessionOnce(): SessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSession(session: SessionEntity)

    @Query("DELETE FROM session")
    suspend fun clearSession()
}