package ioc.andresgq.gamehubmobile.navigation

import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RoleBottomTabsTest {

    @Test
    fun userRole_exposesFiveUserTabsInExpectedOrder() {
        val tabs = bottomTabsForRole(UserRole.USER)

        assertEquals(5, tabs.size)
        assertEquals(MainSection.HOME, tabs[0].section)
        assertEquals(MainSection.RESERVE, tabs[1].section)
        assertEquals(MainSection.MY_RESERVATIONS, tabs[2].section)
        assertEquals(MainSection.CATALOG, tabs[3].section)
        assertEquals(MainSection.PROFILE, tabs[4].section)
    }

    @Test
    fun adminRole_exposesFiveAdminTabsInExpectedOrder() {
        val tabs = bottomTabsForRole(UserRole.ADMIN)

        assertEquals(5, tabs.size)
        assertEquals(MainSection.HOME, tabs[0].section)
        assertEquals(MainSection.ADMIN_RESERVATIONS, tabs[1].section)
        assertEquals(MainSection.MANAGEMENT, tabs[2].section)
        assertEquals(MainSection.USERS, tabs[3].section)
        assertEquals(MainSection.PROFILE, tabs[4].section)
    }

    @Test
    fun everyRole_hasUniqueRoutes() {
        val roles = listOf(UserRole.USER, UserRole.ADMIN)

        roles.forEach { role ->
            val tabs = bottomTabsForRole(role)
            assertTrue(tabs.map { it.route }.distinct().size == tabs.size)
        }
    }
}

