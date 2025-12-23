package com.example.iptvplayertv.data.repository

import android.util.Log
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.remote.XtreamApi
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

interface LiveTvRepository {
    suspend fun getCategories(host: String, user: String, pass: String): Result<List<LiveCategory>>
    suspend fun getChannelsByCategory(
        host: String,
        user: String,
        pass: String,
        categoryId: String
    ): Result<List<LiveChannelDetail>>
    fun clearCache()
}

@Singleton
class LiveTvRepositoryImpl @Inject constructor(
    private val api: XtreamApi
) : LiveTvRepository {

    companion object {
        private const val TAG = "LiveTvRepository"
        private const val CACHE_DURATION_MS = 5 * 60 * 1000L // 5 minutos
    }

    // Caché en memoria
    private var categoriesCache: Pair<Long, List<LiveCategory>>? = null
    private val channelsCache = mutableMapOf<String, Pair<Long, List<LiveChannelDetail>>>()

    override suspend fun getCategories(
        host: String,
        user: String,
        pass: String
    ): Result<List<LiveCategory>> {
        return try {
            // Verificar caché
            val cached = categoriesCache
            if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
                Log.d(TAG, "✓ Categorías desde caché (${cached.second.size})")
                return Result.success(cached.second)
            }

            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo categorías desde servidor...")

            val response = api.getLiveCategories(
                url = url,
                username = user,
                password = pass
            )

            if (response.isSuccessful) {
                val categories = response.body() ?: emptyList()

                // Guardar en caché
                categoriesCache = Pair(System.currentTimeMillis(), categories)

                Log.d(TAG, "✓ Categorías obtenidas: ${categories.size}")
                Result.success(categories)
            } else {
                Log.e(TAG, "✗ Error obteniendo categorías: ${response.code()}")
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo categorías", e)
            Result.failure(e)
        }
    }

    override suspend fun getChannelsByCategory(
        host: String,
        user: String,
        pass: String,
        categoryId: String
    ): Result<List<LiveChannelDetail>> {
        return try {
            // Verificar caché
            val cached = channelsCache[categoryId]
            if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
                Log.d(TAG, "✓ Canales desde caché para categoría $categoryId (${cached.second.size})")
                return Result.success(cached.second)
            }

            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo canales de categoría $categoryId...")

            val response = api.getLiveStreamsByCategory(
                url = url,
                username = user,
                password = pass,
                categoryId = categoryId
            )

            if (response.isSuccessful) {
                val channels = response.body() ?: emptyList()

                // Guardar en caché
                channelsCache[categoryId] = Pair(System.currentTimeMillis(), channels)

                Log.d(TAG, "✓ Canales obtenidos: ${channels.size}")
                Result.success(channels)
            } else {
                Log.e(TAG, "✗ Error obteniendo canales: ${response.code()}")
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo canales", e)
            Result.failure(e)
        }
    }

    override fun clearCache() {
        categoriesCache = null
        channelsCache.clear()
        Log.d(TAG, "✓ Caché limpiado")
    }
}