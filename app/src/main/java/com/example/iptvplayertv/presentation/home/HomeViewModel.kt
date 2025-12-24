package com.example.iptvplayertv.presentation.home

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvplayertv.data.preferences.UserPreferences
import com.example.iptvplayertv.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val repository: XtreamRepository
) : ViewModel() {

    companion object {
        const val TAG = "HomeViewModel"
    }

    private val _state = mutableStateOf(HomeState())
    val state: State<HomeState> = _state

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                Log.d(TAG, "Intentando cargar credenciales...")
                val credentials = userPreferences.userCredentials.firstOrNull()

                if (credentials != null) {
                    Log.d(TAG, "✓ Credenciales encontradas!")
                    Log.d(TAG, "Host: ${credentials.host}")
                    Log.d(TAG, "Username: ${credentials.username}")
                    Log.d(TAG, "Status: ${credentials.status}")
                    Log.d(TAG, "Exp Date: ${credentials.expDate}")

                    // Cargar información del usuario
                    _state.value = _state.value.copy(
                        userInfo = UserDisplayInfo(
                            username = credentials.username,
                            expDate = formatExpDate(credentials.expDate),
                            status = credentials.status ?: "Unknown"
                        )
                    )

                    // Cargar contadores en paralelo
                    loadCounters(
                        credentials.host,
                        credentials.username,
                        credentials.password
                    )
                } else {
                    Log.w(TAG, "✗ No hay credenciales guardadas")
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error cargando datos", e)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    private fun loadCounters(host: String, username: String, password: String) {
        // Lanzar las 3 peticiones en paralelo
        viewModelScope.launch {
            val liveResult = repository.getLiveChannelsCount(host, username, password)
            liveResult.onSuccess { count ->
                Log.d(TAG, "Live channels: $count")
                _state.value = _state.value.copy(liveChannelsCount = count)
            }
        }

        viewModelScope.launch {
            val moviesResult = repository.getMoviesCount(host, username, password)
            moviesResult.onSuccess { count ->
                Log.d(TAG, "Movies: $count")
                _state.value = _state.value.copy(moviesCount = count)
            }
        }

        viewModelScope.launch {
            val seriesResult = repository.getSeriesCount(host, username, password)
            seriesResult.onSuccess { count ->
                Log.d(TAG, "Series: $count")
                _state.value = _state.value.copy(seriesCount = count)
            }
        }

        // Actualizar timestamp
        _state.value = _state.value.copy(
            lastUpdate = getCurrentTimestamp()
        )
    }

    private fun formatExpDate(timestamp: String?): String {
        if (timestamp == null) return "N/A"

        return try {
            val date = Date(timestamp.toLong() * 1000)
            val format = SimpleDateFormat("MMMM dd, yyyy", Locale("es", "ES"))
            format.format(date)
        } catch (_: Exception) {
            timestamp
        }
    }

    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }

    fun refreshData() {
        loadData()
    }

    fun logout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cerrando sesión...")
                userPreferences.clearCredentials()
                Log.d(TAG, "✓ Sesión cerrada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error al cerrar sesión", e)
            }
        }
    }
}