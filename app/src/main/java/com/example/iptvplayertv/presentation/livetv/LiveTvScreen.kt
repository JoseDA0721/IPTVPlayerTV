package com.example.iptvplayertv.presentation.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
// import coil.compose.AsyncImage  // Coil no está disponible, usaremos placeholder
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState

@Composable
fun LiveTvScreen(
    viewModel: LiveTvViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (streamUrl: String, channelName: String, channelNumber: Int) -> Unit
) {
    val state by viewModel.state

    LiveTvScreenContent(
        state = state,
        onCategorySelected = { viewModel.selectCategory(it) },
        onChannelSelected = { channel ->
            viewModel.selectChannel(channel)

            // Construir URL del stream
            state.credentials?.let { creds ->
                val streamUrl = channel.getStreamUrl(
                    host = creds.host,
                    username = creds.username,
                    password = creds.password
                )
                onNavigateToPlayer(streamUrl, channel.name, channel.num)
            }
        },
        onRefresh = { viewModel.refresh() },
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvScreenContent(
    state: LiveTvState,
    onCategorySelected: (LiveCategory) -> Unit,
    onChannelSelected: (LiveChannelDetail) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            LiveTvTopBar(
                onNavigateBack = onNavigateBack,
                onRefresh = onRefresh
            )

            // Categories Row
            if (state.categories.isNotEmpty()) {
                CategoriesRow(
                    categories = state.categories,
                    selectedCategory = state.selectedCategory,
                    onCategorySelected = onCategorySelected
                )
            }

            Spacer(Modifier.height(20.dp))

            // Channels Grid
            when (state.loadState) {
                is LiveTvLoadState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFD97706))
                    }
                }
                is LiveTvLoadState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "❌ ${state.loadState.message}",
                            color = Color(0xFFFF5555),
                            fontSize = 18.sp
                        )
                    }
                }
                else -> {
                    if (state.filteredChannels.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No hay canales disponibles",
                                color = Color(0xFF999999),
                                fontSize = 18.sp
                            )
                        }
                    } else {
                        ChannelsList(
                            channels = state.filteredChannels,
                            onChannelSelected = onChannelSelected
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvTopBar(
    onNavigateBack: () -> Unit,
    onRefresh: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color(0xFFF5F5F5),
                    modifier = Modifier.size(32.dp)
                )
            }

            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = null,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(40.dp)
            )

            Text(
                text = "TV EN VIVO",
                color = Color(0xFFF5F5F5),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold
            )
        }

        IconButton(onClick = onRefresh) {
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Actualizar",
                tint = Color(0xFFD97706),
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoriesRow(
    categories: List<LiveCategory>,
    selectedCategory: LiveCategory?,
    onCategorySelected: (LiveCategory) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(categories) { category ->
            CategoryChip(
                category = category,
                isSelected = category.categoryId == selectedCategory?.categoryId,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryChip(
    category: LiveCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        modifier = Modifier
            .height(48.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = if (isFocused) 1.05f else 1f
                scaleY = if (isFocused) 1.05f else 1f
            },
        colors = ButtonDefaults.colors(
            containerColor = if (isSelected) Color(0xFFD97706) else Color(0xFF1A1A1A),
            focusedContainerColor = if (isSelected) Color(0xFFD97706) else Color(0xFF2A2A2A),
            contentColor = Color(0xFFF5F5F5)
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(24.dp))
    ) {
        Text(
            text = category.categoryName,
            fontSize = 16.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ChannelsList(
    channels: List<LiveChannelDetail>,
    onChannelSelected: (LiveChannelDetail) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(channels) { index, channel ->
            ChannelCard(
                channel = channel,
                onClick = { onChannelSelected(channel) }
            )
        }

        // Espaciado al final
        item {
            Spacer(Modifier.height(40.dp))
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ChannelCard(
    channel: LiveChannelDetail,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = if (isFocused) 1.02f else 1f
                scaleY = if (isFocused) 1.02f else 1f
            },
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF1A1A1A),
            focusedContainerColor = Color(0xFF2A2A2A)
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(12.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Channel Icon
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF0D0D0D))
                    .border(
                        width = if (isFocused) 2.dp else 0.dp,
                        color = if (isFocused) Color(0xFFD97706) else Color.Transparent,
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                // TODO: Agregar Coil para cargar imágenes
                // Por ahora solo mostramos el icono
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = null,
                    tint = if (channel.streamIcon != null) Color(0xFFD97706) else Color(0xFF666666),
                    modifier = Modifier.size(40.dp)
                )
            }

            // Channel Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = channel.name,
                    color = Color(0xFFF5F5F5),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(4.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (channel.categoryName != null) {
                        Text(
                            text = channel.categoryName,
                            color = Color(0xFF999999),
                            fontSize = 14.sp
                        )
                    }

                    if (channel.tvArchive == 1) {
                        Text(
                            text = "• Archive",
                            color = Color(0xFFD97706),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Number Badge
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(if (isFocused) Color(0xFFD97706) else Color(0xFF2A2A2A)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${channel.num}",
                    color = Color(0xFFF5F5F5),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}