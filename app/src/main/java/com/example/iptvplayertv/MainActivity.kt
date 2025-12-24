package com.example.iptvplayertv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.iptvplayertv.presentation.account.AccountViewModel
import com.example.iptvplayertv.presentation.account.UserInfoScreen
import com.example.iptvplayertv.presentation.home.HomeScreen
import com.example.iptvplayertv.presentation.home.HomeViewModel
import com.example.iptvplayertv.presentation.livetv.LiveTvScreen
import com.example.iptvplayertv.presentation.livetv.LiveTvViewModel
import com.example.iptvplayertv.presentation.login.LoginScreen
import com.example.iptvplayertv.presentation.login.LoginViewModel
import com.example.iptvplayertv.presentation.player.PlayerScreen
import com.example.iptvplayertv.ui.theme.IPTVPlayerTVTheme
import dagger.hilt.android.AndroidEntryPoint
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPTVPlayerTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    IPTVPlayerApp()
                }
            }
        }
    }
}

@Composable
fun IPTVPlayerApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            val viewModel: LoginViewModel = hiltViewModel()
            LoginScreen(
                viewModel = viewModel,
                onSuccess = {
                    navController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("home") {
            val viewModel: HomeViewModel = hiltViewModel()
            HomeScreen(
                viewModel = viewModel,
                onNavigateToLiveTV = {
                    navController.navigate("live_tv")
                },
                onNavigateToMovies = { /* TODO */ },
                onNavigateToSeries = { /* TODO */ },
                onNavigateToAccount = {
                    navController.navigate("account")
                },
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("home") { inclusive = true }
                    }
                }
            )
        }

        composable("account") {
            val viewModel: AccountViewModel = hiltViewModel()
            UserInfoScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        composable("live_tv") {
            val viewModel: LiveTvViewModel = hiltViewModel()
            LiveTvScreen(
                viewModel = viewModel,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToPlayer = { streamUrl, channelName, channelNumber ->
                    val encodedUrl = URLEncoder.encode(streamUrl, StandardCharsets.UTF_8.toString())
                    val encodedName = URLEncoder.encode(channelName, StandardCharsets.UTF_8.toString())
                    navController.navigate("player/$encodedUrl/$encodedName/$channelNumber")
                }
            )
        }

        composable(
            route = "player/{streamUrl}/{channelName}/{channelNumber}",
            arguments = listOf(
                navArgument("streamUrl") { type = NavType.StringType },
                navArgument("channelName") { type = NavType.StringType },
                navArgument("channelNumber") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val encodedUrl = backStackEntry.arguments?.getString("streamUrl") ?: ""
            val encodedName = backStackEntry.arguments?.getString("channelName") ?: ""
            val channelNumber = backStackEntry.arguments?.getInt("channelNumber") ?: 0

            val streamUrl = URLDecoder.decode(encodedUrl, StandardCharsets.UTF_8.toString())
            val channelName = URLDecoder.decode(encodedName, StandardCharsets.UTF_8.toString())

            PlayerScreen(
                streamUrl = streamUrl,
                channelName = channelName,
                channelNumber = channelNumber,
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}