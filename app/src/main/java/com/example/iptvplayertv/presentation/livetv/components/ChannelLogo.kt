package com.example.iptvplayertv.presentation.livetv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImagePainter
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.SubcomposeAsyncImageContent
import coil3.request.ImageRequest
import coil3.request.allowHardware
import coil3.request.crossfade

/**
 * ✅ Componente optimizado para mostrar logos de canales
 * Soporta:
 * - Carga desde URL con Coil
 * - Estados: Loading, Success, Error
 * - Placeholder cuando no hay logo
 * - Caché automático
 */
@Composable
fun ChannelLogo(
    logoUrl: String?,
    contentDescription: String = "Logo del canal",
    size: Dp = 50.dp,
    modifier: Modifier = Modifier
) {
    // ✅ FIX 1: Validar que la URL sea válida antes de intentar cargar
    val isValidUrl = !logoUrl.isNullOrBlank() &&
            (logoUrl.startsWith("http://") || logoUrl.startsWith("https://"))

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1A1A1A)), // ✅ FIX 2: Fondo más visible
        contentAlignment = Alignment.Center
    ) {
        when {
            // Si no hay URL válida, mostrar ícono genérico
            !isValidUrl -> {
                Icon(
                    imageVector = Icons.Default.LiveTv,
                    contentDescription = contentDescription,
                    tint = Color(0xFFD97706), // ✅ FIX 3: Color más visible
                    modifier = Modifier.size(size * 0.5f)
                )
            }

            // Si hay URL válida, intentar cargar con Coil
            else -> {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(logoUrl)
                        .crossfade(300)
                        // ✅ FIX 4: Configuración robusta
                        .allowHardware(false) // Desactivar hardware decoding para compatibilidad
                        .build(),
                    contentDescription = contentDescription,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1A1A1A)) // Fondo visible mientras carga
                ) {
                    when (val state = painter.state) {
                        // ⏳ Cargando
                        is AsyncImagePainter.State.Loading -> {
                            CircularProgressIndicator(
                                color = Color(0xFFD97706),
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(size * 0.4f)
                            )
                        }

                        // ✅ Carga exitosa
                        is AsyncImagePainter.State.Success -> {
                            SubcomposeAsyncImageContent()
                        }

                        // ❌ Error al cargar
                        is AsyncImagePainter.State.Error -> {
                            // ✅ FIX 5: Log del error para debugging
                            android.util.Log.e(
                                "ChannelLogo",
                                "Error cargando logo: $logoUrl - ${state.result.throwable.message}"
                            )

                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = "Error cargando logo",
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(size * 0.5f)
                            )
                        }

                        // ⭕ Estado vacío
                        is AsyncImagePainter.State.Empty -> {
                            Icon(
                                imageVector = Icons.Default.LiveTv,
                                contentDescription = contentDescription,
                                tint = Color(0xFFD97706),
                                modifier = Modifier.size(size * 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * ✅ Variante más simple sin estados visuales
 * Útil cuando necesitas mejor rendimiento
 */
@Composable
fun ChannelLogoSimple(
    logoUrl: String?,
    contentDescription: String = "Logo del canal",
    size: Dp = 50.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF000000)),
        contentAlignment = Alignment.Center
    ) {
        if (logoUrl.isNullOrBlank()) {
            Icon(
                imageVector = Icons.Default.LiveTv,
                contentDescription = contentDescription,
                tint = Color(0xFF555555),
                modifier = Modifier.size(size * 0.5f)
            )
        } else {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(logoUrl)
                    .crossfade(200)
                    .build(),
                contentDescription = contentDescription,
                contentScale = ContentScale.Fit,
                error = {
                    Icon(
                        imageVector = Icons.Default.BrokenImage,
                        contentDescription = "Error",
                        tint = Color(0xFF555555),
                        modifier = Modifier.size(size * 0.5f)
                    )
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}