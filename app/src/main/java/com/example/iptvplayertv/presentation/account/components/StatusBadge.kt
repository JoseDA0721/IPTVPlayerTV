package com.example.iptvplayertv.presentation.account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import androidx.tv.material3.Text
import com.example.iptvplayertv.presentation.account.AccountStatus

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun StatusBadge(accountStatus: AccountStatus) {
    Surface(
        shape = RoundedCornerShape(50.dp),
        modifier = Modifier
            .background(when (accountStatus) {
                AccountStatus.ACTIVE -> Color.Green
                AccountStatus.EXPIRED -> Color.Red
                AccountStatus.WARNING -> Color.Yellow
                AccountStatus.INACTIVE -> Color.Gray
            })
    ){
        Text(
            text = accountStatus.toString(),
            color = Color.Black
        )
    }
}