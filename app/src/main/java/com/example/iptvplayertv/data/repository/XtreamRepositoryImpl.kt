package com.example.iptvplayertv.data.repository

import com.example.iptvplayertv.data.model.XtreamAuthResponse
import com.example.iptvplayertv.data.remote.XtreamApi
import javax.inject.Inject

class XtreamRepositoryImpl @Inject constructor(
    private val api: XtreamApi
) : XtreamRepository {

    override suspend fun login(
        host: String,
        user: String,
        pass: String
    ): Result<XtreamAuthResponse> {
        return try {
            val cleanHost = host.trim().removeSuffix("/")
            val authUrl = "$cleanHost/player_api.php"

            android.util.Log.d("XtreamRepo", "Intentando conectar a: $authUrl")

            // 1. Get the Retrofit Response
            val response = api.authenticate(
                url = authUrl,
                username = user,
                password = pass
            )

            // 2. Check if the HTTP call was successful (2xx status code)
            if (response.isSuccessful) {
                val body = response.body()

                // 3. Check if body is not null and status is Active
                if (body != null && body.userInfo.status.equals("Active", ignoreCase = true)) {
                    Result.success(body)
                } else if (body != null) {
                    Result.failure(Exception("Usuario inactivo o credenciales inválidas"))
                } else {
                    Result.failure(Exception("Respuesta del servidor vacía"))
                }
            } else {
                // Handle HTTP errors (404, 500, etc.)
                Result.failure(Exception("Error del servidor: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
