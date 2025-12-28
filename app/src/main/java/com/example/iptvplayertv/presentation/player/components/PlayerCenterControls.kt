package com.example.iptvplayertv.presentation.player.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerCenterControls(
    isPlaying: Boolean,
    onPlayPauseClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        IconButton(
            onClick = onPlayPauseClick,
            modifier = Modifier.size(90.dp)
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                tint = Color.White,
                modifier = Modifier.size(90.dp)
            )
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
)
@Composable
fun PlayerCenterControlsPreview() {
    PlayerCenterControls(
        isPlaying = false,
        onPlayPauseClick = {}
    )
}