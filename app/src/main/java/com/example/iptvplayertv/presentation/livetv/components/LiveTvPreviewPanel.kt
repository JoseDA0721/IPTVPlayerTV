package com.example.iptvplayertv.presentation.livetv.components

import android.content.Context
import android.media.AudioManager
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeMute
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
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
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import androidx.tv.material3.Surface
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class)
@Composable
fun LiveTvPreviewPanel(
    selectedChannel: LiveChannelDetail?,
    loadState: LiveTvLoadState,
    modifier: Modifier = Modifier,
    streamUrl: String? = null,
    onPlayFullscreen: ((streamUrl: String, channelName: String, channelNumber: Int, categoryName: String) -> Unit)? = null
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    var isBuffering by remember { mutableStateOf(false) }
    var hasError by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }

    // Estados para el volumen del sistema
    var systemVolume by remember { mutableStateOf(0f) }
    var systemMaxVolume by remember { mutableStateOf(100) }
    var isMuted by remember { mutableStateOf(false) }

    // AudioManager para controlar el volumen del sistema
    val audioManager = remember {
        if (!isPreview) {
            context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        } else {
            null
        }
    }

    // Función para actualizar el volumen desde el sistema
    fun updateSystemVolume() {
        audioManager?.let { am ->
            val currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            systemMaxVolume = maxVolume
            systemVolume = if (maxVolume > 0) {
                currentVolume.toFloat() / maxVolume.toFloat()
            } else {
                0f
            }
            isMuted = currentVolume == 0
        }
    }

    // Monitorear cambios en el volumen del sistema
    LaunchedEffect(Unit) {
        if (!isPreview) {
            while (isActive) {
                updateSystemVolume()
                delay(500) // Verificar cada 500ms
            }
        }
    }

    // Crear ExoPlayer y mantenerlo mientras el componente esté activo
    val exoPlayer = remember {
        if (isPreview) {
            null
        } else {
            ExoPlayer.Builder(context).build().apply {
                // Inicialmente usar el volumen del sistema
                updateSystemVolume()
                volume = systemVolume

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
                    }

                    override fun onIsPlayingChanged(playing: Boolean) {
                        isPlaying = playing
                    }
                })
            }
        }
    }

    // Sincronizar volumen de ExoPlayer con el volumen del sistema
    LaunchedEffect(systemVolume) {
        exoPlayer?.volume = systemVolume
    }

    // Actualizar el stream cuando cambie el canal seleccionado o la URL
    LaunchedEffect(streamUrl) {
        if (!isPreview && exoPlayer != null && streamUrl != null) {
            try {
                exoPlayer.stop()
                exoPlayer.clearMediaItems()

                val mediaItem = MediaItem.fromUri(streamUrl)
                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true

                isBuffering = true
                hasError = false
            } catch (e: Exception) {
                hasError = true
                isBuffering = false
            }
        }
    }

    // Liberar recursos cuando se destruya el composable
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    Column(
        modifier = modifier
            .background(Color(0xFF0D0D0D))
            .padding(40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Video Player Area (Clickeable para pantalla completa)
        VideoPlayerClickableArea(
            selectedChannel = selectedChannel,
            streamUrl = streamUrl,
            isPlaying = isPlaying,
            isBuffering = isBuffering,
            hasError = hasError,
            isMuted = isMuted,
            systemVolume = systemVolume,
            loadState = loadState,
            isPreview = isPreview,
            exoPlayer = exoPlayer,
            onPlayFullscreen = onPlayFullscreen
        )

        // Program Details
        if (selectedChannel != null) {
            ProgramDetails(channel = selectedChannel)
        } else {
            EmptyProgramDetails()
        }
    }
}

@Composable
private fun ProgramDetails(channel: LiveChannelDetail) {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        // Meta Info
        Row(
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Text(
                text = "CANAL ${channel.num}",
                color = Color(0xFFD97706),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            if (channel.categoryName != null) {
                Text(
                    text = "•",
                    color = Color(0xFFD97706),
                    fontSize = 14.sp
                )
                Text(
                    text = channel.categoryName.uppercase(),
                    color = Color(0xFFD97706),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Title
        Text(
            text = channel.name,
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF5F5F5)
        )

        // Description
        Text(
            text = "Vista previa del canal. Presione OK en el control remoto para ver en pantalla completa.",
            color = Color(0xFF999999),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun EmptyProgramDetails() {
    Column(
        verticalArrangement = Arrangement.spacedBy(15.dp)
    ) {
        Text(
            text = "Seleccione un canal",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFF5F5F5)
        )

        Text(
            text = "Navegue por las categorías a la izquierda y seleccione un canal para ver la vista previa aquí.",
            color = Color(0xFF999999),
            fontSize = 16.sp,
            lineHeight = 24.sp
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, UnstableApi::class)
@Composable
private fun VideoPlayerClickableArea(
    selectedChannel: LiveChannelDetail?,
    streamUrl: String?,
    isPlaying: Boolean,
    isBuffering: Boolean,
    hasError: Boolean,
    isMuted: Boolean,
    systemVolume: Float,
    loadState: LiveTvLoadState,
    isPreview: Boolean,
    exoPlayer: ExoPlayer?,
    onPlayFullscreen: ((streamUrl: String, channelName: String, channelNumber: Int, categoryName: String) -> Unit)?
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = {
            // Navegar a pantalla completa si hay canal seleccionado
            if (selectedChannel != null && streamUrl != null && onPlayFullscreen != null) {
                onPlayFullscreen(
                    streamUrl,
                    selectedChannel.name,
                    selectedChannel.num,
                    selectedChannel.categoryName ?: ""
                )
            }
        },
        shape = ClickableSurfaceDefaults.shape(
            shape = RoundedCornerShape(16.dp)
        ),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent
        ),
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .onFocusChanged { isFocused = it.isFocused }
            .border(
                width = if (isFocused) 3.dp else 0.dp,
                color = if (isFocused) Color(0xFFD97706) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF111111), Color(0xFF222222))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                // Modo Preview de Android Studio
                isPreview -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF444444),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Vista Previa",
                            color = Color(0xFF444444),
                            fontSize = 16.sp
                        )
                    }
                }

                // Estado de carga inicial
                loadState is LiveTvLoadState.Loading && selectedChannel == null -> {
                    CircularProgressIndicator(color = Color(0xFFD97706))
                }

                // Mostrar player de video
                streamUrl != null && exoPlayer != null -> {
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

                    // Overlay de buffering
                    if (isBuffering) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = Color(0xFFD97706),
                                    modifier = Modifier.size(48.dp)
                                )
                                Text(
                                    text = "Cargando stream...",
                                    color = Color.White,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    // Overlay de error
                    if (hasError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.7f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "⚠️",
                                    fontSize = 48.sp
                                )
                                Text(
                                    text = "Error al cargar el stream",
                                    color = Color(0xFFFF5555),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Hint para pantalla completa (aparece al enfocar)
                    if (isFocused && !isBuffering && !hasError) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                                Text(
                                    text = "Presione OK para pantalla completa",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Placeholder cuando no hay canal seleccionado
                else -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF444444),
                            modifier = Modifier.size(64.dp)
                        )
                        Text(
                            text = "Seleccione un canal",
                            color = Color(0xFF444444),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // Badge "EN VIVO" cuando hay canal seleccionado y está reproduciendo
            if (selectedChannel != null && isPlaying && !isFocused) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Indicador de volumen del sistema
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFF1A1A1A).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isMuted)
                                    Icons.Default.VolumeMute
                                else
                                    Icons.Default.VolumeUp,
                                contentDescription = "Volumen",
                                tint = if (isMuted) Color(0xFFFF5555) else Color(0xFF4CAF50),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isMuted)
                                    "MUTE"
                                else
                                    "${(systemVolume * 100).toInt()}%",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Badge EN VIVO
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(0xFFD97706).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(Color.Red, shape = RoundedCornerShape(4.dp))
                            )
                            Text(
                                text = "EN VIVO",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k"
)
@Composable
fun LiveTvPreviewPanelPreview() {
    val dummyChannel = LiveChannelDetail(
        num = 5,
        name = "Fox Sports",
        streamType = "live",
        streamId = 1005,
        streamIcon = "",
        epgChannelId = "foxsports",
        added = "2024",
        categoryId = "1",
        categoryName = "Deportes",
        customSid = null,
        tvArchive = 1,
        directSource = null,
        tvArchiveDuration = 7
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight()
                .background(Color(0xFF0D0D0D))
        ){
            LiveTvPreviewPanel(
                selectedChannel = dummyChannel,
                loadState = LiveTvLoadState.Success(),
                streamUrl = null, // En preview no mostramos video real
                onPlayFullscreen = { url, name, num, cat ->
                    println("Navegando a fullscreen: $name")
                }
            )
        }
    }
}