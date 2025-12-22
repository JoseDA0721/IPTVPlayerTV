package com.example.iptvplayertv.data.repository

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface XtreamApiService {
    @GET("{host}/player_api.php")
    suspend fun getLiveStreams(
        @Path("host", encoded = true) host: String,
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_live_streams"
    ): List<Any> // Replace Any with your data model if you have one

    @GET("{host}/player_api.php")
    suspend fun getMovies(
        @Path("host", encoded = true) host: String,
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_vod_streams"
    ): List<Any>

    @GET("{host}/player_api.php")
    suspend fun getSeries(
        @Path("host", encoded = true) host: String,
        @Query("username") user: String,
        @Query("password") pass: String,
        @Query("action") action: String = "get_series"
    ): List<Any>
}
