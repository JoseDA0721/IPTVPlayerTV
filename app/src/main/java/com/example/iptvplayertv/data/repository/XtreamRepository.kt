
package com.example.iptvplayertv.data.repository

import android.util.Log
import com.example.iptvplayertv.data.local.dao.AccountInfoDao
import com.example.iptvplayertv.data.local.dao.ContentCountsDao
import com.example.iptvplayertv.data.local.entities.AccountInfoEntity
import com.example.iptvplayertv.data.local.entities.ContentCountsEntity
import com.example.iptvplayertv.data.model.XtreamAuthResponse
import com.example.iptvplayertv.data.remote.XtreamApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

interface XtreamRepository {
    suspend fun login(host: String, user: String, pass: String): Result<XtreamAuthResponse>
    suspend fun getLiveChannelsCount(host: String, user: String, pass: String): Result<Int>
    suspend fun getMoviesCount(host: String, user: String, pass: String): Result<Int>
    suspend fun getSeriesCount(host: String, user: String, pass: String): Result<Int>
    suspend fun getAccountInfo(host: String, user: String, pass: String): Result<XtreamAuthResponse>

}

/**
 * Data class para almacenar contadores en caché
 */
data class ContentCounts(
    val liveChannels: Int = 0,
    val movies: Int = 0,
    val series: Int = 0,
    val timestamp: Long = 0L
)

class XtreamRepositoryImp @Inject constructor(
    private val api: XtreamApi,
    private val contentCountsDao: ContentCountsDao,
    private val accountInfoDao: AccountInfoDao
) : XtreamRepository {

    companion object {
        private const val TAG = "XtreamRepository"
        private const val CACHE_DURATION_MS = 10 * 60 * 1000L // 10 minutos
        private const val ACCOUNT_CACHE_DURATION_MS = 5 * 60 * 1000L
    }

    private val cacheMutex = Mutex()
    private var memoryCountsCache: ContentCounts? = null
    private var memoryAccountCache: Pair<Long, XtreamAuthResponse>? = null

    override suspend fun login(
        host: String,
        user: String,
        pass: String
    ): Result<XtreamAuthResponse> {
        return try {
            val cleanHost = host.trim().removeSuffix("/")
            val authUrl = "$cleanHost/player_api.php"

            Log.d(TAG, "Intentando conectar a: $authUrl")

            val response = api.authenticate(
                url = authUrl,
                username = user,
                password = pass
            )

            if (response.isSuccessful) {
                val body = response.body()

                if (body != null && body.userInfo.status.equals("Active", ignoreCase = true)) {
                    // ✅ Guardar en Room
                    saveAccountToDatabase(host, user, body)

                    // ✅ Guardar en caché memoria
                    cacheMutex.withLock {
                        memoryAccountCache = Pair(System.currentTimeMillis(), body)
                    }

                    Result.success(body)
                } else if (body != null) {
                    Result.failure(Exception("Usuario inactivo o credenciales inválidas"))
                } else {
                    Result.failure(Exception("Respuesta del servidor vacía"))
                }
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error en login", e)
            Result.failure(e)
        }
    }

    /**
     * ✅ CLAVE: Estrategia de 3 niveles
     * 1. Memoria (más rápido)
     * 2. Room Database (medio)
     * 3. API (más lento)
     */
    suspend fun getAllCounts(
        host: String,
        user: String,
        pass: String,
        forceRefresh: Boolean = false
    ): Result<ContentCounts> = coroutineScope {
        try {
            // ✅ NIVEL 1: Memoria
            if (!forceRefresh) {
                cacheMutex.withLock {
                    memoryCountsCache?.let { cached ->
                        val age = System.currentTimeMillis() - cached.timestamp
                        if (age < CACHE_DURATION_MS) {
                            Log.d(TAG, "✓ Contadores desde MEMORIA (${age / 1000}s)")
                            return@coroutineScope Result.success(cached)
                        }
                    }
                }
            }

            // ✅ NIVEL 2: Room Database
            if (!forceRefresh) {
                val dbCounts = contentCountsDao.getCounts()
                if (dbCounts != null && dbCounts.host == host) {
                    val age = System.currentTimeMillis() - dbCounts.timestamp
                    if (age < CACHE_DURATION_MS) {
                        val counts = ContentCounts(
                            liveChannels = dbCounts.liveChannels,
                            movies = dbCounts.movies,
                            series = dbCounts.series,
                            timestamp = dbCounts.timestamp
                        )

                        // Actualizar memoria
                        cacheMutex.withLock {
                            memoryCountsCache = counts
                        }

                        Log.d(TAG, "✓ Contadores desde DATABASE (${age / 1000}s)")
                        return@coroutineScope Result.success(counts)
                    }
                }
            }

            // ✅ NIVEL 3: API (en paralelo)
            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo contadores desde API...")

            val liveDeferred = async {
                api.getLiveStreams(url, user, pass)
            }
            val moviesDeferred = async {
                api.getVodStreams(url, user, pass)
            }
            val seriesDeferred = async {
                api.getSeries(url, user, pass)
            }

            val liveResponse = liveDeferred.await()
            val moviesResponse = moviesDeferred.await()
            val seriesResponse = seriesDeferred.await()

            val counts = ContentCounts(
                liveChannels = if (liveResponse.isSuccessful) {
                    liveResponse.body()?.size ?: 0
                } else 0,
                movies = if (moviesResponse.isSuccessful) {
                    moviesResponse.body()?.size ?: 0
                } else 0,
                series = if (seriesResponse.isSuccessful) {
                    seriesResponse.body()?.size ?: 0
                } else 0,
                timestamp = System.currentTimeMillis()
            )

            // ✅ Guardar en ambos cachés
            saveCountsToDatabase(host, counts)
            cacheMutex.withLock {
                memoryCountsCache = counts
            }

            Log.d(TAG, "✓ Contadores desde API y guardados")
            Result.success(counts)

        } catch (e: Exception) {
            Log.e(TAG, "✗ Error obteniendo contadores", e)

            // ✅ FALLBACK: Intentar Database aunque esté expirado
            val dbCounts = contentCountsDao.getCounts()
            if (dbCounts != null) {
                val counts = ContentCounts(
                    liveChannels = dbCounts.liveChannels,
                    movies = dbCounts.movies,
                    series = dbCounts.series,
                    timestamp = dbCounts.timestamp
                )
                Log.w(TAG, "Usando caché DATABASE antiguo por error")
                return@coroutineScope Result.success(counts)
            }

            // ✅ FALLBACK 2: Memoria
            cacheMutex.withLock {
                memoryCountsCache?.let {
                    Log.w(TAG, "Usando caché MEMORIA antiguo por error")
                    return@coroutineScope Result.success(it)
                }
            }

            Log.e(TAG, "Fallo total obteniendo contadores. Retornando valores vacíos para evitar crash.")
            Result.success(ContentCounts(0, 0, 0, System.currentTimeMillis()))
        }
    }

    override suspend fun getLiveChannelsCount(
        host: String,
        user: String,
        pass: String
    ): Result<Int> {
        return getAllCounts(host, user, pass).map { it.liveChannels }
    }

    override suspend fun getMoviesCount(
        host: String,
        user: String,
        pass: String
    ): Result<Int> {
        return getAllCounts(host, user, pass).map { it.movies }
    }

    override suspend fun getSeriesCount(
        host: String,
        user: String,
        pass: String
    ): Result<Int> {
        return getAllCounts(host, user, pass).map { it.series }
    }

    override suspend fun getAccountInfo(
        host: String,
        user: String,
        pass: String
    ): Result<XtreamAuthResponse> {
        return try {
            // ✅ NIVEL 1: Memoria
            cacheMutex.withLock {
                memoryAccountCache?.let { cached ->
                    val age = System.currentTimeMillis() - cached.first
                    if (age < ACCOUNT_CACHE_DURATION_MS) {
                        Log.d(TAG, "✓ Account desde MEMORIA")
                        return Result.success(cached.second)
                    }
                }
            }

            // ✅ NIVEL 2: Database
            val dbAccount = accountInfoDao.getAccountInfo(user)
            if (dbAccount != null) {
                val age = System.currentTimeMillis() - dbAccount.timestamp
                if (age < ACCOUNT_CACHE_DURATION_MS) {
                    // Reconstruir XtreamAuthResponse desde Entity
                    // (Simplificado - necesitas mapear correctamente)
                    Log.d(TAG, "✓ Account desde DATABASE")
                    // TODO: Implementar mapper completo
                }
            }

            // ✅ NIVEL 3: API
            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            val response = api.getAccountsInfo(url, user, pass)

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                saveAccountToDatabase(host, user, body)
                cacheMutex.withLock {
                    memoryAccountCache = Pair(System.currentTimeMillis(), body)
                }

                Log.d(TAG, "✓ Account desde API y guardado")
                Result.success(body)
            } else {
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Error", e)
            Result.failure(e)
        }
    }

    private suspend fun saveCountsToDatabase(host: String, counts: ContentCounts) {
        contentCountsDao.insertCounts(
            ContentCountsEntity(
                id = 1,
                liveChannels = counts.liveChannels,
                movies = counts.movies,
                series = counts.series,
                timestamp = counts.timestamp,
                host = host
            )
        )
    }

    private suspend fun saveAccountToDatabase(
        host: String,
        username: String,
        response: XtreamAuthResponse
    ) {
        accountInfoDao.insertAccountInfo(
            AccountInfoEntity(
                username = username,
                host = host,
                expDate = response.userInfo.expDate,
                createdAt = response.userInfo.createdAt,
                status = response.userInfo.status,
                isTrial = response.userInfo.isTrial == "1",
                activeConnections = response.userInfo.activeCons?.toIntOrNull() ?: 0,
                maxConnections = response.userInfo.maxConnections?.toIntOrNull() ?: 0,
                timezone = response.serverInfo.timezone,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearAllCache() {
        cacheMutex.withLock {
            memoryCountsCache = null
            memoryAccountCache = null
        }
        contentCountsDao.clearCounts()
        accountInfoDao.clearAccountInfo()
        Log.d(TAG, "✓ Caché completo limpiado (Memoria + Database)")
    }
}