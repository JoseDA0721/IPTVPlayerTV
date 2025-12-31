package com.example.iptvplayertv.presentation.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Text
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState
import com.example.iptvplayertv.presentation.components.LoadingScreen
import com.example.iptvplayertv.presentation.livetv.components.LiveTvNavigationPanel
import com.example.iptvplayertv.presentation.livetv.components.LiveTvPreviewPanel
import com.example.iptvplayertv.presentation.livetv.components.SidebarMenu

@Composable
fun LiveTvScreen(
    viewModel: LiveTvViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (streamUrl: String, channelName: String, channelNumber: Int) -> Unit
) {
    val state by viewModel.state

    // ✅ NUEVO: Manejo de estados de carga
    when {
        // Estado inicial: Mostrando pantalla de carga mientras se cargan categorías
        state.categories.isEmpty() && state.loadState is LiveTvLoadState.Loading -> {
            LoadingScreen(
                message = "Cargando categorías...",
        //        subtitle = "Conectando al servidor"
            )
        }

        // Error al cargar categorías
        state.categories.isEmpty() && state.loadState is LiveTvLoadState.Error -> {
            ErrorScreen(
                message = (state.loadState as LiveTvLoadState.Error).message,
                onRetry = { viewModel.loadCategories() },
                onBack = onNavigateBack
            )
        }

        // Categorías cargadas: Mostrar interfaz normal
        else -> {
            LiveTvMainContent(
                state = state,
                viewModel = viewModel,
                onNavigateBack = onNavigateBack,
                onNavigateToPlayer = onNavigateToPlayer
            )
        }
    }
}

@Composable
private fun LiveTvMainContent(
    state: LiveTvState,
    viewModel: LiveTvViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (streamUrl: String, channelName: String, channelNumber: Int) -> Unit
) {
    val streamUrl = remember(state.selectedChannel, state.credentials) {
        if (state.selectedChannel != null && state.credentials != null) {
            state.selectedChannel.getStreamUrl(
                host = state.credentials.host,
                username = state.credentials.username,
                password = state.credentials.password
            )
        } else {
            null
        }
    }

    var showingChannels by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Sidebar Menu
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.05f)
                    .fillMaxHeight()
            ) {
                SidebarMenu()
            }

            // Left Panel - Navigation
            LiveTvNavigationPanel(
                state = state,
                showingChannels = showingChannels,
                onCategorySelected = { category ->
                    viewModel.selectCategory(category)
                    showingChannels = true
                },
                onChannelSelected = { viewModel.selectChannel(it) },
                onBackToCategories = { showingChannels = false },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
            )

            // Right Panel - Preview
            LiveTvPreviewPanel(
                selectedChannel = state.selectedChannel,
                loadState = state.loadState,
                streamUrl = streamUrl,
                onPlayFullscreen = { url, name, num, category ->
                    onNavigateToPlayer(url, name, num)
                },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.60f)
            )
        }
    }
}

/**
 * ✅ NUEVO: Pantalla de error con opción de reintentar
 */
@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Column(
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.padding(40.dp)
        ) {
            Text(
                text = "⚠️",
                fontSize = 64.sp
            )

            Text(
                text = "Error al cargar contenido",
                fontSize = 28.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                color = Color(0xFFF5F5F5)
            )

            Text(
                text = message,
                fontSize = 18.sp,
                color = Color(0xFF999999),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                androidx.tv.material3.Button(
                    onClick = onBack
                ) {
                    androidx.tv.material3.Text("Volver")
                }

                androidx.tv.material3.Button(
                    onClick = onRetry,
                    colors = androidx.tv.material3.ButtonDefaults.colors(
                        containerColor = Color(0xFFD97706)
                    )
                ) {
                    androidx.tv.material3.Text("Reintentar")
                }
            }
        }
    }
}

// ============= PREVIEW =============

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF0D0D0D
)
@Composable
fun LiveTvScreenPreview() {
    val dummyCategories = listOf(
        LiveCategory(categoryId = "1", categoryName = "Deportes", parentId = 0),
        LiveCategory(categoryId = "2", categoryName = "Noticias", parentId = 0),
        LiveCategory(categoryId = "3", categoryName = "Entretenimiento", parentId = 0),
        LiveCategory(categoryId = "4", categoryName = "Películas", parentId = 0)
    )

    val dummyChannels = listOf(
        LiveChannelDetail(
            num = 101,
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
        )
    )

    var showingChannels by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<LiveCategory?>(null) }

    val currentState = LiveTvState(
        categories = dummyCategories,
        selectedCategory = selectedCategory,
        channels = dummyChannels,
        filteredChannels = if (selectedCategory == null) emptyList() else dummyChannels,
        loadState = LiveTvLoadState.Success(),
        searchQuery = ""
    )

    LiveTvMainContent(
        state = currentState,
        viewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
        onNavigateBack = {},
        onNavigateToPlayer = { _, _, _ -> }
    )
}