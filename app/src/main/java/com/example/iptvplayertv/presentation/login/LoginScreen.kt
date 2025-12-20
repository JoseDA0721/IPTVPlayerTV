import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSuccess: () -> Unit
) {
    val state by viewModel.state

    if (state.success) {
        LaunchedEffect(Unit) {
            onSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Xtream Login",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(24.dp))

        TvTextField(
            value = state.host,
            onValueChange = {
                viewModel.onEvent(LoginEvent.HostChanged(it))
            },
            label = { Text("Host (http://ip:port)") }
        )

        Spacer(Modifier.height(12.dp))

        TvTextField(
            value = state.username,
            onValueChange = {
                viewModel.onEvent(LoginEvent.UsernameChanged(it))
            },
            label = { Text("Usuario") }
        )

        Spacer(Modifier.height(12.dp))

        TvTextField(
            value = state.password,
            onValueChange = {
                viewModel.onEvent(LoginEvent.PasswordChanged(it))
            },
            label = { Text("Contraseña") }
        )

        Spacer(Modifier.height(24.dp))

        TvButton(
            onClick = {
                viewModel.onEvent(LoginEvent.Submit)
            }
        ) {
            Text("Conectar")
        }

        if (state.isLoading) {
            Spacer(Modifier.height(16.dp))
            CircularProgressIndicator()
        }

        state.error?.let {
            Spacer(Modifier.height(16.dp))
            Text(text = it, color = Color.Red)
        }
    }
}
