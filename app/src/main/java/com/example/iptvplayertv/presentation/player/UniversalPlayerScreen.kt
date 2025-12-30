package com.example.iptvplayertv.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.iptvplayertv.presentation.player.components.*
import kotlinx.coroutines.delay

/**
 * ✅ Pantalla de reproductor OPTIMIZADA
 * Previene memory leaks y mejora el rendimiento
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
    val context = LocalContext.current

    var showControls by remember { mutableStateOf(true) }
    var playerState by remember { mutableStateOf(VideoPlayerState()) }
    var currentPosition by remember { mutableStateOf(0) }

    // ✅ OPTIMIZACIÓN CRÍTICA 1: ExoPlayer como estado singleton
    // Previene crear múltiples instancias
    val exoPlayer = remember(context) {
        ExoPlayer.Builder(context).build().apply {
            // Configuración inicial
            playWhenReady = true
        }
    }

    // ✅ OPTIMIZACIÓN CRÍTICA 2: Cleanup garantizado
    DisposableEffect(Unit) {
        onDispose {
            // Liberar recursos INMEDIATAMENTE
            exoPlayer.stop()
            exoPlayer.clearMediaItems()
            exoPlayer.release()
        }
    }

    // Auto-hide controles
    LaunchedEffect(showControls, playerState.isPlaying) {
        if (showControls && playerState.isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    // ✅ OPTIMIZACIÓN 3: Actualizar posición solo si es VOD
    LaunchedEffect(exoPlayer, config.hasProgress()) {
        if (config.hasProgress()) {
            while (true) {
                currentPosition = (exoPlayer.currentPosition / 1000).toInt()
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
        onPlayerReady = { /* Ya tenemos el player */ },
        exoPlayer = exoPlayer,
        onAction = { action ->
            when (action) {
                PlayerAction.Back -> {
                    // ✅ Detener reproducción antes de salir
                    exoPlayer.stop()
                    onNavigateBack()
                }
                PlayerAction.PlayPause -> {
                    if (exoPlayer.isPlaying) exoPlayer.pause() else exoPlayer.play()
                }
                PlayerAction.ShowList -> onShowList()
                PlayerAction.ShowSeasons -> onShowSeasons()
                PlayerAction.PlayNext -> onPlayNext()
                PlayerAction.ToggleAspectRatio -> {
                    // TODO: Implementar
                }
                PlayerAction.ShowSettings -> {
                    // TODO: Implementar
                }
                is PlayerAction.Seek -> {
                    exoPlayer.seekTo(action.positionSeconds * 1000L)
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
    exoPlayer: ExoPlayer,
    onAction: (PlayerAction) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ===== VIDEO PLAYER =====
        VideoPlayerComponent(
            streamUrl = config.streamUrl,
            onStateChange = onPlayerStateChange,
            onPlayerReady = onPlayerReady,
            exoPlayer = exoPlayer, // ✅ Pasar player existente
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

                    // Controles Centrales
                    if (!playerState.isPlaying && !playerState.isBuffering) {
                        PlayerCenterControls(
                            isPlaying = playerState.isPlaying,
                            onPlayPauseClick = { onAction(PlayerAction.PlayPause) }
                        )
                    }

                    // Barra Inferior
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

@Composable
private fun UniversalPlayerTopBar(
    config: PlayerConfiguration,
    onBackClick: () -> Unit
) {
    PlayerTopBar(
        channelName = config.title,
        channelNumber = when (config.contentType) {
            PlayerContentType.LIVE_TV -> config.liveTvData?.channelNumber ?: 0
            PlayerContentType.MOVIE -> config.movieData?.movieId ?: 0
            PlayerContentType.SERIES -> config.seriesData?.seriesId ?: 0
        },
        categoryName = config.subtitle ?: "",
        isLive = config.contentType == PlayerContentType.LIVE_TV,
        customBadge = config.getBadgeText(),
        onBackClick = onBackClick
    )
}