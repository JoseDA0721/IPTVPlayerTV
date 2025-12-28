package com.example.iptvplayertv.presentation.livetv

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState
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

    // Construir la URL del stream cuando hay un canal seleccionado
    val streamUrl = remember(state.selectedChannel, state.credentials) {
        if (state.selectedChannel != null && state.credentials != null) {
            state.selectedChannel!!.getStreamUrl(
                host = state.credentials!!.host,
                username = state.credentials!!.username,
                password = state.credentials!!.password
            )
        } else {
            null
        }
    }

    LiveTvScreenContent(
        state = state,
        streamUrl = streamUrl,
        onCategorySelected = { viewModel.selectCategory(it) },
        onChannelSelected = { channel ->
            viewModel.selectChannel(channel)
        },
        onRefresh = { viewModel.refresh() },
        onNavigateBack = onNavigateBack,
        onNavigateToPlayer = onNavigateToPlayer
    )
}

@Composable
fun LiveTvScreenContent(
    state: LiveTvState,
    streamUrl: String?,
    onCategorySelected: (LiveCategory) -> Unit,
    onChannelSelected: (LiveChannelDetail) -> Unit,
    onRefresh: () -> Unit,
    onNavigateBack: () -> Unit,
    onNavigateToPlayer: (streamUrl: String, channelName: String, channelNumber: Int) -> Unit
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
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.05f)
                    .fillMaxHeight()
            ){
                SidebarMenu()
            }

            // Left Panel (30% width) - Navigation
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

            // Right Panel (60% width) - Preview
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

    var showingChannels by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<LiveCategory?>(null) }

    val currentFilteredChannels = remember(selectedCategory) {
        if (selectedCategory == null) emptyList()
        else dummyChannels.filter { it.categoryId == selectedCategory?.categoryId }
    }

    val currentState = LiveTvState(
        categories = dummyCategories,
        selectedCategory = selectedCategory,
        channels = dummyChannels,
        filteredChannels = currentFilteredChannels,
        loadState = LiveTvLoadState.Success(),
        searchQuery = ""
    )

    LiveTvScreenContent(
        state = currentState,
        streamUrl = null,
        onCategorySelected = {},
        onChannelSelected = {},
        onRefresh = {},
        onNavigateBack = {},
        onNavigateToPlayer = { _, _, _ -> }
    )
}