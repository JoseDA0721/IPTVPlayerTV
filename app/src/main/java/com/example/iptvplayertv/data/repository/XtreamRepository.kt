package com.example.iptvplayertv.data.repository

import android.util.Log
import com.example.iptvplayertv.data.model.XtreamAuthResponse
import com.example.iptvplayertv.data.remote.XtreamApi
import javax.inject.Inject

interface XtreamRepository {
    suspend fun login(host: String, user: String, pass: String): Result<XtreamAuthResponse>
    suspend fun getLiveChannelsCount(host: String, user: String, pass: String): Result<Int>
    suspend fun getMoviesCount(host: String, user: String, pass: String): Result<Int>
    suspend fun getSeriesCount(host: String, user: String, pass: String): Result<Int>
}

class XtreamRepositoryImpl @Inject constructor(
    private val api: XtreamApi
) : XtreamRepository {

    companion object {
        private const val TAG = "XtreamRepository"
    }

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
            Result.failure(e)
        }
    }

    override suspend fun getLiveChannelsCount(
        host: String,
        user: String,
        pass: String
    ): Result<Int> {
        return try {
            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo canales en vivo...")

            val response = api.getLiveStreams(
                url = url,
                username = user,
                password = pass
            )

            if (response.isSuccessful) {
                val count = response.body()?.size ?: 0
                Log.d(TAG, "✓ Canales en vivo: $count")
                Result.success(count)
            } else {
                Log.e(TAG, "✗ Error obteniendo canales: ${response.code()}")
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo canales", e)
            Result.success(0)
        }
    }

    override suspend fun getMoviesCount(
        host: String,
        user: String,
        pass: String
    ): Result<Int> {
        return try {
            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo películas...")

            val response = api.getVodStreams(
                url = url,
                username = user,
                password = pass
            )

            if (response.isSuccessful) {
                val count = response.body()?.size ?: 0
                Log.d(TAG, "✓ Películas: $count")
                Result.success(count)
            } else {
                Log.e(TAG, "✗ Error obteniendo películas: ${response.code()}")
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo películas", e)
            Result.success(0)
        }
    }

    override suspend fun getSeriesCount(
        host: String,
        user: String,
        pass: String
    ): Result<Int> {
        return try {
            val cleanHost = host.trim().removeSuffix("/")
            val url = "$cleanHost/player_api.php"

            Log.d(TAG, "Obteniendo series...")

            val response = api.getSeries(
                url = url,
                username = user,
                password = pass
            )

            if (response.isSuccessful) {
                val count = response.body()?.size ?: 0
                Log.d(TAG, "✓ Series: $count")
                Result.success(count)
            } else {
                Log.e(TAG, "✗ Error obteniendo series: ${response.code()}")
                Result.success(0)
            }
        } catch (e: Exception) {
            Log.e(TAG, "✗ Excepción obteniendo series", e)
            Result.success(0)
        }
    }
}