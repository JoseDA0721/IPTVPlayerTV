package com.example.iptvplayertv.presentation.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.iptvplayertv.presentation.player.PlayerAction
import com.example.iptvplayertv.presentation.player.PlayerConfiguration
import com.example.iptvplayertv.presentation.player.PlayerContentType

/**
 * Barra inferior universal que se adapta al tipo de contenido
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun UniversalPlayerBottomBar(
    config: PlayerConfiguration,
    isPlaying: Boolean = false,
    currentPositionSeconds: Int = 0,
    onAction: (PlayerAction) -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 16.dp)
    ) {
        // Barra de progreso (solo para VOD)
        if (config.hasProgress()) {
            ProgressBar(
                currentSeconds = currentPositionSeconds,
                totalSeconds = config.getDurationSeconds(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Fila de controles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado izquierdo: Controles de reproducción
            LeftControls(
                config = config,
                isPlaying = isPlaying,
                currentSeconds = currentPositionSeconds,
                onAction = onAction
            )

            // Lado derecho: Controles adicionales
            RightControls(
                config = config,
                onAction = onAction
            )
        }
    }
}

/**
 * Barra de progreso para contenido VOD
 */
@Composable
private fun ProgressBar(
    currentSeconds: Int,
    totalSeconds: Int,
    modifier: Modifier = Modifier
) {
    val progress = if (totalSeconds > 0) {
        (currentSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(Color.White.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(Color(0xFFD97706))
        )
    }
}

/**
 * Controles del lado izquierdo (Play, tiempo, etc.)
 */
@Composable
private fun LeftControls(
    config: PlayerConfiguration,
    isPlaying: Boolean,
    currentSeconds: Int,
    onAction: (PlayerAction) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Play/Pause
        PlayerControlButton(
            icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
            onClick = { onAction(PlayerAction.PlayPause) }
        )

        // Tiempo (solo VOD)
        if (config.hasProgress()) {
            Text(
                text = "${formatTime(currentSeconds)} / ${formatTime(config.getDurationSeconds())}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        } else {
            // Para Live TV: mostrar badge o indicador
            Text(
                text = "",
                color = Color(0xFFD97706),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        // Botón siguiente episodio (solo series con next)
        if (config.contentType == PlayerContentType.SERIES
            && config.seriesData?.hasNextEpisode == true) {
            PlayerControlButton(
                icon = Icons.Default.SkipNext,
                contentDescription = "Siguiente episodio",
                onClick = { onAction(PlayerAction.PlayNext) }
            )
        }
    }
}

/**
 * Controles del lado derecho (Listas, ajustes, etc.)
 */
@Composable
private fun RightControls(
    config: PlayerConfiguration,
    onAction: (PlayerAction) -> Unit
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Botón de lista (canales/películas/episodios)
        PlayerControlButton(
            icon = when (config.contentType) {
                PlayerContentType.LIVE_TV -> Icons.Default.Tv
                PlayerContentType.MOVIE -> Icons.Default.Movie
                PlayerContentType.SERIES -> Icons.AutoMirrored.Filled.List
            },
            contentDescription = config.getListButtonLabel(),
            onClick = { onAction(PlayerAction.ShowList) }
        )

        // Botón de temporadas (solo series)
        if (config.contentType == PlayerContentType.SERIES) {
            PlayerControlButton(
                icon = Icons.Default.VideoLibrary,
                contentDescription = "Temporadas",
                onClick = { onAction(PlayerAction.ShowSeasons) }
            )
        }

        // Redimensionar
        PlayerControlButton(
            icon = Icons.Default.AspectRatio,
            contentDescription = "Redimensionar",
            onClick = { onAction(PlayerAction.ToggleAspectRatio) }
        )

        // Configuración
        PlayerControlButton(
            icon = Icons.Default.Settings,
            contentDescription = "Configuración",
            onClick = { onAction(PlayerAction.ShowSettings) }
        )
    }
}

/**
 * Botón de control reutilizable
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayerControlButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Surface(
        onClick = onClick,
        shape = ClickableSurfaceDefaults.shape(shape = CircleShape),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = if (isFocused) Color(0xFFD97706) else Color.Transparent,
            focusedContainerColor = Color(0xFFD97706),
            contentColor = Color.White,
            focusedContentColor = Color.White
        ),
        modifier = Modifier
            .size(40.dp)
            .onFocusChanged { isFocused = it.isFocused }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp),
                tint = Color.White
            )
        }
    }
}

/**
 * Formatea segundos a MM:SS o HH:MM:SS
 */
private fun formatTime(seconds: Int): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, secs)
    } else {
        String.format("%d:%02d", minutes, secs)
    }
}

// ==================== PREVIEWS ====================

@Preview(device = "id:tv_4k")
@Composable
fun LiveTvBottomBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        UniversalPlayerBottomBar(
            config = PlayerConfiguration.forLiveTV(
                streamUrl = "http://example.com/stream",
                channelName = "Discovery Channel",
                channelNumber = 101,
                categoryName = "Documentales"
            ),
            isPlaying = true,
            currentPositionSeconds = 0
        )
    }
}

@Preview(device = "id:tv_4k")
@Composable
fun MovieBottomBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        UniversalPlayerBottomBar(
            config = PlayerConfiguration.forMovie(
                streamUrl = "http://example.com/movie",
                movieName = "Avatar: El Camino del Agua",
                movieId = 12345,
                durationSeconds = 11520, // 3h 12min
                year = "2022",
                genre = "Ciencia Ficción"
            ),
            isPlaying = true,
            currentPositionSeconds = 3456 // 57:36
        )
    }
}

@Preview(device = "id:tv_4k")
@Composable
fun SeriesBottomBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        UniversalPlayerBottomBar(
            config = PlayerConfiguration.forSeries(
                streamUrl = "http://example.com/episode",
                seriesName = "Breaking Bad",
                seriesId = 1,
                seasonNumber = 5,
                episodeNumber = 14,
                episodeName = "Ozymandias",
                durationSeconds = 2880, // 48min
                hasNextEpisode = true
            ),
            isPlaying = false,
            currentPositionSeconds = 1920 // 32min
        )
    }
}