package com.example.iptvplayertv.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.IconButton
import androidx.tv.material3.IconButtonDefaults
import androidx.tv.material3.Text

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToLiveTV: () -> Unit = {},
    onNavigateToMovies: () -> Unit = {},
    onNavigateToSeries: () -> Unit = {},
    onNavigateToAccount: () -> Unit = {}, // ← Nuevo
    onLogout: () -> Unit = {}
) {
    val state by viewModel.state

    HomeScreenContent(
        state = state,
        onRefresh = { viewModel.refreshData() },
        onNavigateToLiveTV = onNavigateToLiveTV,
        onNavigateToMovies = onNavigateToMovies,
        onNavigateToSeries = onNavigateToSeries,
        onNavigateToAccount = onNavigateToAccount, // ← Nuevo
        onLogout = {
            viewModel.logout()
            onLogout()
        }
    )
}


@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeState, // Assuming your state class is called HomeState
    onRefresh: () -> Unit,
    onNavigateToLiveTV: () -> Unit,
    onNavigateToMovies: () -> Unit,
    onNavigateToSeries: () -> Unit,
    onNavigateToAccount: () -> Unit,
    onLogout: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            //Boton Logout
            TopBar(
                onNavigateToAccount = onNavigateToAccount,
                onLogout = onLogout
            )



            Spacer(Modifier.height(40.dp))

            // Main Content - 3 Cards
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(30.dp)
            ) {
                HomeSectionCard(
                    title = "TV DIRECTO",
                    icon = Icons.Default.Tv,
                    subtitle = "${state.liveChannelsCount} canales",
                    background = Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF1A1A1A)
                        )
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToLiveTV
                )

                HomeSectionCard(
                    title = "PELICULAS",
                    icon = Icons.Default.Movie,
                    subtitle = "${state.moviesCount} películas",
                    background = Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF1A1A1A)
                        )
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToMovies
                )

                HomeSectionCard(
                    title = "SERIES",
                    icon = Icons.Default.LocalMovies,
                    subtitle = "${state.seriesCount} series",
                    background = Brush.verticalGradient(
                        listOf(
                            Color(0xFF1A1A1A),
                            Color(0xFF1A1A1A)
                        )
                    ),
                    modifier = Modifier.weight(1f),
                    onClick = onNavigateToSeries
                )
            }

            Spacer(Modifier.weight(1f))

            // Bottom Info
            BottomInfo(
                userInfo = state.userInfo,
                onRefresh = onRefresh
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
                CircularProgressIndicator(color = Color(0xFFF5F5F5))
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TopBar(
    onNavigateToAccount: () -> Unit,
    onLogout: () -> Unit
) {
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
                .background(Color(0xFF0D0D0D), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "IPTV\nPLAYER",
                color = Color(0xFFF5F5F5),
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
                icon = Icons.Default.Person,
                label = "User Info",
                onClick = onNavigateToAccount
            )
            TopBarButton(
                icon = Icons.Default.SwapHoriz,
                label = "Change User"
            )
            TopBarButton(
                icon = Icons.Default.Settings,
                label = "Settings"
            )
            TopBarButton(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = "Logout",
                onClick = onLogout
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
        IconButton(
            onClick = onClick,
            colors = IconButtonDefaults.colors(
                containerColor = Color.Transparent,
                contentColor = Color(0xFFD97706),
                focusedContainerColor = Color(0xFF555555)
            )
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = Color(0xFFD97706),
                modifier = Modifier.size(28.dp)
            )
        }
        Text(
            text = label,
            color = Color(0xFFF5F5F5),
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HomeSectionCard(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector,
    subtitle: String? = null,
    background: Brush,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Button(
        onClick = onClick,
        // Use the TV version of ButtonDefaults
        colors = ButtonDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            pressedContainerColor = Color.Transparent
        ),
        modifier = modifier
            .height(280.dp)
            .onFocusChanged { isFocused = it.isFocused }
            .graphicsLayer {
                scaleX = if (isFocused) 1.08f else 1f
                scaleY = if (isFocused) 1.08f else 1f
            },
        shape = ButtonDefaults.shape(shape = RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background, RoundedCornerShape(20.dp))
                .border(
                    width = if (isFocused) 3.dp else 0.dp,
                    color = if (isFocused) Color(0xFFFBBF24) else Color.Transparent,
                    shape = RoundedCornerShape(20.dp)
                )
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
                    tint = Color(0xFFF5F5F5),
                    modifier = Modifier.size(80.dp)
                )

                Spacer(Modifier.height(20.dp))

                Text(
                    text = title,
                    color = Color(0xFFF5F5F5),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                subtitle?.let {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        text = it,
                        color = Color(0xFFF5F5F5).copy(alpha = 0.85f),
                        fontSize = 16.sp
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
                tint = Color(0xFFF5F5F5),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Expiration: ${userInfo?.expDate ?: "N/A"}",
                color = Color(0xFFF5F5F5),
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
                tint = Color(0xFFF5F5F5),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Logged In: ${userInfo?.username ?: "Guest"}",
                color = Color(0xFFF5F5F5),
                fontSize = 16.sp
            )
        }

        // Refresh Button
        Button(
            onClick = onRefresh,
            modifier = Modifier.height(48.dp),
            shape = ButtonDefaults.shape(RoundedCornerShape(8.dp)),
            colors = ButtonDefaults.colors(
                containerColor = Color(0xFFD97706),
                contentColor = Color(0xFF1F2937),
                focusedContainerColor = Color(0xFFD97706),
            ),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh",
                    tint = Color(0xFFF5F5F5),
                )
                Text(
                    text = "ACTUALIZAR",
                    color = Color(0xFFF5F5F5),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k"
)
@Composable
fun HomeScreenPreview() {
    // Provide dummy state data
    val dummyState = HomeState(
        liveChannelsCount = 100,
        moviesCount = 500,
        seriesCount = 50,
        lastUpdate = "2023-10-27",
        isLoading = false
    )

    HomeScreenContent(
        state = dummyState,
        onRefresh = {},
        onNavigateToLiveTV = {},
        onNavigateToMovies = {},
        onNavigateToSeries = {},
        onNavigateToAccount = {},
        onLogout = {}
    )
}
