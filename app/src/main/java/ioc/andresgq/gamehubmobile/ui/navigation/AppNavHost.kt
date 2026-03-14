package ioc.andresgq.gamehubmobile.ui.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.compose.rememberNavController
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.ui.screens.home.HomeRoute
import ioc.andresgq.gamehubmobile.ui.screens.home.HomeViewModel
import ioc.andresgq.gamehubmobile.ui.screens.home.HomeViewModelFactory
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginRoute
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginViewModel
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginViewModelFactory

@Composable
fun AppNavHost(
    authRepository: AuthRepository,
    onCloseApp: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = AppDestinations.SessionGate
    ) {
        composable(AppDestinations.SessionGate) {
            SessionGate(
                authRepository = authRepository,
                onAuthenticated = {
                    navController.navigate(AppDestinations.homeRoute(it.userType)) {
                        popUpTo(AppDestinations.SessionGate) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(AppDestinations.Login) {
                        popUpTo(AppDestinations.SessionGate) { inclusive = true }
                    }
                }
            )
        }

        composable(AppDestinations.Login) {
            val loginViewModel: LoginViewModel = viewModel(
                factory = LoginViewModelFactory(authRepository)
            )
            LoginRoute(
                viewModel = loginViewModel,
                onLoginSuccess = { session ->
                    navController.navigate(AppDestinations.homeRoute(session.userType)) {
                        popUpTo(AppDestinations.Login) { inclusive = true }
                    }
                }
            )
        }

        composable(
            route = AppDestinations.HomeRoutePattern,
            arguments = listOf(navArgument("userType") { type = NavType.StringType })
        ) { backStackEntry ->
            val userType = Uri.decode(backStackEntry.arguments?.getString("userType").orEmpty())
            val homeViewModel: HomeViewModel = viewModel(
                factory = HomeViewModelFactory(authRepository)
            )
            HomeRoute(
                viewModel = homeViewModel,
                userTypeFromRoute = userType,
                onLogoutSuccess = {
                    navController.navigate(AppDestinations.Login) {
                        popUpTo(AppDestinations.HomeRoutePattern) { inclusive = true }
                    }
                },
                onCloseApp = onCloseApp
            )
        }
    }
}

@Composable
private fun SessionGate(
    authRepository: AuthRepository,
    onAuthenticated: (UserSession) -> Unit,
    onUnauthenticated: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        val session = authRepository.getSession()
        if (session == null) {
            onUnauthenticated()
        } else {
            onAuthenticated(session)
        }
        isLoading = false
    }

    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}
