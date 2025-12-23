@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.example.iptvplayertv.presentation.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class, ExperimentalTvMaterial3Api::class,
    ExperimentalTvMaterial3Api::class, ExperimentalTvMaterial3Api::class
)
@Composable
fun PlayerScreen(
    streamUrl: String,
    channelName: String,
    channelNumber: Int,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current
    var showControls by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(false) }
    var isBuffering by remember { mutableStateOf(true) }
    var hasError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // Auto-hide controls after 5 seconds
    LaunchedEffect(showControls) {
        if (showControls && isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    val exoPlayer = remember {
        if (isPreview) {
            null
        }else {
            ExoPlayer.Builder(context).build().apply {
                val mediaItem = MediaItem.fromUri(streamUrl)
                setMediaItem(mediaItem)
                prepare()
                playWhenReady = true

                addListener(object : Player.Listener {
                    override fun onPlaybackStateChanged(playbackState: Int) {
                        when (playbackState) {
                            Player.STATE_BUFFERING -> {
                                isBuffering = true
                                hasError = false
                            }

                            Player.STATE_READY -> {
                                isBuffering = false
                                isPlaying = true
                                hasError = false
                            }

                            Player.STATE_ENDED -> {
                                isPlaying = false
                            }

                            Player.STATE_IDLE -> {
                                isBuffering = false
                            }
                        }
                    }

                    override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                        hasError = true
                        isBuffering = false
                        errorMessage = when (error.errorCode) {
                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED ->
                                "Error de conexión de red"

                            androidx.media3.common.PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_TIMEOUT ->
                                "Tiempo de conexión agotado"

                            androidx.media3.common.PlaybackException.ERROR_CODE_PARSING_CONTAINER_MALFORMED ->
                                "Formato de video no válido"

                            else -> "Error al reproducir: ${error.message}"
                        }
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                })
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Video Player
        if (isPreview){
            Box(
                modifier = Modifier.fillMaxSize().background(Color.DarkGray),
                contentAlignment = Alignment.Center
            ) {
                Text("Video Player Preview", color = Color.White)
            }
        }else {
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

            // Buffering Indicator
            if (isBuffering) {
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

            // Error Overlay
            if (hasError) {
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
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = Color(0xFFFF5555),
                            modifier = Modifier.size(80.dp)
                        )

                        Text(
                            text = "Error de Reproducción",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = errorMessage,
                            color = Color(0xFFCCCCCC),
                            fontSize = 18.sp
                        )

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Button(
                                onClick = {
                                    hasError = false
                                    isBuffering = true
                                    exoPlayer?.seekTo(0)
                                    exoPlayer?.prepare()
                                    exoPlayer?.play()
                                },
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFFD97706)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Reintentar")
                            }

                            Button(
                                onClick = onNavigateBack,
                                colors = ButtonDefaults.colors(
                                    containerColor = Color(0xFF333333)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Volver")
                            }
                        }
                    }
                }
            }

            // Custom Controls (show on click or always visible for first 5s)
            if (showControls && !hasError) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                ) {
                    // Top Bar with Channel Info
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp)
                            .align(Alignment.TopStart),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Column {
                                Text(
                                    text = channelName,
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Canal $channelNumber",
                                    color = Color(0xFFCCCCCC),
                                    fontSize = 16.sp
                                )
                            }
                        }

                        // Status Indicator
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .background(
                                    Color(0xFFD97706),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .background(
                                        if (isPlaying) Color.Red else Color.Gray,
                                        RoundedCornerShape(6.dp)
                                    )
                            )
                            Text(
                                text = if (isPlaying) "EN VIVO" else "DETENIDO",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Center Play/Pause Button
                    IconButton(
                        onClick = {
                            exoPlayer?.let {
                                if (it.isPlaying) it.pause() else it.play()
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(80.dp)
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                    }

                    // Bottom Controls
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        PlayerControlButton(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = "Volumen",
                            onClick = { /* TODO: Volume control */ }
                        )

                        PlayerControlButton(
                            icon = Icons.Default.Subtitles,
                            label = "Subtítulos",
                            onClick = { /* TODO: Subtitles */ }
                        )

                        PlayerControlButton(
                            icon = Icons.Default.Settings,
                            label = "Calidad",
                            onClick = { /* TODO: Quality settings */ }
                        )

                        PlayerControlButton(
                            icon = Icons.Default.Info,
                            label = "Info",
                            onClick = { showControls = !showControls }
                        )
                    }
                }
            }
        }

    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 14.sp
        )
    }
}

// Find your preview function and update it like this:
@Preview(
    showBackground = true,
    device = "id:tv_1080p"
)
@Composable
fun PlayerScreenPreview() {
    // Provide sample data for all required parameters
    PlayerScreen(
        streamUrl = "http://example.com/stream.m3u8",
        channelName = "Discovery Channel",
        channelNumber = 101,
        onNavigateBack = {}
    )
}
