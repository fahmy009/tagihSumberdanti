package com.example.tagihsumberdanti

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.tagihsumberdanti.data.local.SessionManager
import com.example.tagihsumberdanti.ui.admin.AdminMainScreen
import com.example.tagihsumberdanti.ui.admin.AdminViewModel
import com.example.tagihsumberdanti.ui.login.LoginScreen
import com.example.tagihsumberdanti.ui.login.LoginViewModel
import com.example.tagihsumberdanti.ui.penagih.TukangTagihScreen
import com.example.tagihsumberdanti.ui.penagih.TukangTagihViewModel
import com.example.tagihsumberdanti.ui.theme.TagihSumberdantiTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TagihSumberdantiTheme {
                MainAppNavHost()
            }
        }
    }
}

@Composable
fun MainAppNavHost() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val navController = rememberNavController()

    val startDest = if (sessionManager.isLoggedIn()) {
        if (sessionManager.getRole() == "Admin") "admin" else "penagih/${sessionManager.getUsername()}"
    } else {
        "login"
    }

    NavHost(
        navController = navController,
        startDestination = startDest
    ) {
        composable("login") {
            val loginViewModel: LoginViewModel = viewModel()
            LoginScreen(
                viewModel = loginViewModel,
                onLoginSuccess = { user ->
                    sessionManager.saveSession(user.username, user.role)
                    if (user.role == "Admin") {
                        navController.navigate("admin") {
                            popUpTo("login") { inclusive = true }
                        }
                    } else {
                        navController.navigate("penagih/${user.username}") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("admin") {
            val adminViewModel: AdminViewModel = viewModel()
            AdminMainScreen(
                viewModel = adminViewModel,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo("admin") { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = "penagih/{username}",
            arguments = listOf(
                navArgument("username") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val username = backStackEntry.arguments?.getString("username") ?: sessionManager.getUsername()
            val tukangTagihViewModel: TukangTagihViewModel = viewModel()
            TukangTagihScreen(
                collectorUsername = username,
                viewModel = tukangTagihViewModel,
                onLogout = {
                    sessionManager.clearSession()
                    navController.navigate("login") {
                        popUpTo("penagih/$username") { inclusive = true }
                    }
                }
            )
        }
    }
}
