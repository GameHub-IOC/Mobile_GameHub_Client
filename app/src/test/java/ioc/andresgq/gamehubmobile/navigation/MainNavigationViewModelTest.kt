package ioc.andresgq.gamehubmobile.navigation

import org.junit.Assert.assertEquals
import org.junit.Test

class MainNavigationViewModelTest {

    @Test
    fun startsOnHome() {
        val viewModel = MainNavigationViewModel()

        assertEquals(MainSection.HOME, viewModel.selectedSection.value)
    }

    @Test
    fun selectSection_updatesCurrentSection() {
        val viewModel = MainNavigationViewModel()

        viewModel.selectSection(MainSection.RESERVE)

        assertEquals(MainSection.RESERVE, viewModel.selectedSection.value)
    }

    @Test
    fun resetToHome_returnsToHomeSection() {
        val viewModel = MainNavigationViewModel()
        viewModel.selectSection(MainSection.PROFILE)

        viewModel.resetToHome()

        assertEquals(MainSection.HOME, viewModel.selectedSection.value)
    }
}

