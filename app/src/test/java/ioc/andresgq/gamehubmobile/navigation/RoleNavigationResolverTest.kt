package ioc.andresgq.gamehubmobile.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class RoleNavigationResolverTest {

    @Test
    fun userRole_resolvesUserGraph() {
        assertEquals(AppDestinations.UserGraph, resolveRoleGraphRoute("USER"))
    }

    @Test
    fun adminRole_resolvesAdminGraph() {
        assertEquals(AppDestinations.AdminGraph, resolveRoleGraphRoute("ADMIN"))
    }

    @Test
    fun lowercaseAdmin_resolvesAdminGraph() {
        assertEquals(AppDestinations.AdminGraph, resolveRoleGraphRoute("admin"))
    }

    @Test
    fun unknownRole_fallsBackToUserGraph() {
        assertEquals(AppDestinations.UserGraph, resolveRoleGraphRoute("GUEST"))
    }

    @Test
    fun nullRole_fallsBackToUserGraph() {
        assertEquals(AppDestinations.UserGraph, resolveRoleGraphRoute(null))
    }
}

