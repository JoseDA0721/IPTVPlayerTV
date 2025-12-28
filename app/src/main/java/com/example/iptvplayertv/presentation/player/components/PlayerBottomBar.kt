package com.example.iptvplayertv.presentation.player.components

import android.R
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

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerBottomBar(
    isPlaying: Boolean = false,
    currentTime: String = "00:00",
    totalTime: String = "00:00",
    onPlayPauseClick: () -> Unit = {},
    onVolumeClick: () -> Unit = {},
    onChannelListClick: () -> Unit = {},
    onSubtitlesClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    onExpandClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            //.background(Color.Black.copy(alpha = 0.7f))
            .padding(horizontal = 40.dp, vertical = 16.dp)
    ) {
        // Barra de progreso
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Color.White.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.3f) // Simula 30% de progreso
                    .fillMaxHeight()
                    .background(Color(0xFFD97706))
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Fila de controles
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Lado izquierdo: Play/Pause + Volumen + Tiempo
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Play/Pause
                PlayerControlIconButton(
                    icon = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                    onClick = onPlayPauseClick
                )

                // Tiempo
                Text(
                    text = "$currentTime / $totalTime",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Lado derecho: Controles adicionales
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerControlIconButton(
                    icon = Icons.AutoMirrored.Filled.List,
                    contentDescription = "Lista de canales",
                    onClick = onChannelListClick
                )

                PlayerControlIconButton(
                    icon = Icons.Default.AspectRatio,
                    contentDescription = "Redimensionar",
                    onClick = onSubtitlesClick
                )

                PlayerControlIconButton(
                    icon = Icons.Default.Settings,
                    contentDescription = "Configuración",
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun PlayerControlIconButton(
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
            contentAlignment = Alignment.Center,
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

@Preview(device = "id:tv_4k")
@Composable
fun PlayerBottomBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.BottomCenter
    ) {
        PlayerBottomBar(
            isPlaying = true,
            currentTime = "32:03",
            totalTime = "1:53:06"
        )
    }
}