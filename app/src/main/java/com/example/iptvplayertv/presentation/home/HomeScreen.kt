package com.example.iptvplayertv.presentation.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.Text
import com.example.iptvplayertv.R
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToLiveTV: () -> Unit = {},
    onNavigateToMovies: () -> Unit = {},
    onNavigateToSeries: () -> Unit = {}
) {
    val state by viewModel.state

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1E1B4B))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar()

            Spacer(Modifier.height(40.dp))

            // Main Content - 3 Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                MainCard(
                    title = "TV DIRECTO",
                    backgroundColor = Brush.linearGradient(
                        colors = listOf(Color(0xFF06B6D4), Color(0xFF0891B2))
                    ),
                    icon = Icons.Default.Tv,
                    count = state.liveChannelsCount,
                    lastUpdate = state.lastUpdate,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLiveTV
                )

                MainCard(
                    title = "MOVIES",
                    backgroundColor = Brush.linearGradient(
                        colors = listOf(Color(0xFF8B5CF6), Color(0xFF7C3AED))
                    ),
                    icon = Icons.Default.PlayCircle,
                    count = state.moviesCount,
                    lastUpdate = state.lastUpdate,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMovies
                )

                MainCard(
                    title = "SERIES",
                    backgroundColor = Brush.linearGradient(
                        colors = listOf(Color(0xFFF97316), Color(0xFFEA580C))
                    ),
                    icon = Icons.Default.Movie,
                    count = state.seriesCount,
                    lastUpdate = state.lastUpdate,
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSeries
                )
            }

            Spacer(Modifier.weight(1f))

            // Bottom Info
            BottomInfo(
                userInfo = state.userInfo,
                onRefresh = { viewModel.refreshData() }
            )
        }

        // Loading overlay
        if (state.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Logo
        Box(
            modifier = Modifier
                .size(80.dp)
                .background(Color(0xFF1A1A1A), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "IPTV\nPLAYER",
                color = Color(0xFF00D9FF),
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.weight(1f))

        // Menu Icons
        Row(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TopBarButton(
                icon = Icons.Default.Notifications,
                label = "Notifications"
            )
            TopBarButton(
                icon = Icons.Default.Person,
                label = "User Info"
            )
            TopBarButton(
                icon = Icons.Default.SwapHoriz,
                label = "Change User"
            )
            TopBarButton(
                icon = Icons.Default.Settings,
                label = "Settings"
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopBarButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(80.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainCard(
    title: String,
    backgroundColor: Brush,
    icon: ImageVector,
    count: Int,
    lastUpdate: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(280.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor, RoundedCornerShape(20.dp))
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(80.dp)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                // Mostrar contador
                Text(
                    text = "$count disponibles",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )

                Spacer(Modifier.weight(1f))

                if (lastUpdate.isNotEmpty()) {
                    Text(
                        text = "Actualizado: $lastUpdate",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun BottomInfo(
    userInfo: UserDisplayInfo? = null,
    onRefresh: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 60.dp, vertical = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Expiration
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "Calendar",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Expiration: ${userInfo?.expDate ?: "N/A"}",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        // Logged In
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "User",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Logged In: ${userInfo?.username ?: "Guest"}",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        // Refresh Button
        Button(
            onClick = onRefresh,
            shape = RoundedCornerShape(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color.White
                )
                Text(
                    text = "ACTUALIZAR",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}