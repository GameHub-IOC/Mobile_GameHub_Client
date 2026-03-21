package ioc.andresgq.gamehubmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ioc.andresgq.gamehubmobile.ui.navigation.AppNavHost
import ioc.andresgq.gamehubmobile.ui.theme.GameHubMobileTheme

/**
 * Actividad principal de la aplicación.
 *
 * Esta clase actúa como punto de entrada de la interfaz en Android y se encarga
 * de inicializar el contenido Compose de la app. Además, obtiene el contenedor
 * de dependencias definido en [GameHubApplication] para pasar al árbol de UI
 * los repositorios necesarios, como el de autenticación.
 */
class MainActivity : ComponentActivity() {

    /**
     * Se ejecuta cuando la actividad es creada.
     *
     * En este método:
     * - se recupera el contenedor global de dependencias desde la aplicación,
     * - se habilita el modo edge-to-edge para que la UI pueda dibujarse bajo
     *   las barras del sistema,
     * - y se establece el contenido Compose aplicando el tema visual de la app
     *   y cargando el host de navegación principal.
     *
     * @param savedInstanceState estado previo de la actividad, si existe.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Obtiene el contenedor de dependencias compartido por toda la aplicación.
        val appContainer = (application as GameHubApplication).container

        // Permite que la interfaz use toda la pantalla, incluyendo el área
        // detrás de las barras del sistema.
        enableEdgeToEdge()

        // Define el contenido de la actividad usando Jetpack Compose.
        setContent {
            // Aplica el tema visual global de la aplicación.
            GameHubMobileTheme {
                // Carga la navegación principal e inyecta el repositorio de autenticación.
                AppNavHost(
                    authRepository = appContainer.authRepository,
                    onCloseApp = { finish() }
                )
            }
        }
    }
}