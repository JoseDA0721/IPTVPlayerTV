package com.example.iptvplayertv.presentation.home

data class HomeState(
    val userInfo: UserDisplayInfo? = null,
    val lastUpdate: String = "",
    val expirationDate: String = "",
    val liveChannelsCount: Int = 0,
    val moviesCount: Int = 0,
    val seriesCount: Int = 0,
    val isLoading: Boolean = false
)

data class UserDisplayInfo(
    val username: String,
    val expDate: String,
    val status: String
)

sealed class HomeSection(val route: String, val title: String) {
    object LiveTV : HomeSection("live_tv", "TV DIRECTO")
    object Movies : HomeSection("movies", "MOVIES")
    object Series : HomeSection("series", "SERIES")
}