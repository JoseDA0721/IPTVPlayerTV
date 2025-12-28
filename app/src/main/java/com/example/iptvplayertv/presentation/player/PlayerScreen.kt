package com.example.iptvplayertv.presentation.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.exoplayer.ExoPlayer
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.iptvplayertv.presentation.player.components.VideoPlayerComponent
import com.example.iptvplayertv.presentation.player.components.PlayerTopBar
import com.example.iptvplayertv.presentation.player.components.PlayerCenterControls
import com.example.iptvplayertv.presentation.player.components.VideoPlayerState
import com.example.iptvplayertv.presentation.player.components.PlayerBottomBar
import kotlinx.coroutines.delay

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    streamUrl: String,
    channelName: String,
    channelNumber: Int,
    categoryName: String = "",
    onNavigateBack: () -> Unit
) {
    var showControls by remember { mutableStateOf(true) }
    var playerState by remember { mutableStateOf(VideoPlayerState()) }
    var exoPlayer by remember { mutableStateOf<ExoPlayer?>(null) }

    // Auto-hide controles después de 5 segundos
    LaunchedEffect(showControls) {
        if (showControls && playerState.isPlaying) {
            delay(5000)
            showControls = false
        }
    }

    PlayerScreenContent(
        streamUrl = streamUrl,
        channelName = channelName,
        channelNumber = channelNumber,
        categoryName = categoryName,
        showControls = showControls,
        playerState = playerState,
        onShowControls = { showControls = true },
        onPlayerStateChange = { playerState = it },
        onPlayerReady = { exoPlayer = it },
        onNavigateBack = onNavigateBack,
        onPlayPause = {
            exoPlayer?.let {
                if (it.isPlaying) it.pause() else it.play()
            }
        }
    )
}

@Composable
private fun PlayerScreenContent(
    streamUrl: String,
    channelName: String,
    channelNumber: Int,
    categoryName: String,
    showControls: Boolean,
    playerState: VideoPlayerState,
    onShowControls: () -> Unit,
    onPlayerStateChange: (VideoPlayerState) -> Unit,
    onPlayerReady: (ExoPlayer) -> Unit,
    onNavigateBack: () -> Unit,
    onPlayPause: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // ===== VIDEO PLAYER (Fondo) =====
        VideoPlayerComponent(
            streamUrl = streamUrl,
            onStateChange = onPlayerStateChange,
            onPlayerReady = onPlayerReady,
            modifier = Modifier.fillMaxSize()
        )

        // ===== OVERLAY DE CONTROLES =====
        if (showControls && !playerState.hasError) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color = Color.Black.copy(alpha = 0.5f)
                    )
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Barra Superior
                    PlayerTopBar(
                        channelName = channelName,
                        channelNumber = channelNumber,
                        categoryName = categoryName,
                        isLive = true,
                        onBackClick = onNavigateBack
                    )

                    // Controles Centrales (Play/Pause)
//                    PlayerCenterControls(
//                        isPlaying = playerState.isPlaying,
//                        onPlayPauseClick = onPlayPause
//                    )

                    // Barra Inferior
                    PlayerBottomBar(
                        isPlaying = playerState.isPlaying,
                        currentTime = "00:00",
                        totalTime = "00:00",
                    )
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
fun PlayerScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ){
        PlayerScreen(
            streamUrl = "http://example.com/stream.m3u8",
            channelName = "Discovery Channel",
            channelNumber = 101,
            categoryName = "Documentales",
            onNavigateBack = {}
        )
    }
}