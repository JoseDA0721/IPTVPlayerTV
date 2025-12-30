package com.example.iptvplayertv.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * ✅ Tabla para almacenar contadores de contenido
 */
@Entity(tableName = "content_counts")
data class ContentCountsEntity(
    @PrimaryKey val id: Int = 1, // Solo guardamos un registro
    val liveChannels: Int,
    val movies: Int,
    val series: Int,
    val timestamp: Long,
    val host: String // Para invalidar si cambia el servidor
)

/**
 * ✅ Tabla para información de cuenta
 */
@Entity(tableName = "account_info")
data class AccountInfoEntity(
    @PrimaryKey val username: String,
    val host: String,
    val expDate: String?,
    val createdAt: String?,
    val status: String,
    val isTrial: Boolean,
    val activeConnections: Int,
    val maxConnections: Int,
    val timezone: String?,
    val timestamp: Long
)

/**
 * ✅ Tabla para categorías de LiveTV
 */
@Entity(tableName = "live_categories")
data class LiveCategoryEntity(
    @PrimaryKey val categoryId: String,
    val categoryName: String,
    val parentId: Int?,
    val timestamp: Long,
    val host: String
)

/**
 * ✅ Tabla para canales (con límite LRU)
 */
@Entity(
    tableName = "live_channels",
    primaryKeys = ["streamId", "categoryId"]
)
data class LiveChannelEntity(
    val streamId: Int,
    val num: Int,
    val name: String,
    val streamType: String,
    val streamIcon: String?,
    val epgChannelId: String?,
    val categoryId: String,
    val categoryName: String?,
    val tvArchive: Int?,
    val tvArchiveDuration: Int?,
    val timestamp: Long,
    val host: String,
    val accessCount: Int = 0 // Para LRU
)

/**
 * ✅ Tabla para controlar sesión
 */
@Entity(tableName = "session")
data class SessionEntity(
    @PrimaryKey val id: Int = 1,
    val isLoggedIn: Boolean,
    val lastLoginTimestamp: Long,
    val autoLoginEnabled: Boolean = true
)