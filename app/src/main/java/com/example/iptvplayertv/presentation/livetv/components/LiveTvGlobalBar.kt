package com.example.iptvplayertv.presentation.livetv.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.LocalMovies
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Tv
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ClickableSurfaceDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Surface

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SidebarMenu() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .background(Color(0xFF1A1A1A))
            .padding(vertical = 20.dp)
    ) {
        // Iconos centrados verticalmente
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            SidebarIcon(Icons.Default.Home, "Home", false)
            SidebarIcon(Icons.Default.Search, "Search", false)
            SidebarIcon(Icons.Default.LiveTv, "Live TV", false)
            SidebarIcon(Icons.Default.Movie, "Movies", false)
            SidebarIcon(Icons.Default.LocalMovies, "Series", false)
        }

        // Settings en la parte inferior
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            SidebarIcon(Icons.Default.Settings, "Settings", false)
        }
    }
}
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SidebarIcon(icon: ImageVector, label: String, isActive: Boolean) {
    // Usamos Surface para manejo de foco en TV
    Surface(
        onClick = { /* Navegación global */ },
        shape = ClickableSurfaceDefaults.shape(shape = MaterialTheme.shapes.small),
        colors = ClickableSurfaceDefaults.colors(
            containerColor = Color.Transparent,
            focusedContainerColor = Color(0xFFD97706),
            contentColor = Color(0xFFF5F5F5),
            focusedContentColor = Color(0xFFF5F5F5)
        ),
        modifier = Modifier.size(40.dp)
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center) {
            Icon(imageVector = icon, contentDescription = label)
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
)
@Composable
fun SidebarMenuPreview(){
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ){
        Box(
            modifier = Modifier
                .fillMaxWidth(0.05f)
                .fillMaxHeight()
                .background(Color(0xFF1A1A1A))
        ){
            SidebarMenu()
        }

    }
}