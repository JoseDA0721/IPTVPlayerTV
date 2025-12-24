package com.example.iptvplayertv.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.tv.material3.Text
import com.example.iptvplayertv.presentation.account.components.AccountInfoCard
import kotlinx.coroutines.launch

@Composable
fun UserInfoScreen(
    viewModel: AccountViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit = {}
) {
    var accountInfo by remember { mutableStateOf<AccountUiModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()

    // Cargar datos al iniciar
    LaunchedEffect(Unit) {
        isLoading = true
        errorMessage = null

        val result = viewModel.fetchAccountInfo()

        if (result != null) {
            accountInfo = result
        } else {
            errorMessage = "No se pudo cargar la información"
        }

        isLoading = false
    }

    // Función para refrescar
    fun refresh() {
        scope.launch {
            isLoading = true
            errorMessage = null

            val result = viewModel.fetchAccountInfo()

            if (result != null) {
                accountInfo = result
            } else {
                errorMessage = "No se pudo actualizar la información"
            }

            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        when {
            isLoading -> {
                // Loading indicator
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFD97706),
                        modifier = Modifier.size(64.dp)
                    )
                    Text(
                        text = "Cargando información...",
                        color = Color.White
                    )
                }
            }

            errorMessage != null -> {
                // Error message
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "❌ $errorMessage",
                        color = Color(0xFFFF5555)
                    )
                    androidx.tv.material3.Button(
                        onClick = { refresh() }
                    ) {
                        Text("Reintentar")
                    }
                }
            }

            accountInfo != null -> {
                // Mostrar información
                AccountInfoCard(
                    info = accountInfo!!
                )
            }
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF0D0D0D
)
@Composable
fun UserInfoScreenPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        AccountInfoCard(
            info = AccountUiModel(
                userName = "Juan Perez",
                hostUrl = "ott-server-premium.com",
                creationDateFormatted = "enero 01, 2024",
                expiryDateFormatted = "diciembre 15, 2024",
                isTrial = false,
                activeConnections = 1,
                maxConnections = 3,
                timeZone = "America/Thunder_Bay",
                status = AccountStatus.ACTIVE
            ),
            onRefresh = { /* TODO */ },
            onLogout = { /* TODO */ }
        )
    }
}