package com.example.iptvplayertv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Surface
import com.example.iptvplayertv.data.local.dao.SessionDao
import com.example.iptvplayertv.data.preferences.UserPreferences
import com.example.iptvplayertv.presentation.account.AccountViewModel
import com.example.iptvplayertv.presentation.account.UserInfoScreen
import com.example.iptvplayertv.presentation.home.HomeScreen
import com.example.iptvplayertv.presentation.home.HomeViewModel
import com.example.iptvplayertv.presentation.livetv.LiveTvScreen
import com.example.iptvplayertv.presentation.livetv.LiveTvViewModel
import com.example.iptvplayertv.presentation.login.LoginScreen
import com.example.iptvplayertv.presentation.login.LoginViewModel
import com.example.iptvplayertv.presentation.player.PlayerConfiguration
import com.example.iptvplayertv.presentation.player.PlayerSharedViewModel
import com.example.iptvplayertv.presentation.player.UniversalPlayerScreen
import com.example.iptvplayertv.ui.theme.IPTVPlayerTVTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sessionDao: SessionDao

    @Inject
    lateinit var userPreferences: UserPreferences

    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IPTVPlayerTVTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    shape = RectangleShape
                ) {
                    // ✅ NUEVO: Splash Screen mientras verifica sesión
                    SplashScreenWithAutoLogin(
                        sessionDao = sessionDao,
                        userPreferences = userPreferences
                    )
                }
            }
        }
    }
}

/**
 * ✅ Pantalla de carga inicial que verifica sesión
 */
@Composable
fun SplashScreenWithAutoLogin(
    sessionDao: SessionDao,
    userPreferences: UserPreferences
) {
    var isChecking by remember { mutableStateOf(true) }
    var hasValidSession by remember { mutableStateOf(false) }

    // ✅ Verificar sesión una sola vez
    LaunchedEffect(Unit) {
        val session = sessionDao.getSessionOnce()
        val credentials = userPreferences.userCredentials.firstOrNull()

        hasValidSession = session != null &&
                session.isLoggedIn &&
                session.autoLoginEnabled &&
                credentials != null

        isChecking = false
    }

    if (isChecking) {
        // Mostrar splash screen
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0D0D0D)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color(0xFFD97706))
        }
    } else {
        // Mostrar navegación
        IPTVPlayerApp(startWithHome = hasValidSession)
    }
}

@Composable
fun IPTVPlayerApp(startWithHome: Boolean = false) {
    val navController = rememberNavController()
    val playerSharedViewModel: PlayerSharedViewModel = hiltViewModel()

    // ✅ Definir ruta inicial según si hay sesión
    val startDestination = if (startWithHome) "home" else "login"

    NavHost(
        navController = navController,
        startDestination = startDestination
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
                    // ✅ Limpiar sesión al cerrar sesión
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
                    playerSharedViewModel.setPlayerConfig(
                        PlayerConfiguration.forLiveTV(
                            streamUrl = streamUrl,
                            channelName = channelName,
                            channelNumber = channelNumber,
                            categoryName = ""
                        )
                    )
                    navController.navigate("player")
                }
            )
        }

        composable("player") {
            val config = playerSharedViewModel.currentConfig

            if (config != null) {
                UniversalPlayerScreen(
                    config = config,
                    onNavigateBack = {
                        playerSharedViewModel.clearConfig()
                        navController.popBackStack()
                    }
                )
            } else {
                navController.popBackStack()
            }
        }
    }
}