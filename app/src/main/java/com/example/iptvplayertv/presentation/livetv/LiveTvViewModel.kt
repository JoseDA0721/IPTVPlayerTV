package com.example.iptvplayertv.presentation.livetv

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvplayertv.data.model.LiveCategory
import com.example.iptvplayertv.data.model.LiveChannelDetail
import com.example.iptvplayertv.data.model.LiveTvLoadState
import com.example.iptvplayertv.data.preferences.UserPreferences
import com.example.iptvplayertv.data.repository.LiveTvRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LiveTvState(
    val categories: List<LiveCategory> = emptyList(),
    val selectedCategory: LiveCategory? = null,
    val channels: List<LiveChannelDetail> = emptyList(),
    val filteredChannels: List<LiveChannelDetail> = emptyList(),
    val selectedChannel: LiveChannelDetail? = null,
    val loadState: LiveTvLoadState = LiveTvLoadState.Idle,
    val searchQuery: String = "",
    val credentials: com.example.iptvplayertv.data.preferences.UserCredentials? = null
)

@HiltViewModel
class LiveTvViewModel @Inject constructor(
    private val liveTvRepository: LiveTvRepository, // ← Versión optimizada
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "LiveTvViewModel"
    }

    private val _state = mutableStateOf(LiveTvState())
    val state: State<LiveTvState> = _state

    // ✅ Job para cancelar búsquedas en progreso
    private var searchJob: Job? = null

    init {
        loadCategories()
    }

    fun loadCategories() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadState = LiveTvLoadState.Loading)

            try {
                val credentials = userPreferences.userCredentials.firstOrNull()

                if (credentials == null) {
                    _state.value = _state.value.copy(
                        loadState = LiveTvLoadState.Error("No hay sesión activa")
                    )
                    return@launch
                }

                // Guardar credenciales en el estado
                _state.value = _state.value.copy(credentials = credentials)

                // ✅ OPTIMIZACIÓN: Cargar categorías desde caché
                val result = liveTvRepository.getCategories(
                    host = credentials.host,
                    user = credentials.username,
                    pass = credentials.password
                )

                result.fold(
                    onSuccess = { categories ->
                        _state.value = _state.value.copy(
                            categories = categories,
                            loadState = LiveTvLoadState.Success()
                        )

                        // ✅ CAMBIO: NO seleccionar automáticamente
                        // Deja que el usuario elija qué ver primero
                        Log.d(TAG, "✓ ${categories.size} categorías cargadas. Esperando selección del usuario.")
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error cargando categorías", exception)
                        _state.value = _state.value.copy(
                            loadState = LiveTvLoadState.Error(
                                exception.message ?: "Error desconocido"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Excepción cargando categorías", e)
                _state.value = _state.value.copy(
                    loadState = LiveTvLoadState.Error(e.message ?: "Error desconocido")
                )
            }
        }
    }

    /**
     * ✅ OPTIMIZACIÓN: Verificar caché antes de cargar
     */
    fun selectCategory(category: LiveCategory) {
        // Si ya está seleccionada, no hacer nada
        if (_state.value.selectedCategory?.categoryId == category.categoryId) {
            Log.d(TAG, "Categoría ${category.categoryName} ya está seleccionada")
            return
        }

        // Verificar si tenemos canales en caché para esta categoría
        val cachedChannels = liveTvRepository.getCachedChannelsForCategory(category.categoryId)

        if (cachedChannels != null) {
            Log.d(TAG, "✓ Usando ${cachedChannels.size} canales desde caché para ${category.categoryName}")

            _state.value = _state.value.copy(
                selectedCategory = category,
                channels = cachedChannels,
                filteredChannels = cachedChannels,
                searchQuery = "",
                selectedChannel = null,
                loadState = LiveTvLoadState.Success()
            )
            return
        }

        // Si no hay caché, cargar desde servidor
        _state.value = _state.value.copy(
            selectedCategory = category,
            channels = emptyList(),
            filteredChannels = emptyList(),
            searchQuery = "",
            selectedChannel = null
        )

        loadChannelsByCategory(category.categoryId)
    }

    private fun loadChannelsByCategory(categoryId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loadState = LiveTvLoadState.Loading)

            try {
                val credentials = userPreferences.userCredentials.firstOrNull()

                if (credentials == null) {
                    _state.value = _state.value.copy(
                        loadState = LiveTvLoadState.Error("No hay sesión activa")
                    )
                    return@launch
                }

                val result = liveTvRepository.getChannelsByCategory(
                    host = credentials.host,
                    user = credentials.username,
                    pass = credentials.password,
                    categoryId = categoryId
                )

                result.fold(
                    onSuccess = { channels ->
                        val newState = updateStateWithFilter(channels, _state.value.searchQuery)
                        _state.value = newState.copy(
                            loadState = LiveTvLoadState.Success()
                        )
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "Error cargando canales", exception)
                        _state.value = _state.value.copy(
                            loadState = LiveTvLoadState.Error(
                                exception.message ?: "Error desconocido"
                            )
                        )
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Excepción cargando canales", e)
                _state.value = _state.value.copy(
                    loadState = LiveTvLoadState.Error(e.message ?: "Error desconocido")
                )
            }
        }
    }

    private fun updateStateWithFilter(
        currentChannels: List<LiveChannelDetail>,
        query: String
    ): LiveTvState {
        val filtered = if (query.isBlank()) {
            currentChannels
        } else {
            // ✅ OPTIMIZACIÓN: Filtro case-insensitive más eficiente
            val lowerQuery = query.lowercase()
            currentChannels.filter {
                it.name.lowercase().contains(lowerQuery)
            }
        }

        return _state.value.copy(
            channels = currentChannels,
            searchQuery = query,
            filteredChannels = filtered
        )
    }

    fun selectChannel(channel: LiveChannelDetail) {
        _state.value = _state.value.copy(selectedChannel = channel)
    }

    /**
     * ✅ OPTIMIZACIÓN: Búsqueda con debounce
     * Cancela búsquedas anteriores si el usuario sigue escribiendo
     */
    fun updateSearchQuery(query: String) {
        // Cancelar búsqueda anterior si existe
        searchJob?.cancel()

        // Si la búsqueda está vacía, aplicar inmediatamente
        if (query.isBlank()) {
            _state.value = updateStateWithFilter(_state.value.channels, query)
            return
        }

        // Para búsquedas largas, aplicar con delay (debounce)
        searchJob = viewModelScope.launch {
            kotlinx.coroutines.delay(300) // 300ms de debounce
            _state.value = updateStateWithFilter(_state.value.channels, query)
        }
    }

    /**
     * ✅ Refresh inteligente
     */
    fun refresh(forceRefresh: Boolean = false) {
        if (forceRefresh) {
            liveTvRepository.clearCache()
            Log.d(TAG, "Caché de LiveTV limpiado")
        }

        // Recargar categorías
        loadCategories()

        // Si había una categoría seleccionada, recargarla
        _state.value.selectedCategory?.let { category ->
            loadChannelsByCategory(category.categoryId)
        }
    }

    /**
     * ✅ Precargar canales de categorías populares
     * Llamar desde HomeScreen antes de navegar a LiveTV
     */
    suspend fun preloadPopularCategories() {
        val credentials = userPreferences.userCredentials.firstOrNull() ?: return

        // Cargar las primeras 3 categorías en segundo plano
        val result = liveTvRepository.getCategories(
            credentials.host,
            credentials.username,
            credentials.password
        )

        result.onSuccess { categories ->
            categories.take(3).forEach { category ->
                liveTvRepository.getChannelsByCategory(
                    credentials.host,
                    credentials.username,
                    credentials.password,
                    category.categoryId
                )
            }
            Log.d(TAG, "✓ Precarga de 3 categorías completada")
        }
    }
}