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
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import ioc.andresgq.gamehubmobile.data.model.UserSession
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.ui.screens.home.HomeRoute
import ioc.andresgq.gamehubmobile.ui.screens.home.HomeViewModel
import ioc.andresgq.gamehubmobile.ui.screens.home.HomeViewModelFactory
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginRoute
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginViewModel
import ioc.andresgq.gamehubmobile.ui.screens.login.LoginViewModelFactory
import ioc.andresgq.gamehubmobile.ui.screens.register.RegisterRoute
import ioc.andresgq.gamehubmobile.ui.screens.register.RegisterViewModel
import ioc.andresgq.gamehubmobile.ui.screens.register.RegisterViewModelFactory

/**
 * Host principal de navegación de la aplicación.
 *
 * Este composable define el grafo de navegación de Jetpack Compose y conecta
 * las pantallas principales de la app:
 * - una pantalla intermedia que comprueba si existe sesión (`SessionGate`),
 * - la pantalla de login,
 * - y la pantalla principal (`HomeRoute`).
 *
 * Además, crea un único `NavController` con [rememberNavController] y se encarga
 * de construir los `ViewModel` necesarios para cada pantalla mediante sus fábricas.
 *
 * @param authRepository repositorio de autenticación usado para consultar la sesión,
 * iniciar sesión y cerrar sesión desde las distintas pantallas.
 * @param onCloseApp callback que permite cerrar la aplicación desde la pantalla principal.
 */
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
        /**
         * Destino inicial que decide si el usuario debe ir al login o directamente
         * a la pantalla principal en función de si existe una sesión persistida.
         */
        composable(AppDestinations.SessionGate) {
            SessionGate(
                authRepository = authRepository,
                onAuthenticated = {
                    navController.navigate(AppDestinations.homeRoute(it.userType)) {
                        /**
                         * Elimina `SessionGate` del back stack para que el usuario
                         * no pueda volver a esta pantalla intermedia al pulsar atrás.
                         */
                        popUpTo(AppDestinations.SessionGate) { inclusive = true }
                    }
                },
                onUnauthenticated = {
                    navController.navigate(AppDestinations.Login) {
                        /**
                         * Elimina `SessionGate` del back stack tras redirigir al login.
                         */
                        popUpTo(AppDestinations.SessionGate) { inclusive = true }
                    }
                }
            )
        }

        /**
         * Destino de la pantalla de autenticación.
         *
         * Aquí se crea el [LoginViewModel] usando su factoría y se pasa a [LoginRoute].
         * Si el login es correcto, se navega a la home con el tipo de usuario
         * codificado en la ruta.
         *
         * @param authRepository repositorio de autenticación usado para iniciar sesión.
         * @param onCloseApp callback que permite cerrar la aplicación desde la pantalla principal.
         * @param onNavigateToRegister callback que permite navegar a la pantalla de registro.
         */
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
                },
                onNavigateToRegister = {
                    navController.navigate(AppDestinations.Register)
                }
            )
        }


        /**
         * Destino de la pantalla de registro.
         * Similar al login, se crea el [RegisterViewModel] con su factoría y se pasa a [RegisterRoute].
         * Si el registro es correcto, se navega al login para que el usuario acceda.
         *
         * @param authRepository repositorio de autenticación usado para registrar al usuario.
         * @param onCloseApp callback que permite cerrar la aplicación desde la pantalla principal.
         */
        composable(AppDestinations.Register) {
            val registerViewModel: RegisterViewModel = viewModel(
                factory = RegisterViewModelFactory(authRepository)
            )
            RegisterRoute(
                viewModel = registerViewModel,
                onRegisterSuccess = {
                    navController.navigate(AppDestinations.Login) {
                        popUpTo(AppDestinations.Register) { inclusive = true }
                    }
                }
            )
        }

        /**
         * Destino de la pantalla principal.
         *
         * La ruta recibe un argumento dinámico llamado `userType`. Se decodifica
         * con [Uri.decode] para recuperar su valor original antes de pasarlo a la UI.
         * También se crea aquí el [HomeViewModel] mediante su factoría.
         */
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
                        /**
                         * Al cerrar sesión, se elimina la pantalla principal del stack
                         * para impedir volver atrás a una pantalla protegida.
                         */
                        popUpTo(AppDestinations.HomeRoutePattern) { inclusive = true }
                    }
                },
                onCloseApp = onCloseApp
            )
        }
    }
}

/**
 * Pantalla intermedia encargada de comprobar si existe una sesión activa.
 *
 * Este composable no muestra contenido funcional más allá de un indicador de carga
 * temporal mientras consulta el estado de la sesión. Su responsabilidad es decidir
 * la redirección inicial de la app:
 * - si hay sesión, llama a [onAuthenticated],
 * - si no la hay, llama a [onUnauthenticated].
 *
 * La comprobación se realiza una sola vez al entrar en composición mediante
 * [LaunchedEffect] con clave `Unit`.
 *
 * @param authRepository repositorio usado para recuperar la sesión persistida.
 * @param onAuthenticated callback invocado cuando se encuentra una sesión válida.
 * Recibe la [UserSession] recuperada.
 * @param onUnauthenticated callback invocado cuando no existe sesión activa.
 */
@Composable
private fun SessionGate(
    authRepository: AuthRepository,
    onAuthenticated: (UserSession) -> Unit,
    onUnauthenticated: () -> Unit
) {
    /**
     * Estado local que controla si debe mostrarse el indicador de carga.
     *
     * Empieza en `true` y pasa a `false` una vez finaliza la comprobación
     * de la sesión, independientemente del resultado.
     */
    var isLoading by remember { mutableStateOf(true) }

    /**
     * Efecto lanzado al entrar por primera vez en composición.
     *
     * Consulta la sesión almacenada y redirige al usuario según exista o no.
     */
    LaunchedEffect(Unit) {
        val session = authRepository.getSession()
        if (session == null) {
            onUnauthenticated()
        } else {
            onAuthenticated(session)
        }
        isLoading = false
    }

    /**
     * Mientras se realiza la comprobación, se muestra un spinner centrado
     * ocupando toda la pantalla.
     */
    if (isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}