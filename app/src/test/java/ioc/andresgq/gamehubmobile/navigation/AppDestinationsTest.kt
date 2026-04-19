package ioc.andresgq.gamehubmobile.navigation

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AppDestinationsTest {

    @Test
    fun protectedGraphs_haveExpectedRoutes() {
        assertEquals("user_graph", AppDestinations.UserGraph)
        assertEquals("admin_graph", AppDestinations.AdminGraph)
        assertEquals("user/home", AppDestinations.UserHome)
        assertEquals("admin/home", AppDestinations.AdminHome)
    }

    @Test
    fun gameDetailRoute_buildsExpectedPath() {
        assertEquals("game_detail/42", AppDestinations.gameDetailRoute(42L))
        assertEquals("game_detail/{gameId}", AppDestinations.GameDetailRoutePattern)
    }

    @Test
    fun authAndSessionDestinations_areNonBlank() {
        val routes = listOf(
            AppDestinations.SessionGate,
            AppDestinations.Login,
            AppDestinations.Register,
            AppDestinations.HomeRoutePattern
        )

        assertTrue(routes.all { it.isNotBlank() })
    }
}

