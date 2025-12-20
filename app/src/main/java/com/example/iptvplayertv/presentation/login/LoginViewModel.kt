import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repo: XtreamRepository
) : ViewModel() {

    private val _state = mutableStateOf(LoginState())
    val state: State<LoginState> = _state

    fun onEvent(event: LoginEvent) {
        when (event) {
            is LoginEvent.HostChanged ->
                _state.value = _state.value.copy(host = event.value)

            is LoginEvent.UsernameChanged ->
                _state.value = _state.value.copy(username = event.value)

            is LoginEvent.PasswordChanged ->
                _state.value = _state.value.copy(password = event.value)

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
            try {
                val success = repo.login(
                    host = current.host,
                    user = current.username,
                    pass = current.password
                )

                if (success) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        success = true
                    )
                } else {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "Credenciales inválidas"
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Error de conexión"
                )
            }
        }
    }
}
