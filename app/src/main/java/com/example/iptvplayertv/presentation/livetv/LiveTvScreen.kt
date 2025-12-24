package com.example.iptvplayertv.presentation.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState
import com.example.iptvplayertv.presentation.livetv.components.LiveTvNavigationPanel
import com.example.iptvplayertv.presentation.livetv.components.LiveTvPreviewPanel

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

@Composable
fun LiveTvScreenContent(
    state: LiveTvState,
    onCategorySelected: (LiveCategory) -> Unit,
    onChannelSelected: (LiveChannelDetail) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showingChannels by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {

        // Two-panel layout
        Row(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            // Left Panel (35% width) - Navigation
            LiveTvNavigationPanel(
                state = state,
                showingChannels = showingChannels,
                onCategorySelected = { category ->
                    onCategorySelected(category)
                    showingChannels = true
                },
                onChannelSelected = onChannelSelected,
                onBackToCategories = { showingChannels = false },
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.35f)
            )

            // Right Panel (65% width) - Preview
            LiveTvPreviewPanel(
                selectedChannel = state.selectedChannel,
                loadState = state.loadState,
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(0.65f)
            )
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
        ),
        LiveChannelDetail(
            num = 102,
            name = "Fox Sports",
            streamType = "live",
            streamId = 1002,
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

    // 2. ESTADO DEL PREVIEW: Aquí está la magia
    // Controla si estamos viendo categorías o canales
    var showingChannels by remember { mutableStateOf(false) }


    // Controla qué categoría se seleccionó
    var selectedCategory by remember { mutableStateOf<LiveCategory?>(null) }

    // 3. LÓGICA DE FILTRADO (Simulando el ViewModel)
    // Cada vez que cambia la categoría seleccionada, recalculamos la lista de canales
    val currentFilteredChannels = remember(selectedCategory) {
        if (selectedCategory == null) emptyList()
        else dummyChannels.filter { it.categoryId == selectedCategory?.categoryId }
    }

    // 4. Construimos el estado actual basado en las variables anteriores
    // Nota: Asumo que LiveTvState tiene un campo 'filteredChannels'.
    // Si tu data class no lo tiene, ajusta esto para pasar la lista donde corresponda.
    val currentState = LiveTvState(
        categories = dummyCategories,
        selectedCategory = selectedCategory,
        channels = dummyChannels,
        filteredChannels = currentFilteredChannels,// Lista completa // Lista filtrada (NECESARIO AGREGAR ESTO SI NO EXISTE EN TU DATA CLASS)
        loadState = LiveTvLoadState.Success(),
        searchQuery = ""
    )

    LiveTvScreenContent(
        state  = currentState,
        onCategorySelected = {},
        onChannelSelected = {},
        onRefresh = {},
        onNavigateBack = {}
    )
}