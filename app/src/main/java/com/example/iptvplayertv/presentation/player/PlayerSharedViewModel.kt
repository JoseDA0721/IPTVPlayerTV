package com.example.iptvplayertv.presentation.player

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * ✅ ViewModel compartido para pasar datos al Player
 * Evita saturar la navegación con URLs largas
 */
@HiltViewModel
class PlayerSharedViewModel @Inject constructor() : ViewModel() {

    // ✅ Estado mutable para la configuración actual
    var currentConfig by mutableStateOf<PlayerConfiguration?>(null)
        private set

    /**
     * Establecer nueva configuración para el player
     */
    fun setPlayerConfig(config: PlayerConfiguration) {
        currentConfig = config
    }

    /**
     * Limpiar configuración (llamar al salir del player)
     */
    fun clearConfig() {
        currentConfig = null
    }

    /**
     * Actualizar solo la URL del stream (útil para cambiar de canal sin recrear todo)
     */
    fun updateStreamUrl(newUrl: String) {
        currentConfig?.let { config ->
            currentConfig = config.copy(streamUrl = newUrl)
        }
    }

    /**
     * Verificar si hay configuración válida
     */
    fun hasConfig(): Boolean = currentConfig != null

    override fun onCleared() {
        super.onCleared()
        // ✅ Limpiar al destruir el ViewModel
        clearConfig()
    }
}