package com.example.iptvplayertv.presentation.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

    LoginScreenContent(
        state = state,
        onEvent = viewModel::onEvent,
        onSuccess = onSuccess
    )
}
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoginScreenContent(
    state: LoginState,
    onEvent: (LoginEvent) -> Unit,
    onSuccess: () -> Unit
) {
    // Estado para mostrar/ocultar password
    var isPasswordVisible by remember { mutableStateOf(false) }

    // Navegar cuando el login sea exitoso
    LaunchedEffect(state.success) {
        if (state.success) onSuccess()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(700.dp)
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Título principal
            Text(
                text = "IPTV Player",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5F5F5)
            )

            Spacer(Modifier.height(8.dp))

            // Subtítulo
            Text(
                text = "Xtream Login",
                fontSize = 24.sp,
                color = Color(0xFFBBBBBB)
            )

            Spacer(Modifier.height(32.dp))

            // Campo Host
            Text(
                text = "Host (http://ip:port)",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            TvTextField(
                value = state.host,
                onValueChange = { onEvent(LoginEvent.HostChanged(it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Next
                ),
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Campo Usuario
            Text(
                text = "Usuario",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            TvTextField(
                value = state.username,
                onValueChange = { onEvent(LoginEvent.UsernameChanged(it)) },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            // Campo Contraseña
            Text(
                text = "Contraseña",
                fontSize = 14.sp,
                color = Color(0xFFCCCCCC),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp)
            )

            TvTextField(
                value = state.password,
                onValueChange = { onEvent(LoginEvent.PasswordChanged(it)) },
                isPassword = !isPasswordVisible,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(
                        onClick = { isPasswordVisible = !isPasswordVisible }
                    ) {
                        Icon(
                            imageVector = if (isPasswordVisible) {
                                Icons.Filled.Visibility
                            } else {
                                Icons.Filled.VisibilityOff
                            },
                            contentDescription = if (isPasswordVisible) {
                                "Ocultar contraseña"
                            } else {
                                "Mostrar contraseña"
                            },
                            tint = Color(0xFFCCCCCC)
                        )
                    }
                }
            )

            Spacer(Modifier.height(28.dp))

            // Botón Conectar con Loading
            TvButton(
                onClick = { onEvent(LoginEvent.Submit) },
                enabled = !state.isLoading,
                modifier = Modifier
                    .width(109.dp)
                    .height(40.dp)
            ) {
                if (state.isLoading) {
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFFF5F5F5),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            text = "Conectando...",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "Conectar",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // Mensaje de Error
            state.error?.let { error ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFFCC0000),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "❌ $error",
                        color = Color(0xFFF5F5F5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Mensaje de Éxito
            if (state.success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color(0xFF00AA00),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(12.dp)
                ) {
                    Text(
                        text = "✓ ¡Conexión exitosa!",
                        color = Color(0xFFF5F5F5),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k"
)
@Composable
fun LoginScreenPreview() {
    // Now the preview doesn't need a ViewModel at all!
    LoginScreenContent(
        state = LoginState(host = "http://192.168.1.1"),
        onEvent = {},
        onSuccess = {}
    )
}