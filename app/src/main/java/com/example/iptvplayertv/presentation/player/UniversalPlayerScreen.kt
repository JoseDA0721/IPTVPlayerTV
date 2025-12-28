package com.example.iptvplayertv.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.iptvplayertv.presentation.player.components.*
import kotlinx.coroutines.delay

/**
 * Pantalla de reproductor universal
 * Soporta: Live TV, Películas y Series
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UniversalPlayerScreen(
    config: PlayerConfiguration,
    onNavigateBack: () -> Unit,
    onShowList: () -> Unit = {},
    onShowSeasons: () -> Unit = {},
    onPlayNext: () -> Unit = {}
) {
    var showControls by remember { mutableStateOf(true) }
    var playerState by remember { mutableStateOf(VideoPlayerState()) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }
    var currentPosition by remember { mutableStateOf(0) }

    // Auto-hide controles después de 5 segundos
    LaunchedEffect(showControls) {
        if (showControls && playerState.isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    // Actualizar posición actual (solo VOD)
    LaunchedEffect(exoPlayer, config.hasProgress()) {
        if (config.hasProgress()) {
            while (true) {
                exoPlayer?.let {
                    currentPosition = (it.currentPosition / 1000).toInt()
                }
                delay(1000)
            }
        }
    }

    UniversalPlayerScreenContent(
        config = config,
        showControls = showControls,
        playerState = playerState,
        currentPosition = currentPosition,
        onShowControls = { showControls = true },
        onPlayerStateChange = { playerState = it },
        onPlayerReady = { exoPlayer = it },
        onAction = { action ->
            when (action) {
                PlayerAction.Back -> onNavigateBack()
                PlayerAction.PlayPause -> {
                    exoPlayer?.let {
                        if (it.isPlaying) it.pause() else it.play()
                    }
                }
                PlayerAction.ShowList -> onShowList()
                PlayerAction.ShowSeasons -> onShowSeasons()
                PlayerAction.PlayNext -> onPlayNext()
                PlayerAction.ToggleAspectRatio -> {
                    // TODO: Implementar cambio de aspect ratio
                }
                PlayerAction.ShowSettings -> {
                    // TODO: Mostrar panel de configuración
                }
                is PlayerAction.Seek -> {
                    exoPlayer?.seekTo(action.positionSeconds * 1000L)
                }
            }
        }
    )
}

@Composable
private fun UniversalPlayerScreenContent(
    config: PlayerConfiguration,
    showControls: Boolean,
    playerState: VideoPlayerState,
    currentPosition: Int,
    onShowControls: () -> Unit,
    onPlayerStateChange: (VideoPlayerState) -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit,
    onAction: (PlayerAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ===== VIDEO PLAYER (Fondo) =====
        VideoPlayerComponent(
            streamUrl = config.streamUrl,
            onStateChange = onPlayerStateChange,
            onPlayerReady = onPlayerReady,
            modifier = Modifier.fillMaxSize()
        )

        // ===== OVERLAY DE CONTROLES =====
        if (showControls && !playerState.hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Barra Superior
                    UniversalPlayerTopBar(
                        config = config,
                        onBackClick = { onAction(PlayerAction.Back) }
                    )

                    // Controles Centrales (Opcional - solo si está pausado)
                    if (!playerState.isPlaying && !playerState.isBuffering) {
                        PlayerCenterControls(
                            isPlaying = playerState.isPlaying,
                            onPlayPauseClick = { onAction(PlayerAction.PlayPause) }
                        )
                    }

                    // Barra Inferior Universal
                    UniversalPlayerBottomBar(
                        config = config,
                        isPlaying = playerState.isPlaying,
                        currentPositionSeconds = currentPosition,
                        onAction = onAction
                    )
                }
            }
        }
    }
}

/**
 * Barra superior adaptable
 * ⚠️ TEMPORAL: Usa PlayerTopBar sin customBadge hasta actualizarlo
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun UniversalPlayerTopBar(
    config: PlayerConfiguration,
    onBackClick: () -> Unit
) {
    // ⚠️ VERSIÓN COMPATIBLE con PlayerTopBar antiguo
    PlayerTopBar(
        channelName = config.title,
        channelNumber = when (config.contentType) {
            PlayerContentType.LIVE_TV -> config.liveTvData?.channelNumber ?: 0
            PlayerContentType.MOVIE -> config.movieData?.movieId ?: 0
            PlayerContentType.SERIES -> config.seriesData?.seriesId ?: 0
        },
        categoryName = config.subtitle ?: "",
        isLive = config.contentType == PlayerContentType.LIVE_TV,
        // ⚠️ COMENTADO hasta actualizar PlayerTopBar:
        // customBadge = config.getBadgeText(),
        onBackClick = onBackClick
    )
}

// ==================== PREVIEWS ====================

@Preview(device = "id:tv_4k")
@Composable
fun LiveTvPlayerPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        UniversalPlayerScreen(
            config = PlayerConfiguration.forLiveTV(
                streamUrl = "http://example.com/stream",
                channelName = "Discovery Channel",
                channelNumber = 101,
                categoryName = "Documentales"
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(device = "id:tv_4k")
@Composable
fun MoviePlayerPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        UniversalPlayerScreen(
            config = PlayerConfiguration.forMovie(
                streamUrl = "http://example.com/movie",
                movieName = "Avatar: El Camino del Agua",
                movieId = 12345,
                durationSeconds = 11520,
                year = "2022",
                genre = "Ciencia Ficción"
            ),
            onNavigateBack = {}
        )
    }
}

@Preview(device = "id:tv_4k")
@Composable
fun SeriesPlayerPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        UniversalPlayerScreen(
            config = PlayerConfiguration.forSeries(
                streamUrl = "http://example.com/episode",
                seriesName = "Breaking Bad",
                seriesId = 1,
                seasonNumber = 5,
                episodeNumber = 14,
                episodeName = "Ozymandias",
                durationSeconds = 2880,
                hasNextEpisode = true
            ),
            onNavigateBack = {}
        )
    }
}