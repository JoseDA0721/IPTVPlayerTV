package com.example.iptvplayertv.presentation.livetv.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.Text
import com.example.iptvplayertv.data.model.LiveChannelDetail

@Composable
fun ChannelList(
    channels: List<LiveChannelDetail>,
    onChannelSelected: (LiveChannelDetail) -> Unit,
    onBackToCategories: () -> Unit,
    showingChannels: Boolean
) {
    if (channels.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFD97706))
        }
    } else {
        LazyColumn(
            //state = listState,
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x05FFFFFF)),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 20.dp, horizontal = 20.dp)
        ) {

            item {
                val tituloDinamico = channels.firstOrNull()?.categoryName ?: "Canales"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        //.padding(bottom = 20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ){
                        if (showingChannels) {
                            IconButton(
                                onClick = onBackToCategories,
                                modifier = Modifier
                                    .size(32.dp),
                                colors = IconButtonDefaults.colors(
                                    containerColor = Color.Transparent,
                                    contentColor = Color(0xFFD97706),
                                    focusedContainerColor = Color(0xFF555555)
                                )
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Volver",
                                    tint = Color(0xFFD97706),
                                )
                            }
                        }

                        Text(
                            text = tituloDinamico,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5F5F5),
                            //modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

            }

            items(channels) { channel ->
                ChannelItem(
                    channel = channel,
                    onClick = { onChannelSelected(channel) }
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelItem(
    channel: LiveChannelDetail,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Animación para el borde
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "border_animation"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .border(
                width = borderWidth,
                color = if (isFocused) Color(0xFFD97706) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF1A1A1A),
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent,
            contentColor = Color.Transparent,
            focusedContentColor = Color.Transparent
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            // Channel Logo
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color(0xFF000000)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = if (channel.streamIcon != null) Color(0xFFD97706) else Color(0xFF555555),
                    modifier = Modifier.size(24.dp)
                )
            }

            // Channel Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = channel.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFF5F5F5),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (channel.categoryName != null) {
                    Text(
                        text = channel.categoryName,
                        fontSize = 12.sp,
                        color = Color(0xFF999999),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF0D0D0D
)
@Composable
fun ChannelListPreview() {
    val dummyChannels = listOf(
        LiveChannelDetail(
            num = 1,
            name = "ESPN",
            streamType = "live",
            streamId = 1001,
            streamIcon = "https://example.com/espn.png",
            epgChannelId = "espn",
            added = "2024-01-01",
            categoryId = "1",
            categoryName = "Deportes",
            customSid = null,
            tvArchive = 1,
            directSource = null,
            tvArchiveDuration = 7
        ),
        LiveChannelDetail(
            num = 5,
            name = "Fox Sports",
            streamType = "live",
            streamId = 1005,
            streamIcon = null,
            epgChannelId = "foxsports",
            added = "2024-01-01",
            categoryId = "1",
            categoryName = "Deportes",
            customSid = null,
            tvArchive = 1,
            directSource = null,
            tvArchiveDuration = 7
        )
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.40f)
                .fillMaxHeight()
        ) {
            ChannelList(
                channels = dummyChannels,
                onChannelSelected = {},
                onBackToCategories = {},
                showingChannels = true
            )
        }
    }
}