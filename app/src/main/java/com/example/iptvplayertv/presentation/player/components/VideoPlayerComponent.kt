package com.example.iptvplayertv.presentation.player.components

import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

/**
 * Estado del reproductor de video
 */
data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val systemVolume: Float = 0f,
    val isMuted: Boolean = false
)

/**
 * Componente reutilizable de ExoPlayer con sincronización de volumen del sistema
 */
@OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerComponent(
    streamUrl: String,
    modifier: Modifier = Modifier,
    onStateChange: (VideoPlayerState) -> Unit = {},
    onPlayerReady: (ExoPlayer) -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var playerState by remember { mutableStateOf(VideoPlayerState()) }

    // AudioManager para volumen del sistema
    val audioManager = remember {
        if (!isPreview) {
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        } else null
    }

    // Función para actualizar volumen del sistema
    fun updateSystemVolume() {
        audioManager?.let { am ->
            val currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            val volumePercent = if (maxVolume > 0) {
                currentVolume.toFloat() / maxVolume.toFloat()
            } else 0f

            playerState = playerState.copy(
                systemVolume = volumePercent,
                isMuted = currentVolume == 0
            )
        }
    }

    // Crear ExoPlayer
    val exoPlayer = remember {
        if (isPreview) null
        else {
            ExoPlayer.Builder(context).build().apply {
                // Configurar volumen inicial
                updateSystemVolume()
                volume = playerState.systemVolume

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        val newState = when (playbackState) {
                            Player.STATE_BUFFERING -> playerState.copy(
                                isBuffering = true,
                                hasError = false
                            )
                            Player.STATE_READY -> playerState.copy(
                                isBuffering = false,
                                isPlaying = true,
                                hasError = false
                            )
                            Player.STATE_ENDED -> playerState.copy(isPlaying = false)
                            Player.STATE_IDLE -> playerState.copy(isBuffering = false)
                            else -> playerState
                        }
                        playerState = newState
                        onStateChange(newState)
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        val errorMsg = when (error.errorCode) {
                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                                "Error de conexión de red"
                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                                "Tiempo de conexión agotado"
                            androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ->
                                "Formato de video no válido"
                            else -> "Error al reproducir: ${error.message}"
                        }

                        val newState = playerState.copy(
                            hasError = true,
                            isBuffering = false,
                            errorMessage = errorMsg
                        )
                        playerState = newState
                        onStateChange(newState)
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        val newState = playerState.copy(isPlaying = playing)
                        playerState = newState
                        onStateChange(newState)
                    }
                })
            }
        }
    }

    // Notificar cuando el player está listo
    LaunchedEffect(exoPlayer) {
        exoPlayer?.let { onPlayerReady(it) }
    }

    // Monitorear volumen del sistema
    LaunchedEffect(Unit) {
        if (!isPreview) {
            while (isActive) {
                updateSystemVolume()
                delay(500)
            }
        }
    }

    // Sincronizar volumen con ExoPlayer
    LaunchedEffect(playerState.systemVolume) {
        exoPlayer?.volume = playerState.systemVolume
    }

    // Cargar stream
    LaunchedEffect(streamUrl) {
        if (!isPreview && exoPlayer != null && streamUrl.isNotEmpty()) {
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()

                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

                playerState = playerState.copy(isBuffering = true, hasError = false)
            } catch (e: Exception) {
                playerState = playerState.copy(
                    hasError = true,
                    isBuffering = false,
                    errorMessage = "Error al cargar stream"
                )
            }
        }
    }

    // Liberar recursos
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Box(modifier = modifier) {
        when {
            // Modo Preview
            isPreview -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Video Player Preview", color = Color.White)
                }
            }

            // Player de video
            exoPlayer != null -> {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            player = exoPlayer
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Overlay de buffering
        if (playerState.isBuffering) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFD97706),
                        modifier = Modifier.size(64.dp),
                        strokeWidth = 6.dp
                    )
                    Text(
                        text = "Cargando...",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Overlay de error
        if (playerState.hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.9f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                    modifier = Modifier.padding(40.dp)
                ) {
                    Text(
                        text = "⚠️",
                        fontSize = 64.sp
                    )
                    Text(
                        text = "Error de Reproducción",
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = playerState.errorMessage,
                        color = Color(0xFFCCCCCC),
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}