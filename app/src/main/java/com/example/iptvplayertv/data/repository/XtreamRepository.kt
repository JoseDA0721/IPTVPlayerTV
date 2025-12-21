package com.example.iptvplayertv.data.repository

import com.example.iptvplayertv.data.model.XtreamAuthResponse
import com.example.iptvplayertv.data.remote.XtreamApi
import javax.inject.Inject

interface XtreamRepository {
    suspend fun login(host: String, user: String, pass: String): Result<XtreamAuthResponse>
}

