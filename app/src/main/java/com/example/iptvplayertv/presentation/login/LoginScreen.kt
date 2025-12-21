package com.example.iptvplayertv.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.example.iptvplayertv.presentation.components.TvButton
import com.example.iptvplayertv.presentation.components.TvTextField

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onSuccess: () -> Unit) {
    val state by viewModel.state

    // Call the stateless version
    LoginScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSuccess = onSuccess
    )
}
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginState, // Assuming your state class is named LoginState
    onEvent: (LoginEvent) -> Unit,
    onSuccess: () -> Unit
) {

    // Navegar cuando el login sea exitoso
    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(Color(0xFF1A1A1A)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(600.dp)
                .verticalScroll(rememberScrollState())
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título principal
            Text(
                text = "IPTV Player",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(Modifier.height(16.dp))

            // Subtítulo
            Text(
                text = "Xtream Login",
                fontSize = 28.sp,
                color = Color(0xFFBBBBBB)
            )

            Spacer(Modifier.height(40.dp))

            // Campo Host
            Text(
                text = "Host (http://ip:port)",
                fontSize = 16.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            TvTextField(
                value = state.host,
                onValueChange = {
                    onEvent(LoginEvent.HostChanged(it))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // Campo Usuario
            Text(
                text = "Usuario",
                fontSize = 16.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            TvTextField(
                value = state.username,
                onValueChange = {
                    onEvent(LoginEvent.UsernameChanged(it))
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            // Campo Contraseña
            Text(
                text = "Contraseña",
                fontSize = 16.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            TvTextField(
                value = state.password,
                onValueChange = {
                    onEvent(LoginEvent.PasswordChanged(it))
                },
                isPassword = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            // Botón Conectar
            TvButton(
                onClick = {
                    onEvent(LoginEvent.Submit)
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .width(120.dp)
                    .padding(bottom = 16.dp)
                    .height(50.dp)
            ) {

                Text(
                    text = if (state.isLoading) "Conectando..." else "Conectar",
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Loading indicator
            if (state.isLoading) {
                Spacer(Modifier.height(20.dp))
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color.White
                )
            }

            // Mensaje de Error
            state.error?.let { error ->
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFCC0000),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "❌ $error",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Mensaje de Éxito
            if (state.success) {
                Spacer(Modifier.height(24.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF00AA00),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(16.dp)
                ) {
                    Text(
                        text = "✓ ¡Conexión exitosa!",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun LoginScreenPreview() {
    // Now the preview doesn't need a ViewModel at all!
    LoginScreenContent(
        state = LoginState(host = "http://192.168.1.1"),
        onEvent = {},
        onSuccess = {}
    )
}