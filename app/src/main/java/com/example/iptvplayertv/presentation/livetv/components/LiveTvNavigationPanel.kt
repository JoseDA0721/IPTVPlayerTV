package com.example.iptvplayertv.presentation.livetv.components

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState
import com.example.iptvplayertv.presentation.livetv.LiveTvState

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LiveTvNavigationPanel(
    state: LiveTvState,
    showingChannels: Boolean,
    onCategorySelected: (LiveCategory) -> Unit,
    onChannelSelected: (LiveChannelDetail) -> Unit,
    onBackToCategories: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth(0.40f)
            .fillMaxHeight()
            .background(Color(0x05FFFFFF))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            // Content with animation
            AnimatedContent(
                targetState = showingChannels,
                transitionSpec = {
                    (fadeIn() + slideInHorizontally { it }) togetherWith
                            (fadeOut() + slideOutHorizontally { -it })
                },
                label = "content_transition"
            ) { isShowingChannels ->
                if (isShowingChannels) {
                    ChannelList(
                        channels = state.filteredChannels,
                        onChannelSelected = onChannelSelected,
                        onBackToCategories = onBackToCategories,
                        state = state.selectedCategory
                    )
                } else {
                    CategoryList(
                        categories = state.categories,
                        selectedCategory = state.selectedCategory,
                        onCategorySelected = onCategorySelected
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
fun LiveTvNavigationPanelPreview() {
    // 1. Definimos los datos estáticos (igual que antes)
    val dummyCategories = listOf(
        LiveCategory(categoryId = "1", categoryName = "Deportes", parentId = 0),
        LiveCategory(categoryId = "2", categoryName = "Noticias", parentId = 0),
        LiveCategory(categoryId = "3", categoryName = "Entretenimiento", parentId = 0),
        LiveCategory(categoryId = "4", categoryName = "Películas", parentId = 0)
    )

    val dummyChannels = listOf(
        LiveChannelDetail(num = 1, name = "ESPN", streamType = "live", streamId = 1001, streamIcon = "", epgChannelId = "espn", added = "2024", categoryId = "1", categoryName = "Deportes", customSid = null, tvArchive = 1, directSource = null, tvArchiveDuration = 7),
        LiveChannelDetail(num = 2, name = "CNN", streamType = "live", streamId = 1002, streamIcon = "", epgChannelId = "cnn", added = "2024", categoryId = "2", categoryName = "Noticias", customSid = null, tvArchive = 0, directSource = null, tvArchiveDuration = 0),
        LiveChannelDetail(num = 3, name = "Discovery", streamType = "live", streamId = 1003, streamIcon = "", epgChannelId = "discovery", added = "2024", categoryId = "3", categoryName = "Entretenimiento", customSid = null, tvArchive = 1, directSource = null, tvArchiveDuration = 3),
        LiveChannelDetail(num = 4, name = "HBO", streamType = "live", streamId = 1004, streamIcon = "", epgChannelId = "hbo", added = "2024", categoryId = "4", categoryName = "Películas", customSid = null, tvArchive = 0, directSource = null, tvArchiveDuration = 0),
        LiveChannelDetail(num = 5, name = "Fox Sports", streamType = "live", streamId = 1005, streamIcon = "", epgChannelId = "foxsports", added = "2024", categoryId = "1", categoryName = "Deportes", customSid = null, tvArchive = 1, directSource = null, tvArchiveDuration = 7)
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        LiveTvNavigationPanel(
            state = currentState,
            showingChannels = showingChannels,
            // ACCIÓN: Cuando seleccionan categoría
            onCategorySelected = { category ->
                selectedCategory = category // Actualizamos estado
                showingChannels = true      // Cambiamos vista
            },
            // ACCIÓN: Cuando seleccionan canal (solo imprimimos)
            onChannelSelected = { channel ->
                println("Canal seleccionado: ${channel.name}")
            },
            // ACCIÓN: Botón de regreso
            onBackToCategories = {
                showingChannels = false     // Volvemos a categorías
                selectedCategory = null     // Limpiamos selección
            }
        )
    }
}
