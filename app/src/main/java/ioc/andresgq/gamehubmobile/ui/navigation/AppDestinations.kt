package ioc.andresgq.gamehubmobile.ui.navigation

import android.net.Uri

/**
 * Define las rutas de navegación principales de la aplicación.
 *
 * Este objeto centraliza los identificadores de destino usados por el sistema
 * de navegación, evitando strings dispersos por el código y facilitando la
 * construcción de rutas dinámicas.
 */
object AppDestinations {

    /**
     * Ruta del destino que decide a qué pantalla enviar al usuario
     * en función de si existe o no una sesión activa.
     */
    const val SessionGate = "session_gate"

    /**
     * Ruta de la pantalla de inicio de sesión.
     */
    const val Login = "login"

    /**
     * Ruta de la pantalla de registro.
     */
    const val Register = "register"

    /**
     * Patrón de ruta de la pantalla principal.
     *
     * Incluye el argumento dinámico `userType`, que permite adaptar
     * la navegación o la UI según el tipo de usuario autenticado.
     */
    const val HomeRoutePattern = "home/{userType}"

    /**
     * Construye la ruta real hacia la pantalla principal a partir del tipo de usuario.
     *
     * El valor de `userType` se codifica con [Uri.encode] para evitar problemas
     * si contiene espacios u otros caracteres especiales no válidos en una ruta.
     *
     * @param userType tipo de usuario que se insertará en la ruta.
     * @return la ruta completa en formato `home/<userType-codificado>`.
     */
    fun homeRoute(userType: String): String = "home/${Uri.encode(userType)}"
}