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

data class VideoPlayerState(
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val hasError: Boolean = false,
    val errorMessage: String = "",
    val systemVolume: Float = 0f,
    val isMuted: Boolean = false
)

/**
 * ✅ Componente OPTIMIZADO de ExoPlayer
 * Previene memory leaks y mejora el rendimiento
 */
@OptIn(UnstableApi::class, ExperimentalTvMaterial3Api::class)
@Composable
fun VideoPlayerComponent(
    streamUrl: String,
    modifier: Modifier = Modifier,
    exoPlayer: ExoPlayer? = null, // ✅ Permitir pasar player existente
    onStateChange: (VideoPlayerState) -> Unit = {},
    onPlayerReady: (ExoPlayer) -> Unit = {}
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var playerState by remember { mutableStateOf(VideoPlayerState()) }

    // ✅ OPTIMIZACIÓN 1: Crear player solo si no se proporciona
    val player = exoPlayer ?: remember {
        if (isPreview) null
        else ExoPlayer.Builder(context).build()
    }

    // ✅ OPTIMIZACIÓN 2: Usar VolumeMonitor singleton (si lo implementaste)
    // Si no, mantener el código anterior pero con delay más largo
    val audioManager = remember {
        if (!isPreview) {
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        } else null
    }

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

    // ✅ OPTIMIZACIÓN 3: Monitorear volumen con delay más largo
    LaunchedEffect(Unit) {
        if (!isPreview) {
            while (true) {
                updateSystemVolume()
                delay(2000) // ✅ Cambiado de 500ms a 2s
            }
        }
    }

    // ✅ OPTIMIZACIÓN 4: Setup del player una sola vez
    LaunchedEffect(player) {
        player?.let {
            updateSystemVolume()
            it.volume = playerState.systemVolume

            it.addListener(object : Player.Listener {
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

            onPlayerReady(it)
        }
    }

    // Sincronizar volumen con ExoPlayer
    LaunchedEffect(playerState.systemVolume) {
        player?.volume = playerState.systemVolume
    }

    // ✅ OPTIMIZACIÓN 5: Cargar stream con manejo de errores mejorado
    LaunchedEffect(streamUrl) {
        if (!isPreview && player != null && streamUrl.isNotEmpty()) {
            try {
                // ✅ Detener reproducción anterior de forma segura
                player.stop()
                player.clearMediaItems()

                val mediaItem = MediaItem.fromUri(streamUrl)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true

                playerState = playerState.copy(isBuffering = true, hasError = false)
            } catch (e: Exception) {
                playerState = playerState.copy(
                    hasError = true,
                    isBuffering = false,
                    errorMessage = "Error al cargar stream: ${e.message}"
                )
            }
        }
    }

    // ✅ OPTIMIZACIÓN 6: Liberar recursos solo si NO se pasó un player externo
    DisposableEffect(Unit) {
        onDispose {
            // Solo liberar si creamos el player aquí
            if (exoPlayer == null) {
                player?.release()
            }
        }
    }

    Box(modifier = modifier) {
        when {
            isPreview -> {
                Box(
                    modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Video Player Preview", color = Color.White)
                }
            }

            player != null -> {
                AndroidView(
                    factory = { ctx ->
                        PlayerView(ctx).apply {
                            this.player = player
                            useController = false
                            layoutParams = FrameLayout.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                        }
                    },
                    // ✅ OPTIMIZACIÓN 7: Update solo cuando cambia el player
                    update = { view ->
                        view.player = player
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