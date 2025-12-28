package com.example.iptvplayertv.presentation.player

/**
 * Tipos de contenido que puede reproducir el player
 */
enum class PlayerContentType {
    LIVE_TV,    // Canales en vivo
    MOVIE,      // Películas VOD
    SERIES      // Series/Episodios
}

/**
 * Datos específicos para canales en vivo
 */
data class LiveTvPlayerData(
    val channelNumber: Int,
    val categoryName: String,
    val hasEPG: Boolean = false,
    val epgCurrentProgram: String? = null
)

/**
 * Datos específicos para películas
 */
data class MoviePlayerData(
    val movieId: Int,
    val durationSeconds: Int,
    val rating: String? = null,
    val year: String? = null,
    val genre: String? = null,
    val director: String? = null,
    val cast: String? = null,
    val plot: String? = null
)

/**
 * Datos específicos para series
 */
data class SeriesPlayerData(
    val seriesId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeName: String,
    val durationSeconds: Int,
    val totalSeasons: Int = 0,
    val totalEpisodesInSeason: Int = 0,
    val hasNextEpisode: Boolean = false,
    val nextEpisodeId: Int? = null
)

/**
 * Configuración completa del reproductor
 * Esta clase unifica todos los tipos de contenido
 */
data class PlayerConfiguration(
    val contentType: PlayerContentType,
    val streamUrl: String,
    val title: String,
    val subtitle: String? = null,
    val thumbnailUrl: String? = null,

    // Datos específicos por tipo (solo uno será no-nulo)
    val liveTvData: LiveTvPlayerData? = null,
    val movieData: MoviePlayerData? = null,
    val seriesData: SeriesPlayerData? = null
) {
    /**
     * Determina si el contenido tiene progreso/duración
     */
    fun hasProgress(): Boolean = when (contentType) {
        PlayerContentType.LIVE_TV -> false
        PlayerContentType.MOVIE, PlayerContentType.SERIES -> true
    }

    /**
     * Obtiene la duración en segundos (0 si no aplica)
     */
    fun getDurationSeconds(): Int = when (contentType) {
        PlayerContentType.LIVE_TV -> 0
        PlayerContentType.MOVIE -> movieData?.durationSeconds ?: 0
        PlayerContentType.SERIES -> seriesData?.durationSeconds ?: 0
    }

    /**
     * Genera el badge superior derecho
     */
    fun getBadgeText(): String = when (contentType) {
        PlayerContentType.LIVE_TV -> "EN VIVO"
        PlayerContentType.MOVIE -> "PELÍCULA"
        PlayerContentType.SERIES -> {
            val season = seriesData?.seasonNumber ?: 0
            val episode = seriesData?.episodeNumber ?: 0
            "T${season}:E${episode}"
        }
    }

    /**
     * Determina si debe mostrar el botón de lista lateral
     */
    fun showListButton(): Boolean = true

    /**
     * Texto del botón de lista lateral
     */
    fun getListButtonLabel(): String = when (contentType) {
        PlayerContentType.LIVE_TV -> "Canales"
        PlayerContentType.MOVIE -> "Películas"
        PlayerContentType.SERIES -> "Episodios"
    }

    companion object {
        /**
         * Factory: Crear configuración para Live TV
         */
        fun forLiveTV(
            streamUrl: String,
            channelName: String,
            channelNumber: Int,
            categoryName: String = ""
        ) = PlayerConfiguration(
            contentType = PlayerContentType.LIVE_TV,
            streamUrl = streamUrl,
            title = channelName,
            subtitle = "Canal $channelNumber",
            liveTvData = LiveTvPlayerData(
                channelNumber = channelNumber,
                categoryName = categoryName
            )
        )

        /**
         * Factory: Crear configuración para Película
         */
        fun forMovie(
            streamUrl: String,
            movieName: String,
            movieId: Int,
            durationSeconds: Int,
            year: String? = null,
            genre: String? = null
        ) = PlayerConfiguration(
            contentType = PlayerContentType.MOVIE,
            streamUrl = streamUrl,
            title = movieName,
            subtitle = year?.let { "Año $it" },
            movieData = MoviePlayerData(
                movieId = movieId,
                durationSeconds = durationSeconds,
                year = year,
                genre = genre
            )
        )

        /**
         * Factory: Crear configuración para Serie
         */
        fun forSeries(
            streamUrl: String,
            seriesName: String,
            seriesId: Int,
            seasonNumber: Int,
            episodeNumber: Int,
            episodeName: String,
            durationSeconds: Int,
            hasNextEpisode: Boolean = false
        ) = PlayerConfiguration(
            contentType = PlayerContentType.SERIES,
            streamUrl = streamUrl,
            title = seriesName,
            subtitle = "T${seasonNumber}:E${episodeNumber} - $episodeName",
            seriesData = SeriesPlayerData(
                seriesId = seriesId,
                seasonNumber = seasonNumber,
                episodeNumber = episodeNumber,
                episodeName = episodeName,
                durationSeconds = durationSeconds,
                hasNextEpisode = hasNextEpisode
            )
        )
    }
}

/**
 * Eventos/acciones del reproductor
 */
sealed class PlayerAction {
    object PlayPause : PlayerAction()
    object ToggleAspectRatio : PlayerAction()
    object ShowSettings : PlayerAction()
    object ShowList : PlayerAction()
    object ShowSeasons : PlayerAction() // Solo para series
    data class Seek(val positionSeconds: Int) : PlayerAction()
    object PlayNext : PlayerAction() // Series: siguiente episodio
    object Back : PlayerAction()
}