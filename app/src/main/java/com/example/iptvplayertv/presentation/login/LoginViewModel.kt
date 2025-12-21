package com.example.iptvplayertv.presentation.login

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvplayertv.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: XtreamRepository
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.HostChanged -> {
                Log.d(TAG, "Host changed: ${event.value}")
                _state.value = _state.value.copy(host = event.value, error = null)
            }

            is LoginEvent.UsernameChanged -> {
                Log.d(TAG, "Username changed: ${event.value}")
                _state.value = _state.value.copy(username = event.value, error = null)
            }

            is LoginEvent.PasswordChanged -> {
                Log.d(TAG, "Password changed: [HIDDEN]")
                _state.value = _state.value.copy(password = event.value, error = null)
            }

            LoginEvent.Submit -> {
                Log.d(TAG, "Submit button pressed")
                login()
            }
        }
    }

    private fun login() {
        val current = _state.value

        Log.d(TAG, "=== INICIO LOGIN ===")
        Log.d(TAG, "Host: ${current.host}")
        Log.d(TAG, "Username: ${current.username}")
        Log.d(TAG, "Password: ${if (current.password.isNotBlank()) "[PROVIDED]" else "[EMPTY]"}")

        // Validación de campos
        if (current.host.isBlank() ||
            current.username.isBlank() ||
            current.password.isBlank()
        ) {
            val errorMsg = "Todos los campos son obligatorios"
            Log.w(TAG, "Validation failed: $errorMsg")
            _state.value = current.copy(error = errorMsg)
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)
            Log.d(TAG, "Estado: LOADING...")

            try {
                val result = repo.login(
                    host = current.host,
                    user = current.username,
                    pass = current.password
                )

                result.fold(
                    onSuccess = { response ->
                        Log.d(TAG, "✓ LOGIN EXITOSO")
                        Log.d(TAG, "User status: ${response.userInfo.status}")
                        Log.d(TAG, "Server URL: ${response.serverInfo.url}")

                        _state.value = _state.value.copy(
                            isLoading = false,
                            success = true,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        val errorMsg = exception.message ?: "Error de conexión"
                        Log.e(TAG, "✗ LOGIN FALLIDO: $errorMsg", exception)

                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = errorMsg
                        )
                    }
                )
            } catch (e: Exception) {
                val errorMsg = "Error inesperado: ${e.message}"
                Log.e(TAG, "✗ EXCEPCIÓN: $errorMsg", e)

                _state.value = _state.value.copy(
                    isLoading = false,
                    error = errorMsg
                )
            }

            Log.d(TAG, "=== FIN LOGIN ===")
        }
    }
}