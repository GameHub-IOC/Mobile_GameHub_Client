package ioc.andresgq.gamehubmobile.navigation

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole

/**
 * Resuelve el grafo protegido apropiado a partir del rol bruto de sesión.
 *
 * Mientras la UI principal definitiva se construye, el fallback seguro para
 * valores desconocidos será el grafo de usuario.
 */
fun resolveRoleGraphRoute(rawUserType: String?): String {
    return when (UserRole.fromRaw(rawUserType)) {
        UserRole.USER -> AppDestinations.UserGraph
        UserRole.ADMIN -> AppDestinations.AdminGraph
    }
}

