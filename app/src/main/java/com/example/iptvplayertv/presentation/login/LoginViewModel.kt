package com.example.iptvplayertv.presentation.login

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvplayertv.data.local.dao.SessionDao
import com.example.iptvplayertv.data.local.entities.SessionEntity
import com.example.iptvplayertv.data.preferences.UserPreferences
import com.example.iptvplayertv.data.repository.XtreamRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: XtreamRepository,
    private val userPreferences: UserPreferences,
    private val sessionDao: SessionDao // ✅ NUEVO
) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    init {
        // ✅ Verificar si hay sesión activa al iniciar
        checkExistingSession()
    }

    /**
     * ✅ NUEVO: Verificar sesión existente
     */
    private fun checkExistingSession() {
        viewModelScope.launch {
            val session = sessionDao.getSessionOnce()
            val credentials = userPreferences.userCredentials.firstOrNull()

            if (session != null &&
                session.isLoggedIn &&
                session.autoLoginEnabled &&
                credentials != null) {

                // ✅ Hay sesión válida, auto-login
                _state.value = _state.value.copy(
                    host = credentials.host,
                    username = credentials.username,
                    password = credentials.password,
                    isLoading = false,
                    success = true // ← Esto dispara navegación automática
                )
            }
        }
    }

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.HostChanged ->
                _state.value = _state.value.copy(host = event.value, error = null)

            is LoginEvent.UsernameChanged ->
                _state.value = _state.value.copy(username = event.value, error = null)

            is LoginEvent.PasswordChanged ->
                _state.value = _state.value.copy(password = event.value, error = null)

            LoginEvent.Submit -> login()
        }
    }

    private fun login() {
        val current = _state.value

        if (current.host.isBlank() ||
            current.username.isBlank() ||
            current.password.isBlank()
        ) {
            _state.value = current.copy(error = "Todos los campos son obligatorios")
            return
        }

        viewModelScope.launch {
            _state.value = current.copy(isLoading = true, error = null)

            val result = repository.login(
                host = current.host,
                user = current.username,
                pass = current.password
            )

            result.fold(
                onSuccess = { response ->
                    // ✅ Guardar credenciales
                    userPreferences.saveCredentials(
                        host = current.host,
                        username = current.username,
                        password = current.password,
                        expDate = response.userInfo.expDate,
                        status = response.userInfo.status
                    )

                    // ✅ NUEVO: Crear sesión persistente
                    sessionDao.updateSession(
                        SessionEntity(
                            id = 1,
                            isLoggedIn = true,
                            lastLoginTimestamp = System.currentTimeMillis(),
                            autoLoginEnabled = true
                        )
                    )

                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = true,
                        error = null
                    )
                },
                onFailure = { exception ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = exception.message ?: "Error de conexión"
                    )
                }
            )
        }
    }
}