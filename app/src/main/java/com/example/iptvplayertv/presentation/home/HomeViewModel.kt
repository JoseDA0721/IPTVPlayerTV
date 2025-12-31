package com.example.iptvplayertv.presentation.home

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvplayertv.data.local.dao.SessionDao
import com.example.iptvplayertv.data.preferences.UserPreferences
import com.example.iptvplayertv.data.repository.XtreamRepositoryImp
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
    private val repository: XtreamRepositoryImp, // ✅ Con Room
    private val sessionDao: SessionDao // ✅ NUEVO
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
                Log.d(TAG, "Cargando datos...")
                val credentials = userPreferences.userCredentials.firstOrNull()

                if (credentials != null) {
                    Log.d(TAG, "✓ Credenciales encontradas")

                    _state.value = _state.value.copy(
                        userInfo = UserDisplayInfo(
                            username = credentials.username,
                            expDate = formatExpDate(credentials.expDate),
                            status = credentials.status ?: "Unknown"
                        )
                    )

                    // ✅ Cargar contadores (con fallback a Database)
                    loadAllCounters(
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

    private fun loadAllCounters(host: String, username: String, password: String) {
        viewModelScope.launch {
            val result = repository.getAllCounts(host, username, password)

            result.onSuccess { counts ->
                Log.d(TAG, "✓ Contadores: Live=${counts.liveChannels}, Movies=${counts.movies}, Series=${counts.series}")

                _state.value = _state.value.copy(
                    liveChannelsCount = counts.liveChannels,
                    moviesCount = counts.movies,
                    seriesCount = counts.series,
                    lastUpdate = getCurrentTimestamp()
                )
            }.onFailure { error ->
                Log.e(TAG, "✗ Error obteniendo contadores (usando fallback)", error)
                // Los valores por defecto (0) ya están en el estado
            }
        }
    }

    private fun formatExpDate(timestamp: String?): String {
        if (timestamp == null) return "N/A"

        return try {
            val date = Date(timestamp.toLong() * 1000)
            val format = SimpleDateFormat("MMMM dd, yyyy", Locale.forLanguageTag("es-ES"))
            format.format(date)
        } catch (_: Exception) {
            timestamp
        }
    }

    private fun getCurrentTimestamp(): String {
        val format = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        return format.format(Date())
    }

    fun refreshData(forceRefresh: Boolean = false) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            try {
                val credentials = userPreferences.userCredentials.firstOrNull()

                if (credentials != null) {
                    if (forceRefresh) {
                        repository.clearAllCache()
                        Log.d(TAG, "Caché limpiado para refresh forzado")
                    }

                    val result = repository.getAllCounts(
                        credentials.host,
                        credentials.username,
                        credentials.password,
                        forceRefresh = forceRefresh
                    )

                    result.onSuccess { counts ->
                        _state.value = _state.value.copy(
                            liveChannelsCount = counts.liveChannels,
                            moviesCount = counts.movies,
                            seriesCount = counts.series,
                            lastUpdate = getCurrentTimestamp()
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error en refresh", e)
            } finally {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Cerrando sesión...")

                // ✅ Limpiar caché
                repository.clearAllCache()

                // ✅ NUEVO: Limpiar sesión en Database
                sessionDao.clearSession()

                // ✅ Limpiar credenciales
                userPreferences.clearCredentials()

                Log.d(TAG, "✓ Sesión cerrada exitosamente")
            } catch (e: Exception) {
                Log.e(TAG, "✗ Error al cerrar sesión", e)
            }
        }
    }
}