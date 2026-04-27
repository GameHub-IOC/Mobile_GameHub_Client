@file:Suppress("unused")

package ioc.andresgq.gamehubmobile.navigation

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole

/**
 * Descriptor mínimo de una pestaña principal de la app.
 */
data class BottomTabSpec(
    val route: String,
    val label: String,
    val section: MainSection
)

/**
 * Rutas internas del shell principal con bottom navigation.
 */
object MainTabRoutes {
    const val HOME = "main/home"
    const val RESERVE = "main/reserve"
    const val MY_RESERVATIONS = "main/my-reservations"
    const val CATALOG = "main/catalog"
    const val ADMIN_RESERVATIONS = "main/admin/reservations"
    const val MANAGEMENT = "main/admin/management"
    const val USERS = "main/admin/users"
    const val PROFILE = "main/profile"
}

/**
 * Devuelve las tabs visibles según rol.
 */
fun bottomTabsForRole(role: UserRole): List<BottomTabSpec> {
    return when (role) {
        UserRole.USER -> listOf(
            BottomTabSpec(MainTabRoutes.HOME, "Inicio", MainSection.HOME),
            BottomTabSpec(MainTabRoutes.RESERVE, "Reservar", MainSection.RESERVE),
            BottomTabSpec(MainTabRoutes.MY_RESERVATIONS, "Mis reservas", MainSection.MY_RESERVATIONS),
            BottomTabSpec(MainTabRoutes.CATALOG, "Catalogo", MainSection.CATALOG),
            BottomTabSpec(MainTabRoutes.PROFILE, "Perfil", MainSection.PROFILE)
        )

        UserRole.ADMIN -> listOf(
            BottomTabSpec(MainTabRoutes.HOME, "Inicio", MainSection.HOME),
            BottomTabSpec(MainTabRoutes.ADMIN_RESERVATIONS, "Reservas", MainSection.ADMIN_RESERVATIONS),
            BottomTabSpec(MainTabRoutes.MANAGEMENT, "Gestion", MainSection.MANAGEMENT),
            BottomTabSpec(MainTabRoutes.USERS, "Usuarios", MainSection.USERS),
            BottomTabSpec(MainTabRoutes.PROFILE, "Perfil", MainSection.PROFILE)
        )
    }
}

