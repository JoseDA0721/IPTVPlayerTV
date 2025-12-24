package com.example.iptvplayertv.presentation.account.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.SurfaceDefaults
import androidx.tv.material3.Text
import com.example.iptvplayertv.presentation.account.AccountStatus

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatusBadge(accountStatus: AccountStatus) {
    val (color, text) = when (accountStatus) {
        AccountStatus.ACTIVE -> Color(0xFF4CAF50) to "ACTIVA"
        AccountStatus.EXPIRED -> Color(0xFFF44336) to "EXPIRADA"
        AccountStatus.WARNING -> Color(0xFFFF9800) to "ADVERTENCIA"
        AccountStatus.INACTIVE -> Color.Gray to "INACTIVA"
    }

    Surface(
        shape = RoundedCornerShape(50.dp),
        colors = SurfaceDefaults.colors(
            containerColor = color
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = Color.White
        )
    }
}