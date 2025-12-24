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
    private val liveTvRepository: LiveTvRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        private const val TAG = "LiveTvViewModel"
    }

    private val _state = mutableStateOf(LiveTvState())
    val state: State<LiveTvState> = _state

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

                        // Seleccionar primera categoría automáticamente
                        if (categories.isNotEmpty()) {
                            selectCategory(categories.first())
                        }
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

    fun selectCategory(category: LiveCategory) {
        if (_state.value.selectedCategory?.categoryId == category.categoryId) {
            return // Ya está seleccionada
        }

        _state.value = _state.value.copy(
            selectedCategory = category,
            channels = emptyList(),
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

    // Agrega esta función privada en tu ViewModel para reutilizar la lógica
    private fun updateStateWithFilter(
        currentChannels: List<LiveChannelDetail>,
        query: String
    ): LiveTvState {
        val filtered = if (query.isBlank()) {
            currentChannels
        } else {
            currentChannels.filter { it.name.contains(query, ignoreCase = true) }
        }

        return _state.value.copy(
            channels = currentChannels,
            searchQuery = query,
            filteredChannels = filtered // Actualizamos la lista filtrada
        )
    }

    fun selectChannel(channel: LiveChannelDetail) {
        _state.value = _state.value.copy(selectedChannel = channel)
    }

    fun updateSearchQuery(query: String) {
        _state.value = updateStateWithFilter(_state.value.channels, query)
    }

    fun refresh() {
        liveTvRepository.clearCache()
        loadCategories()
    }


}