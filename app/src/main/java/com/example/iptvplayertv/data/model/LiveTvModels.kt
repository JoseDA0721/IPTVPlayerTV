package com.example.iptvplayertv.data.model

import com.google.gson.annotations.SerializedName

// Modelo para Categorías de Canales
data class LiveCategory(
    @SerializedName("category_id")
    val categoryId: String,
    @SerializedName("category_name")
    val categoryName: String,
    @SerializedName("parent_id")
    val parentId: Int?
)

// Modelo extendido para LiveChannel con más información
data class LiveChannelDetail(
    @SerializedName("num")
    val num: Int,
    @SerializedName("name")
    val name: String,
    @SerializedName("stream_type")
    val streamType: String,
    @SerializedName("stream_id")
    val streamId: Int,
    @SerializedName("stream_icon")
    val streamIcon: String?,
    @SerializedName("epg_channel_id")
    val epgChannelId: String?,
    @SerializedName("added")
    val added: String?,
    @SerializedName("category_id")
    val categoryId: String?,
    @SerializedName("category_name")
    val categoryName: String?,
    @SerializedName("custom_sid")
    val customSid: String?,
    @SerializedName("tv_archive")
    val tvArchive: Int?,
    @SerializedName("direct_source")
    val directSource: String?,
    @SerializedName("tv_archive_duration")
    val tvArchiveDuration: Int?
) {
    // URL para reproducir el canal
    fun getStreamUrl(host: String, username: String, password: String, extension: String = "m3u8"): String {
        val cleanHost = host.trim().removeSuffix("/")
        return "$cleanHost/live/$username/$password/$streamId.$extension"
    }
}

// Estado de carga para UI
sealed class LiveTvLoadState {
    object Idle : LiveTvLoadState()
    object Loading : LiveTvLoadState()
    data class Success(val message: String = "") : LiveTvLoadState()
    data class Error(val message: String) : LiveTvLoadState()
}