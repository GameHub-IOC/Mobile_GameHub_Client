@file:Suppress("unused")

package ioc.andresgq.gamehubmobile.navigation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Secciones principales previstas para la navegación inferior.
 *
 * En esta fase se modelan solo las áreas base del flujo principal,
 * sin introducir todavía la UI completa de bottom navigation.
 */
enum class MainSection {
    HOME,
    RESERVE,
    MY_RESERVATIONS,
    CATALOG,
    ADMIN_RESERVATIONS,
    MANAGEMENT,
    USERS,
    PROFILE
}

/**
 * ViewModel que centraliza qué sección principal está activa.
 */
class MainNavigationViewModel : ViewModel() {

    private val _selectedSection = MutableStateFlow(MainSection.HOME)
    val selectedSection: StateFlow<MainSection> = _selectedSection.asStateFlow()

    fun selectSection(section: MainSection) {
        _selectedSection.value = section
    }

    fun resetToHome() {
        _selectedSection.value = MainSection.HOME
    }
}

