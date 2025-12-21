package com.example.iptvplayertv.data.remote

import com.example.iptvplayertv.data.model.XtreamAuthResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

interface XtreamApi {

    @GET
    suspend fun authenticate(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String
    ): Response<XtreamAuthResponse>
}