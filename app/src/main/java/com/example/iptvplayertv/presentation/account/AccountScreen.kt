package com.example.iptvplayertv.presentation.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.iptvplayertv.presentation.account.components.AccountInfoCard

@Composable
fun UserInfoScreen(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        contentAlignment = Alignment.Center
    ) {
        AccountInfoCard(
            info = AccountUiModel(
                userName = "Juan Perez",
                hostUrl = "http://ott-server-premium.com",
                creationDateFormatted = "01 Ene 2024",
                expiryDateFormatted = "15 Dic 2024",
                isTrial = false,
                activeConnections = 1,
                maxConnections = 3,
                timeZone = "00:1A:2B:3C:4D:5E",
                status = AccountStatus.ACTIVE
            ),
        )
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
    UserInfoScreen()
}