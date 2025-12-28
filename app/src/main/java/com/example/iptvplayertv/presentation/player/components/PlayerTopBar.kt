package com.example.iptvplayertv.presentation.player.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerTopBar(
    channelName: String,
    channelNumber: Int,
    categoryName: String = "",
    isLive: Boolean = true,
    customBadge: String? = null, // ← NUEVO: Badge personalizado
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Lado izquierdo: Botón back + Info del canal
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Botón de regresar
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(50.dp)
                    .background(
                        color = Color.White.copy(alpha = 0.1f),
                        shape = CircleShape
                    )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Volver",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            // Información del canal
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = channelName,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Canal $channelNumber",
                        color = Color(0xFFCCCCCC),
                        fontSize = 16.sp
                    )

                    if (categoryName.isNotEmpty()) {
                        Text(
                            text = "•",
                            color = Color(0xFFCCCCCC),
                            fontSize = 16.sp
                        )
                        Text(
                            text = categoryName,
                            color = Color(0xFFCCCCCC),
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }

        // Lado derecho: Badge
        // ⚠️ CAMBIO IMPORTANTE: Usar customBadge si existe, sino usar "EN VIVO" para isLive
        val badgeText = customBadge ?: if (isLive) "EN VIVO" else null

        if (badgeText != null) {
            Box(
                modifier = Modifier
                    .background(
                        color = Color(0xFFD97706),
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Punto rojo parpadeante (solo para "EN VIVO")
                    if (badgeText == "EN VIVO") {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .background(Color.Red, shape = CircleShape)
                        )
                    }
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
)
@Composable
fun PlayerTopBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PlayerTopBar(
            channelName = "Discovery Channel",
            channelNumber = 101,
            categoryName = "Documentales",
            isLive = true,
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Movie Badge Preview",
    device = "id:tv_4k",
)
@Composable
fun MovieTopBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PlayerTopBar(
            channelName = "Avatar: El Camino del Agua",
            channelNumber = 12345,
            categoryName = "Ciencia Ficción",
            isLive = false,
            customBadge = "PELÍCULA",
            onBackClick = {}
        )
    }
}

@Preview(
    name = "Series Badge Preview",
    device = "id:tv_4k",
)
@Composable
fun SeriesTopBarPreview() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        PlayerTopBar(
            channelName = "Breaking Bad",
            channelNumber = 1,
            categoryName = "Ozymandias",
            isLive = false,
            customBadge = "T5:E14",
            onBackClick = {}
        )
    }
}