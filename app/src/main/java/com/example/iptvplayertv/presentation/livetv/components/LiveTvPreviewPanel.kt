package com.example.iptvplayertv.presentation.livetv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvPreviewPanel(
    selectedChannel: LiveChannelDetail?,
    loadState: LiveTvLoadState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color(0xFF0D0D0D))
            .padding(40.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Video Player Area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFF111111), Color(0xFF222222))
                    ),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            when (loadState) {
                is LiveTvLoadState.Loading -> {
                    CircularProgressIndicator(color = Color(0xFFD97706))
                }
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
                            text = "Vista Previa",
                            color = Color(0xFF444444),
                            fontSize = 16.sp
                        )
                    }
                }
            }

            // En Vivo Badge
            if (selectedChannel != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .background(
                            color = Color(0xFFD97706).copy(alpha = 0.9f),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = "EN VIVO",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

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
            text = "Navegue por los canales a la izquierda y seleccione uno para ver la vista previa aquí. Presione 'OK' en el control remoto para pantalla completa.",
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
            .background(Color(0xFF0D0D0D))
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth(0.6f)
                .fillMaxHeight()
                .background(Color(0xFF0D0D0D))
        ){
            LiveTvPreviewPanel(
                selectedChannel = dummyChannel,
                loadState = LiveTvLoadState.Success()
            )
        }
    }
}