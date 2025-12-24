package com.example.iptvplayertv.presentation.account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
fun AccountInfoCard (
    info: AccountUiModel
){
    Surface(
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .background(Color.Transparent)
            .border(width =1.dp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
            )
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = Color.Black.copy(alpha = 0.3f),
                spotColor = Color.Black.copy(alpha = 0.3f)
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

@Composable
fun DataGrid(
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
        ){
            DataItem(
                label = "Host URL",
                value = hostUrl,
                modifier = Modifier.weight(1f)
            )

            DataItem(
                label = "Fecha de creación",
                value = creationDate,
                modifier = Modifier.weight(1f)
            )
        }

        // Segunda fila

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ){
            DataItem(
                label = "Fecha de expiración",
                value = expiryDate,
                modifier = Modifier.weight(1f)
            )

            DataItem(
                label = "Prueba gratuita",
                value = if (isTrial) "Sí" else "No",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun HeaderSection(
    userName: String,
    status: AccountStatus
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxWidth(),
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
            ){
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
        ){
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

