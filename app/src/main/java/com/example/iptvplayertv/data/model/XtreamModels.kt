package com.example.iptvplayertv.data.model

import com.google.gson.annotations.SerializedName

data class XtreamAuthResponse(
    @SerializedName("user_info")
    val userInfo: UserInfo,
    @SerializedName("server_info")
    val serverInfo: ServerInfo
)

data class UserInfo(
    val username: String,
    val password: String,
    val message: String?,
    val auth: Int,
    val status: String,
    @SerializedName("exp_date")
    val expDate: String?,
    @SerializedName("is_trial")
    val isTrial: String?,
    @SerializedName("active_cons")
    val activeCons: String?,
    @SerializedName("created_at")
    val createdAt: String?,
    @SerializedName("max_connections")
    val maxConnections: String?,
    @SerializedName("allowed_output_formats")
    val allowedOutputFormats: List<String>?
)

data class ServerInfo(
    val url: String,
    val port: String,
    @SerializedName("https_port")
    val httpsPort: String?,
    @SerializedName("server_protocol")
    val serverProtocol: String?,
    @SerializedName("rtmp_port")
    val rtmpPort: String?,
    val timestamp: String?
)

// Modelos para categorías
data class LiveChannel(
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
    @SerializedName("custom_sid")
    val customSid: String?,
    @SerializedName("tv_archive")
    val tvArchive: Int?,
    @SerializedName("direct_source")
    val directSource: String?,
    @SerializedName("tv_archive_duration")
    val tvArchiveDuration: Int?
)

data class VodItem(
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
    @SerializedName("rating")
    val rating: String?,
    @SerializedName("rating_5based")
    val rating5based: Double?,
    @SerializedName("added")
    val added: String?,
    @SerializedName("category_id")
    val categoryId: String?,
    @SerializedName("container_extension")
    val containerExtension: String?,
    @SerializedName("custom_sid")
    val customSid: String?,
    @SerializedName("direct_source")
    val directSource: String?
)