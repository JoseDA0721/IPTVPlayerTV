package com.example.iptvplayertv.data.remote

import com.example.iptvplayertv.data.model.LiveChannel
import com.example.iptvplayertv.data.model.VodItem
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

    // Obtener lista de canales en vivo
    @GET
    suspend fun getLiveStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_live_streams"
    ): Response<List<LiveChannel>>

    // Obtener lista de películas (VOD)
    @GET
    suspend fun getVodStreams(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_vod_streams"
    ): Response<List<VodItem>>

    // Obtener lista de series
    @GET
    suspend fun getSeries(
        @Url url: String,
        @Query("username") username: String,
        @Query("password") password: String,
        @Query("action") action: String = "get_series"
    ): Response<List<VodItem>>
}