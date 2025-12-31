package com.example.iptvplayertv.data.repository

import android.util.Log
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.remote.XtreamApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
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
    fun getCachedChannelsForCategory(categoryId: String): List<LiveChannelDetail>?
    fun clearCache()
}

@Singleton
class LiveTvRepositoryImpl @Inject constructor(
    private val api: XtreamApi
) : LiveTvRepository {

    companion object {
        private const val TAG = "LiveTvRepository"
        private const val CACHE_DURATION_MS = 10 * 60 * 1000L // 10 minutos
        private const val MAX_CACHED_CATEGORIES = 10 // Máximo de categorías en caché
    }

    // ✅ Mutex para thread-safety
    private val cacheMutex = Mutex()

    // Caché de categorías
    private var categoriesCache: Pair<Long, List<LiveCategory>>? = null

    // ✅ OPTIMIZACIÓN: LRU Cache para canales (elimina las más antiguas)
    private val channelsCache = object : LinkedHashMap<String, Pair<Long, List<LiveChannelDetail>>>(
        MAX_CACHED_CATEGORIES,
        0.75f,
        true // accessOrder = true (LRU)
    ) {
        override fun removeEldestEntry(
            eldest: MutableMap.MutableEntry<String, Pair<Long, List<LiveChannelDetail>>>?
        ): Boolean {
            return size > MAX_CACHED_CATEGORIES
        }
    }

    override suspend fun getCategories(
        host: String,
        user: String,
        pass: String
    ): Result<List<LiveCategory>> {
        return try {
            // ✅ Thread-safe cache check
            cacheMutex.withLock {
                val cached = categoriesCache
                if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
                    Log.d(TAG, "✓ Categorías desde caché (${cached.second.size})")
                    return Result.success(cached.second)
                }
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

                // ✅ Thread-safe cache update
                cacheMutex.withLock {
                    categoriesCache = Pair(System.currentTimeMillis(), categories)
                }

                Log.d(TAG, "✓ Categorías obtenidas: ${categories.size}")
                Result.success(categories)
            } else {
                Log.e(TAG, "✗ Error obteniendo categorías: ${response.code()}")

                // ✅ Retornar caché antigua si hay error
                cacheMutex.withLock {
                    categoriesCache?.let {
                        Log.w(TAG, "Retornando caché antigua debido a error")
                        return Result.success(it.second)
                    }
                }

                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo categorías", e)

            // ✅ Retornar caché antigua en caso de excepción
            cacheMutex.withLock {
                categoriesCache?.let {
                    Log.w(TAG, "Retornando caché antigua debido a excepción")
                    return Result.success(it.second)
                }
            }

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
            // ✅ Thread-safe cache check
            cacheMutex.withLock {
                val cached = channelsCache[categoryId]
                if (cached != null && System.currentTimeMillis() - cached.first < CACHE_DURATION_MS) {
                    Log.d(TAG, "✓ Canales desde caché para categoría $categoryId (${cached.second.size})")
                    return Result.success(cached.second)
                }
            }

            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo canales de categoría $categoryId desde servidor...")

            val response = api.getLiveStreamsByCategory(
                url = url,
                username = user,
                password = pass,
                categoryId = categoryId
            )

            if (response.isSuccessful) {
                val channels = response.body() ?: emptyList()

                // ✅ Thread-safe cache update
                cacheMutex.withLock {
                    channelsCache[categoryId] = Pair(System.currentTimeMillis(), channels)
                }

                Log.d(TAG, "✓ Canales obtenidos: ${channels.size}")
                Result.success(channels)
            } else {
                Log.e(TAG, "✗ Error obteniendo canales: ${response.code()}")

                // ✅ Retornar caché antigua si hay error
                cacheMutex.withLock {
                    channelsCache[categoryId]?.let {
                        Log.w(TAG, "Retornando caché antigua debido a error")
                        return Result.success(it.second)
                    }
                }

                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo canales", e)

            // ✅ Retornar caché antigua en caso de excepción
            cacheMutex.withLock {
                channelsCache[categoryId]?.let {
                    Log.w(TAG, "Retornando caché antigua debido a excepción")
                    return Result.success(it.second)
                }
            }

            if (e is java.io.EOFException || e is com.google.gson.stream.MalformedJsonException) {
                Log.w(TAG, "⚠ Error de datos (EOF/Json) en categoría $categoryId. Retornando lista vacía para evitar crash.")
                return Result.success(emptyList())
            }

            return Result.success(emptyList())

            //Result.failure(e)
        }
    }

    /**
     * ✅ NUEVO: Obtener canales desde caché sin esperar
     * Útil para verificar si ya tenemos datos antes de hacer una llamada
     */
    override fun getCachedChannelsForCategory(categoryId: String): List<LiveChannelDetail>? {
        val cached = channelsCache[categoryId] ?: return null
        val age = System.currentTimeMillis() - cached.first

        return if (age < CACHE_DURATION_MS) {
            Log.d(TAG, "✓ Canales en caché para $categoryId (edad: ${age / 1000}s)")
            cached.second
        } else {
            Log.d(TAG, "✗ Caché expirado para $categoryId")
            null
        }
    }

    override fun clearCache() {
        categoriesCache = null
        channelsCache.clear()
        Log.d(TAG, "✓ Caché limpiado")
    }

    /**
     * ✅ NUEVO: Obtener estadísticas de caché
     */
    fun getCacheStats(): Map<String, Any> {
        val categoriesAge = categoriesCache?.let {
            (System.currentTimeMillis() - it.first) / 1000
        }

        val channelsCacheInfo = channelsCache.mapValues { (_, value) ->
            mapOf(
                "count" to value.second.size,
                "age_seconds" to (System.currentTimeMillis() - value.first) / 1000
            )
        }

        return mapOf(
            "categories_age_seconds" to (categoriesAge ?: "no cache"),
            "categories_count" to (categoriesCache?.second?.size ?: 0),
            "channels_cache_size" to channelsCache.size,
            "channels_details" to channelsCacheInfo
        )
    }
}