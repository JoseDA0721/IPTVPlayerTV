package com.example.iptvplayertv.presentation.account.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ConnectionBar (
    current: Int,
    max: Int
){
    val progress = current.toFloat() / max.toFloat()

    Box(
        modifier = Modifier
            .height(12.dp)
            .clip(RoundedCornerShape(10.dp))
    ){
        Box (
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(
                    listOf(
                        Color(0xFF4CA1F5),
                        Color(0xFF7F00B2)
                    )
                ))
        )
    }
}