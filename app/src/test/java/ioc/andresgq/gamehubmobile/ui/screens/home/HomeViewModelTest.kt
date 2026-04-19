package ioc.andresgq.gamehubmobile.ui.screens.home

import ioc.andresgq.gamehubmobile.navigation.MainNavigationViewModel
import ioc.andresgq.gamehubmobile.navigation.MainSection
import ioc.andresgq.gamehubmobile.navigation.MainTabRoutes
import org.junit.Assert.assertEquals
import org.junit.Test

class HomeViewModelTest {

    @Test
    fun resetToHome_alwaysReturnsToHomeSection() {
        val viewModel = MainNavigationViewModel()
        viewModel.selectSection(MainSection.PROFILE)

        viewModel.resetToHome()

        assertEquals(MainSection.HOME, viewModel.selectedSection.value)
    }

    @Test
    fun homeTabRoute_keepsExpectedMainShellRoute() {
        assertEquals("main/home", MainTabRoutes.HOME)
    }
}


