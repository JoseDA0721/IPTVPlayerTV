package com.example.iptvplayertv.presentation.account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.iptvplayertv.presentation.account.AccountStatus
import com.example.iptvplayertv.presentation.account.AccountUiModel

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun AccountInfoCard(
    info: AccountUiModel
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
            )
            .fillMaxWidth(0.5f)
            .widthIn(min = 400.dp, max = 600.dp),
        color = Color(0xFF1A1A1A),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Header
            HeaderSection(
                userName = info.userName,
                status = info.status
            )

            // Divisor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.1f))
            )

            // Grid de datos 2x2
            DataGrid(
                hostUrl = info.hostUrl,
                creationDate = info.creationDateFormatted,
                expiryDate = info.expiryDateFormatted,
                isTrial = info.isTrial
            )

            // Barra de conexiones
            ConnectionSection(
                activeConnections = info.activeConnections,
                maxConnections = info.maxConnections
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun ActionButtons(
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        androidx.tv.material3.Button(
            onClick = onLogout,
            modifier = Modifier.weight(1f),
            colors = androidx.tv.material3.ButtonDefaults.colors(
                containerColor = Color(0xFF4A4D5E),
                contentColor = Color.White
            ),
            //shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Cerrar Sesión",
                style = MaterialTheme.typography.titleMedium
            )
        }

        androidx.tv.material3.Button(
            onClick = onRefresh,
            modifier = Modifier.weight(1f),
            colors = androidx.tv.material3.ButtonDefaults.colors(
                containerColor = Color.White,
                contentColor = Color(0xFF2C2E3A)
            ),
            //shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = "Actualizar",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun DataGrid(
    hostUrl: String,
    creationDate: String,
    expiryDate: String,
    isTrial: Boolean
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Primera fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            DataItem(
                label = "Servidor / Host",
                value = hostUrl,
                modifier = Modifier.weight(1f)
            )

            DataItem(
                label = "Fecha Inicio",
                value = creationDate,
                modifier = Modifier.weight(1f)
            )
        }

        // Segunda fila
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            DataItem(
                label = "Fecha Expiración",
                value = expiryDate,
                modifier = Modifier.weight(1f),
                valueColor = Color(0xFFFFA726)
            )

            DataItem(
                label = "Tipo de Cuenta",
                value = if (isTrial) "Prueba" else "No Prueba",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HeaderSection(
    userName: String,
    status: AccountStatus
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Surface(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape),
            color = Color(0xFF5B6EF5)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = userName.take(2).uppercase(),
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color(0xFFF8F8FA)
                )
            }
        }

        // Nombre y Estado
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = userName,
                style = MaterialTheme.typography.titleLarge,
                color = Color(0xFFF8F8FA)
            )
            StatusBadge(accountStatus = status)
        }
    }
}

@Composable
private fun DataItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = valueColor
        )
    }
}

@Composable
private fun ConnectionSection(
    activeConnections: Int,
    maxConnections: Int
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Dispositivos Activos",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Text(
                text = "$activeConnections / $maxConnections",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }

        ConnectionBar(
            current = activeConnections,
            max = maxConnections
        )
    }
}