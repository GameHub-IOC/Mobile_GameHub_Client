package ioc.andresgq.gamehubmobile.ui.screens.main

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import ioc.andresgq.gamehubmobile.ui.screens.dashboard.ReservationStatusChip
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import ioc.andresgq.gamehubmobile.R
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.resolveGameThumbnailUrl
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.navigation.MainNavigationViewModel
import ioc.andresgq.gamehubmobile.navigation.MainSection
import ioc.andresgq.gamehubmobile.navigation.MainTabRoutes
import ioc.andresgq.gamehubmobile.navigation.bottomTabsForRole
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationStepValidation
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationTableOption
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationTurnOption
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationWizardState
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationWizardStep
import ioc.andresgq.gamehubmobile.ui.screens.admin.ManagementScreen
import ioc.andresgq.gamehubmobile.ui.screens.admin.UsersScreen
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameListScreen
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.CatalogViewModel
import ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameItemUi
import ioc.andresgq.gamehubmobile.ui.screens.dashboard.DashboardScreen
import ioc.andresgq.gamehubmobile.ui.screens.dashboard.DashboardViewModel
import ioc.andresgq.gamehubmobile.ui.screens.profile.ProfileInfo
import ioc.andresgq.gamehubmobile.ui.screens.profile.ProfileViewModel
import ioc.andresgq.gamehubmobile.ui.screens.reservation.ReservationFlowViewModel
import ioc.andresgq.gamehubmobile.ui.screens.reservations.AdminReservationsScreen
import ioc.andresgq.gamehubmobile.ui.screens.reservations.MyReservationsScreen
import ioc.andresgq.gamehubmobile.ui.screens.reservations.ReservationListItemUi
import ioc.andresgq.gamehubmobile.ui.screens.reservations.ReservationListViewModel
import ioc.andresgq.gamehubmobile.ui.state.UiState

/**
 * Shell principal de la aplicación una vez autenticado el usuario.
 *
 * Coordina la navegación por pestañas según el [role], conecta los distintos
 * ViewModel de catálogo, perfil y reservas, y centraliza varios efectos
 * laterales relevantes del flujo principal:
 * - sincronización de la pestaña seleccionada,
 * - cierre de sesión,
 * - recarga de reservas por rol,
 * - redirección automática tras crear una reserva.
 *
 * También actúa como punto de composición de las pantallas de primer nivel
 * mostradas dentro del `bottom navigation`.
 */

/**
 * Devuelve el icono adecuado para cada sección de la barra de navegación inferior.
 */
private fun iconForSection(section: MainSection) = when (section) {
    MainSection.HOME               -> Icons.Filled.Home
    MainSection.RESERVE            -> Icons.Filled.DateRange
    MainSection.MY_RESERVATIONS    -> Icons.Filled.List
    MainSection.CATALOG            -> Icons.Filled.Star
    MainSection.ADMIN_RESERVATIONS -> Icons.Filled.List
    MainSection.MANAGEMENT         -> Icons.Filled.Settings
    MainSection.USERS              -> Icons.Filled.Search
    MainSection.PROFILE            -> Icons.Filled.AccountCircle
}

@Composable
fun MainShellRoute(
    role: UserRole,
    catalogViewModel: CatalogViewModel,
    profileViewModel: ProfileViewModel,
    dashboardViewModel: DashboardViewModel,
    reservationFlowViewModel: ReservationFlowViewModel,
    reservationListViewModel: ReservationListViewModel,
    onGameClick: (Long) -> Unit,
    onLogoutSuccess: () -> Unit,
    onCloseApp: () -> Unit,
    modifier: Modifier = Modifier
) {
    val mainNavigationViewModel: MainNavigationViewModel = viewModel()
    val tabNavController = rememberNavController()

    val catalogState by catalogViewModel.catalogState.collectAsState()
    val profileState by profileViewModel.profileState.collectAsState()
    val logoutState by profileViewModel.logoutState.collectAsState()

    val wizardState by reservationFlowViewModel.wizardState.collectAsState()
    val stepValidation by reservationFlowViewModel.stepValidation.collectAsState()
    val submitState by reservationFlowViewModel.submitState.collectAsState()
    val submitFeedbackMessage by reservationFlowViewModel.submitFeedbackMessage.collectAsState()
    val needsTableRecovery by reservationFlowViewModel.needsTableRecovery.collectAsState()
    val turnOptionsState by reservationFlowViewModel.turnOptionsState.collectAsState()
    val tableOptionsState by reservationFlowViewModel.tableOptionsState.collectAsState()
    val allTablesState by reservationFlowViewModel.allTablesState.collectAsState()

    val myReservationsState by reservationListViewModel.myReservationsState.collectAsState()
    val adminReservationsState by reservationListViewModel.adminReservationsState.collectAsState()

    val tabs = remember(role) { bottomTabsForRole(role) }
    val startDestination = tabs.first().route

    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Mantiene sincronizado el estado visual de la barra inferior con la ruta real.
    LaunchedEffect(currentRoute, tabs) {
        val matchingSection = tabs.firstOrNull { it.route == currentRoute }?.section
        if (matchingSection != null) {
            mainNavigationViewModel.selectSection(matchingSection)
        }
    }

    // Cuando el logout termina con éxito, se consume el estado y se delega la salida.
    LaunchedEffect(logoutState) {
        if (logoutState is UiState.Success) {
            profileViewModel.consumeLogoutState()
            onLogoutSuccess()
        }
    }

    // Cada cambio de rol fuerza una recarga del listado de reservas correspondiente.
    LaunchedEffect(role) {
        reservationListViewModel.refreshForRole(role)
    }

    // Tras crear una reserva, se refresca el listado y se redirige a la pestaña
    // más relevante para el rol actual. El wizard se reinicia ANTES de navegar para
    // evitar que su estado se guarde (saveState) y se restaure luego en la pestaña HOME.
    LaunchedEffect(submitState) {
        if (submitState is UiState.Success) {
            reservationListViewModel.refreshForRole(role)

            // ① Reiniciamos el wizard PRIMERO para que el estado limpio sea
            //    el que quede en memoria; así el "saveState" del popUpTo no
            //    captura un borrador a medio rellenar.
            reservationFlowViewModel.resetFlow()

            val targetSection = if (role == UserRole.USER) {
                MainSection.MY_RESERVATIONS
            } else {
                MainSection.ADMIN_RESERVATIONS
            }
            val targetRoute = tabs.firstOrNull { it.section == targetSection }?.route
            if (targetRoute != null) {
                mainNavigationViewModel.selectSection(targetSection)
                tabNavController.navigate(targetRoute) {
                    // Usamos la ruta explícita de HOME en lugar de findStartDestination()
                    // para garantizar que siempre se llega al destino correcto.
                    // saveState=false: no guardamos el estado del wizard completado;
                    // restoreState=false: no restauramos estados anteriores de MY_RESERVATIONS
                    // para asegurarnos de que el usuario ve una vista limpia.
                    popUpTo(MainTabRoutes.HOME) {
                        inclusive = false
                        saveState = false
                    }
                    launchSingleTop = true
                    restoreState = false
                }
            }
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            // Barra de navegación inferior basada en el rol del usuario autenticado.
            NavigationBar {
                tabs.forEach { tab ->
                    val isSelected = mainNavigationViewModel.selectedSection.collectAsState().value == tab.section
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            mainNavigationViewModel.selectSection(tab.section)
                            val isHomeTab = tab.route == MainTabRoutes.HOME
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    // Al volver al HOME nunca guardamos el estado de la pestaña
                                    // actual para no contaminarlo con rutas del wizard u otras.
                                    saveState = !isHomeTab
                                }
                                launchSingleTop = true
                                // Para HOME: nunca restaurar estado guardado bajo su clave
                                // (evita que el wizard u otra ruta vuelva a aparecer en el
                                // dashboard). Para el resto de pestañas, sí restaurar.
                                restoreState = !isHomeTab
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = iconForSection(tab.section),
                                contentDescription = tab.label
                            )
                        },
                        label = { Text(tab.label) }
                    )
                }
            }
        }
    ) { paddingValues ->
        NavHost(
            navController = tabNavController,
            startDestination = startDestination,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Dashboard o landing principal dentro del shell autenticado.
            composable(MainTabRoutes.HOME) {
                DashboardTabScreen(
                    role = role,
                    profileState = profileState,
                    dashboardViewModel = dashboardViewModel,
                    onReserveNow = {
                        val targetRoute =
                            tabs.firstOrNull { it.section == MainSection.RESERVE }?.route
                        if (targetRoute != null) {
                            mainNavigationViewModel.selectSection(MainSection.RESERVE)
                            // popUpTo HOME garantiza [HOME, RESERVE] sin acumulación.
                            // saveState=false / restoreState=false → wizard siempre limpio.
                            tabNavController.navigate(targetRoute) {
                                popUpTo(MainTabRoutes.HOME) {
                                    inclusive = false
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    },
                    onViewReservations = {
                        val targetSection = if (role == UserRole.USER) {
                            MainSection.MY_RESERVATIONS
                        } else {
                            MainSection.ADMIN_RESERVATIONS
                        }
                        val targetRoute = tabs.firstOrNull { it.section == targetSection }?.route
                        if (targetRoute != null) {
                            mainNavigationViewModel.selectSection(targetSection)
                            tabNavController.navigate(targetRoute) {
                                popUpTo(MainTabRoutes.HOME) {
                                    inclusive = false
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                )
            }

            // Wizard de creación de reservas paso a paso.
            composable(MainTabRoutes.RESERVE) {
                ReservationWizardTabScreen(
                    role = role,
                    wizardState = wizardState,
                    stepValidation = stepValidation,
                    submitState = submitState,
                    submitFeedbackMessage = submitFeedbackMessage,
                    needsTableRecovery = needsTableRecovery,
                    turnOptionsState = turnOptionsState,
                    tableOptionsState = tableOptionsState,
                    allTablesState = allTablesState,
                    catalogState = catalogState,
                    onDateChange = reservationFlowViewModel::updateDate,
                    onSelectTurn = reservationFlowViewModel::selectTurn,
                    onSelectTable = reservationFlowViewModel::selectTable,
                    onSelectGame = reservationFlowViewModel::selectGame,
                    onSelectUser = reservationFlowViewModel::selectUser,
                    onBack = reservationFlowViewModel::goToPreviousStep,
                    onContinue = reservationFlowViewModel::continueToNextStep,
                    onSubmit = reservationFlowViewModel::submitReservation,
                    onReset = reservationFlowViewModel::resetFlow,
                    onReloadTurnOptions = reservationFlowViewModel::loadReservationOptions,
                    onReloadTableOptions = reservationFlowViewModel::reloadTableOptions
                )
            }

            // Vista de reservas del usuario final autenticado.
            composable(MainTabRoutes.MY_RESERVATIONS) {
                MyReservationsScreen(
                    state = myReservationsState,
                    onReload = reservationListViewModel::loadMyReservations
                )
            }

            // Entrada al catálogo navegable desde la barra inferior.
            composable(MainTabRoutes.CATALOG) {
                CatalogTabScreen(
                    catalogState = catalogState,
                    onGameClick = onGameClick,
                    onRefresh = { catalogViewModel.loadCatalog(force = true) },
                    onReserveClick = { game ->
                        // 1. Reinicia el wizard a paso DATE y pre-selecciona el juego elegido
                        reservationFlowViewModel.resetFlow()
                        val chipLabel = buildString {
                            append(game.nombre)
                            append(" · #${game.id}")
                            append(" · ${game.categoria}")
                            append(" · Disponible")
                            val obs = game.observaciones?.trim()
                                .takeUnless { it.isNullOrBlank() } ?: "Sin observaciones"
                            append(" · $obs")
                        }
                        reservationFlowViewModel.selectGame(game.nombre, game.id, chipLabel)

                        // 2. Navega a la pestaña RESERVE (mismo patrón que onReserveNow)
                        val targetRoute =
                            tabs.firstOrNull { it.section == MainSection.RESERVE }?.route
                        if (targetRoute != null) {
                            mainNavigationViewModel.selectSection(MainSection.RESERVE)
                            tabNavController.navigate(targetRoute) {
                                popUpTo(MainTabRoutes.HOME) {
                                    inclusive = false
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                )
            }

            // Vista administrativa con el conjunto global de reservas.
            composable(MainTabRoutes.ADMIN_RESERVATIONS) {
                AdminReservationsScreen(
                    state = adminReservationsState,
                    onReload = reservationListViewModel::loadAdminReservations
                )
            }

            composable(MainTabRoutes.MANAGEMENT) {
                ManagementScreen()
            }

            composable(MainTabRoutes.USERS) {
                UsersScreen()
            }

            composable(MainTabRoutes.PROFILE) {
                val reservationsSummaryState = if (role == UserRole.USER) {
                    myReservationsState
                } else {
                    adminReservationsState
                }
                ProfileTabScreen(
                    role = role,
                    profileState = profileState,
                    logoutState = logoutState,
                    myReservationsState = reservationsSummaryState,
                    onLogout = profileViewModel::logout,
                    onCloseApp = onCloseApp,
                    onViewReservations = {
                        val targetSection = if (role == UserRole.USER) {
                            MainSection.MY_RESERVATIONS
                        } else {
                            MainSection.ADMIN_RESERVATIONS
                        }
                        val targetRoute = tabs.firstOrNull { it.section == targetSection }?.route
                        if (targetRoute != null) {
                            mainNavigationViewModel.selectSection(targetSection)
                            tabNavController.navigate(targetRoute) {
                                popUpTo(MainTabRoutes.HOME) {
                                    inclusive = false
                                    saveState = false
                                }
                                launchSingleTop = true
                                restoreState = false
                            }
                        }
                    }
                )
            }
        }
    }
}

/**
 * Wrapper del dashboard que conecta el [DashboardViewModel] con la UI.
 *
 * Sincroniza el nombre de usuario desde [profileState] cada vez que cambia,
 * y lanza una recarga automática al entrar en composición.
 */
@Composable
private fun DashboardTabScreen(
    role: UserRole,
    profileState: UiState<ProfileInfo>,
    dashboardViewModel: DashboardViewModel,
    onReserveNow: () -> Unit,
    onViewReservations: () -> Unit
) {
    val uiState by dashboardViewModel.uiState.collectAsState()
    val username = (profileState as? UiState.Success)?.data?.username.orEmpty()

    // Propaga nombre al ViewModel cuando el perfil lo entrega.
    LaunchedEffect(username) {
        if (username.isNotBlank()) dashboardViewModel.setUsername(username)
    }

    // Recarga automática al entrar en la pestaña (role como clave evita recargas duplicadas).
    LaunchedEffect(role) {
        dashboardViewModel.refresh()
    }

    DashboardScreen(
        uiState = uiState,
        onReserveNow = onReserveNow,
        onViewReservations = onViewReservations,
        onRefresh = dashboardViewModel::refresh
    )
}

/** Wrapper del catálogo principal consumido desde la navegación por pestañas. */
@Composable
private fun CatalogTabScreen(
    catalogState: UiState<List<GameItemUi>>,
    onGameClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    onReserveClick: ((GameItemUi) -> Unit)?
) {
    GameListScreen(
        catalogState = catalogState,
        onGameClick = onGameClick,
        onRefresh = onRefresh,
        onReserveClick = onReserveClick
    )
}

/**
 * Contenido del wizard de reservas dentro de la pestaña principal de reserva.
 *
 * El flujo se divide en pasos (`DATE`, `TURN`, `TABLE`, `GAME`, `CONFIRMATION`)
 * y delega toda la lógica de negocio al ViewModel mediante callbacks. La UI se
 * limita a representar el estado actual, mostrar validaciones y ofrecer acciones
 * de reintento o recuperación cuando cambian las disponibilidades.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReservationWizardTabScreen(
    role: UserRole,
    wizardState: ReservationWizardState,
    stepValidation: ReservationStepValidation?,
    submitState: UiState<Unit>,
    submitFeedbackMessage: String?,
    needsTableRecovery: Boolean,
    turnOptionsState: UiState<List<ReservationTurnOption>>,
    tableOptionsState: UiState<List<ReservationTableOption>>,
    allTablesState: UiState<List<ReservationTableOption>>,
    catalogState: UiState<List<GameItemUi>>,
    onDateChange: (String) -> Unit,
    onSelectTurn: (Long, String?) -> Unit,
    onSelectTable: (Int, Long) -> Unit,
    onSelectGame: (String?, Long?, String?) -> Unit,
    onSelectUser: (String) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    onSubmit: () -> Unit,
    onReset: () -> Unit,
    onReloadTurnOptions: () -> Unit,
    onReloadTableOptions: () -> Unit
) {
    val draft = wizardState.draft
    val selectedGameName = draft.juegoNombre
    val selectedGameId = draft.juegoId
    val selectedGameLabel = draft.juegoEtiqueta
    var gameFilterQuery by rememberSaveable { mutableStateOf("") }
    var adminUserName by rememberSaveable(draft.usuarioNombre) {
        mutableStateOf(draft.usuarioNombre.orEmpty())
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 32.dp)
    ) {
        item {
            Text("Wizard de reserva", style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Paso actual: ${wizardState.currentStep.name}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        when (wizardState.currentStep) {
            // Paso 1: captura de fecha base sobre la que se calculan mesas libres.
            ReservationWizardStep.DATE -> {
                item {
                    var showDatePicker by remember { mutableStateOf(false) }

                    // Convierte "yyyy-MM-dd" del draft a millis UTC para inicializar el picker
                    val initialMillis: Long? = remember(draft.fecha) {
                        runCatching {
                            val parts = draft.fecha.split("-")
                            if (parts.size == 3) {
                                val cal = java.util.Calendar.getInstance(
                                    java.util.TimeZone.getTimeZone("UTC")
                                )
                                cal.set(
                                    parts[0].toInt(),
                                    parts[1].toInt() - 1,
                                    parts[2].toInt(),
                                    0, 0, 0
                                )
                                cal.set(java.util.Calendar.MILLISECOND, 0)
                                cal.timeInMillis
                            } else null
                        }.getOrNull()
                    }

                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = initialMillis
                    )

                    // Botón que muestra la fecha seleccionada o invita a elegir una
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (draft.fecha.isBlank()) "Seleccionar fecha" else draft.fecha
                        )
                    }

                    if (showDatePicker) {
                        DatePickerDialog(
                            onDismissRequest = { showDatePicker = false },
                            confirmButton = {
                                Button(onClick = {
                                    val millis = datePickerState.selectedDateMillis
                                    if (millis != null) {
                                        val cal = java.util.Calendar.getInstance(
                                            java.util.TimeZone.getTimeZone("UTC")
                                        )
                                        cal.timeInMillis = millis
                                        val fecha = "%04d-%02d-%02d".format(
                                            cal.get(java.util.Calendar.YEAR),
                                            cal.get(java.util.Calendar.MONTH) + 1,
                                            cal.get(java.util.Calendar.DAY_OF_MONTH)
                                        )
                                        onDateChange(fecha)
                                    }
                                    showDatePicker = false
                                }) { Text("Aceptar") }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showDatePicker = false }) {
                                    Text("Cancelar")
                                }
                            }
                        ) {
                            DatePicker(state = datePickerState)
                        }
                    }
                }
            }

            // Paso 2: selección del turno. Desde aquí ya se puede desencadenar la
            // consulta de mesas libres si la fecha es válida.
            ReservationWizardStep.TURN -> {
                item { Text("Selecciona un turno") }
                when (turnOptionsState) {
                    UiState.Idle, UiState.Loading -> item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) { CircularProgressIndicator() }
                    }

                    is UiState.Error -> {
                        item {
                            Text(
                                text = turnOptionsState.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        item {
                            OutlinedButton(onClick = onReloadTurnOptions) {
                                Text("Reintentar")
                            }
                        }
                    }

                    is UiState.Success -> {
                        if (turnOptionsState.data.isEmpty()) {
                            item { Text("No hay turnos disponibles para reservar") }
                        } else {
                            items(turnOptionsState.data, key = { it.id }) { turnOption ->
                                val horario = when {
                                    turnOption.horaInicio != null && turnOption.horaFin != null ->
                                        " (${turnOption.horaInicio}–${turnOption.horaFin})"
                                    turnOption.horaInicio != null -> " (${turnOption.horaInicio})"
                                    else -> ""
                                }
                                FilterChip(
                                    selected = draft.turnoId == turnOption.id,
                                    onClick = { onSelectTurn(turnOption.id, turnOption.nombre) },
                                    label = { Text("${turnOption.nombre}$horario") }
                                )
                            }
                        }
                    }
                }
            }

            // Paso 3: mapa visual de mesas. Verde = libre (filtradas por fecha+turno desde
            // GET /mesas/libres?fecha=…&turnoId=…); gris = no disponible en ese turno.
            ReservationWizardStep.TABLE -> {
                item {
                    Text("Selecciona una mesa")
                    Text(
                        text = "Mesas disponibles para ${draft.fecha.ifBlank { "–" }}" +
                            " · turno ${draft.turnoNombre ?: draft.turnoId?.toString() ?: "–"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                when {
                    // Si todavía no se eligió fecha/turno, indicamos que hace falta.
                    tableOptionsState == UiState.Idle -> item {
                        Text("Selecciona fecha y turno para ver las mesas disponibles")
                    }

                    tableOptionsState == UiState.Loading || allTablesState == UiState.Loading -> item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) { CircularProgressIndicator() }
                    }

                    tableOptionsState is UiState.Error -> {
                        item {
                            Text(
                                text = (tableOptionsState as UiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        item {
                            OutlinedButton(onClick = onReloadTableOptions) {
                                Text("Reintentar")
                            }
                        }
                    }

                    // Mapa visual: todas las mesas operativas, coloreadas según disponibilidad.
                    allTablesState is UiState.Success -> {
                        val allTables = (allTablesState as UiState.Success<List<ReservationTableOption>>).data
                        val freeTables = (tableOptionsState as? UiState.Success<List<ReservationTableOption>>)?.data.orEmpty()
                        val freeIds = freeTables.map { it.id }.toSet()

                        item {
                            TableMapGrid(
                                allTables = allTables,
                                freeTableIds = freeIds,
                                selectedTableNumero = draft.mesaNumero,
                                onSelectTable = onSelectTable
                            )
                        }
                        item { TableMapLegend() }

                        if (freeTables.isEmpty()) {
                            item {
                                Text(
                                    "No hay mesas libres para la fecha y turno seleccionados",
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Fallback: lista simple si no hay estado de todas las mesas.
                    tableOptionsState is UiState.Success -> {
                        val tables = (tableOptionsState as UiState.Success<List<ReservationTableOption>>).data
                        if (tables.isEmpty()) {
                            item { Text("No hay mesas operativas en este momento") }
                        } else {
                            items(tables, key = { it.numero }) { tableOption ->
                                FilterChip(
                                    selected = draft.mesaNumero == tableOption.numero,
                                    onClick = { onSelectTable(tableOption.numero, tableOption.id) },
                                    label = { Text("Mesa ${tableOption.numero}") }
                                )
                            }
                        }
                    }

                    else -> item { Text("Selecciona fecha y turno para cargar mesas libres") }
                }
            }

            // Paso 4: selección de juego requerida. Solo los juegos disponibles
            // pueden elegirse. El filtro ayuda a reducir el catálogo.
            ReservationWizardStep.GAME -> {
                item {
                    Text("Selecciona un juego")
                    Text(
                        text = "Solo los juegos disponibles pueden reservarse",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item {
                    OutlinedTextField(
                        value = gameFilterQuery,
                        onValueChange = { gameFilterQuery = it },
                        label = { Text("Filtrar juego (nombre/observaciones)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                when (catalogState) {
                    is UiState.Success -> {
                        val normalizedQuery = gameFilterQuery.trim()
                        val visibleGames = catalogState.data
                            .distinctBy { it.id }
                            .sortedWith(compareBy({ it.nombre.lowercase() }, { it.id }))
                            .filter { game ->
                                if (normalizedQuery.isBlank()) {
                                    true
                                } else {
                                    game.nombre.contains(normalizedQuery, ignoreCase = true) ||
                                        game.observaciones
                                            ?.contains(normalizedQuery, ignoreCase = true)
                                            ?: false
                                }
                            }
                            .take(12)
                        if (visibleGames.isEmpty()) {
                            item {
                                Text("No hay juegos que coincidan con el filtro")
                            }
                        }
                        // Paso final: resumen previo al envío. Si el rol es ADMIN se solicita
                        // además el usuario destino de la reserva.
                        items(visibleGames, key = { it.id }) { game ->
                            val observationsLabel = game.observaciones
                                ?.trim()
                                .takeUnless { it.isNullOrBlank() }
                                ?: "Sin observaciones"
                            val chipLabel = buildString {
                                append(game.nombre)
                                append(" · #${game.id}")
                                append(" · ${game.categoria}")
                                append(" · ${if (game.disponible) "Disponible" else "No disponible"}")
                                append(" · $observationsLabel")
                            }
                            val selected = when {
                                selectedGameId != null -> selectedGameId == game.id
                                else -> selectedGameName == game.nombre
                            }
                            WizardGameCard(
                                game = game,
                                selected = selected,
                                onToggle = {
                                    if (selected) {
                                        onSelectGame(null, null, null)
                                    } else {
                                        onSelectGame(game.nombre, game.id, chipLabel)
                                    }
                                }
                            )
                        }
                    }

                    else -> {
                        item { Text("Carga el catálogo para elegir juego") }
                    }
                }
            }

            ReservationWizardStep.CONFIRMATION -> {
                item {
                    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("Confirmacion", fontWeight = FontWeight.SemiBold)
                            Text("Fecha: ${draft.fecha}")
                            Text("Turno: ${draft.turnoId ?: "-"}")
                            Text("Mesa: ${draft.mesaNumero ?: "-"}")
                            val selectedGameLine = if (selectedGameName != null) {
                                selectedGameLabel ?: selectedGameName
                            } else {
                                "Sin preferencia"
                            }
                            Text("Juego: $selectedGameLine")
                        }
                    }
                }

                if (role == UserRole.ADMIN) {
                    item {
                        OutlinedTextField(
                            value = adminUserName,
                            onValueChange = {
                                adminUserName = it
                                onSelectUser(it)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Usuario para la reserva") },
                            singleLine = true
                        )
                    }
                }
            }
        }

        // Validación del paso actual o del envío global, mostrada como feedback inline.
        if (!stepValidation?.message.isNullOrBlank()) {
            item {
                Text(
                    text = stepValidation.message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        // Mensajes informativos adicionales, por ejemplo intentos duplicados de envío.
        if (!submitFeedbackMessage.isNullOrBlank()) {
            item {
                Text(
                    text = submitFeedbackMessage,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (submitState is UiState.Error) {
            item {
                Text(
                    text = submitState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Si la mesa deja de estar disponible justo antes del submit, la UI permite
            // refrescar las opciones y rehacer la selección desde el paso TABLE.
            if (wizardState.currentStep == ReservationWizardStep.TABLE && needsTableRecovery) {
                item {
                    OutlinedButton(onClick = onReloadTableOptions) {
                        Text("Actualizar mesas libres")
                    }
                }
            }
        }

        if (submitState is UiState.Success) {
            item {
                Text("Reserva creada correctamente")
            }
        }

        // Barra de acciones común del wizard. Cambia entre navegación de pasos y confirmación.
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    onClick = onBack,
                    enabled = wizardState.currentStep != ReservationWizardStep.DATE
                ) { Text("Atras") }

                if (wizardState.currentStep == ReservationWizardStep.CONFIRMATION) {
                    Button(
                        onClick = onSubmit,
                        enabled = submitState !is UiState.Loading
                    ) {
                        Text(if (submitState is UiState.Loading) "Enviando..." else "Confirmar")
                    }
                    OutlinedButton(onClick = onReset) { Text("Reiniciar") }
                } else {
                    val hasAvailableTables = (tableOptionsState as? UiState.Success)
                        ?.data
                        ?.isNotEmpty()
                        ?: true
                    val canContinueFromTable =
                        wizardState.currentStep != ReservationWizardStep.TABLE ||
                            hasAvailableTables
                    Button(
                        onClick = onContinue,
                        enabled = canContinueFromTable
                    ) { Text("Continuar") }
                }
            }
        }
    }
}

/** Pantalla de perfil mostrada como pestaña final dentro del shell autenticado. */
@Composable
private fun ProfileTabScreen(
    role: UserRole,
    profileState: UiState<ProfileInfo>,
    logoutState: UiState<Unit>,
    myReservationsState: UiState<List<ReservationListItemUi>>,
    onLogout: () -> Unit,
    onCloseApp: () -> Unit,
    onViewReservations: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {

        // ─── 1. Avatar + nombre + rol ────────────────────────────────────
        item {
            when (profileState) {
                UiState.Idle, UiState.Loading -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }

                is UiState.Error -> {
                    Text(
                        text = profileState.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                is UiState.Success -> {
                    val info = profileState.data
                    val initials = info.username
                        .trim()
                        .split("\\s+".toRegex())
                        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
                        .take(2)
                        .joinToString("")
                        .ifBlank { "?" }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = initials,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                text = info.username,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            val chipBg = if (role == UserRole.ADMIN)
                                MaterialTheme.colorScheme.tertiaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                            val chipContent = if (role == UserRole.ADMIN)
                                MaterialTheme.colorScheme.onTertiaryContainer
                            else
                                MaterialTheme.colorScheme.onSecondaryContainer
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = chipBg,
                                contentColor = chipContent
                            ) {
                                Text(
                                    text = if (role == UserRole.ADMIN) "ADMIN" else "USUARIO",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // ─── 2. Resumen de reservas ──────────────────────────────────────
        item {
            ProfileReservationsSummaryCard(
                role = role,
                myReservationsState = myReservationsState,
                onViewAll = onViewReservations
            )
        }

        // ─── 3. Acciones de cuenta ───────────────────────────────────────
        item {
            ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Cuenta",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onLogout,
                            enabled = logoutState !is UiState.Loading,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                if (logoutState is UiState.Loading) "Cerrando..." else "Cerrar sesión"
                            )
                        }
                        OutlinedButton(
                            onClick = onCloseApp,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Salir")
                        }
                    }
                    if (logoutState is UiState.Error) {
                        Text(
                            text = logoutState.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}

/**
 * Tarjeta de resumen de reservas del usuario, mostrada dentro del perfil.
 *
 * - Para rol USER muestra "Mis reservas" con contador y las 2 últimas.
 * - Para rol ADMIN muestra "Reservas globales" con contador y las 2 últimas.
 * - "Ver todas" navega a la pestaña correspondiente.
 */
@Composable
private fun ProfileReservationsSummaryCard(
    role: UserRole,
    myReservationsState: UiState<List<ReservationListItemUi>>,
    onViewAll: () -> Unit
) {
    val sectionTitle = if (role == UserRole.ADMIN) "Reservas globales" else "Mis reservas"

    ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = sectionTitle,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                TextButton(onClick = onViewAll) {
                    Text("Ver todas", style = MaterialTheme.typography.labelMedium)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            when (myReservationsState) {
                UiState.Idle, UiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }

                is UiState.Error -> {
                    Text(
                        text = myReservationsState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                is UiState.Success -> {
                    val list = myReservationsState.data
                    if (list.isEmpty()) {
                        Text(
                            text = "Todavía no hay reservas registradas",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = "${list.size} reserva${if (list.size != 1) "s" else ""} en total",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        list.take(2).forEachIndexed { index, item ->
                            if (index > 0) {
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(end = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(2.dp)
                                ) {
                                    Text(
                                        text = "📅 ${item.fecha}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${item.turnoNombre} · Mesa ${item.mesaNumero ?: "—"}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                ReservationStatusChip(estado = item.estado)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Tarjeta de juego para el wizard de reserva
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Tarjeta de selección de juego dentro del wizard de reserva.
 *
 * Reutiliza la misma estética que [ioc.andresgq.gamehubmobile.ui.screens.gamecatalog.GameCard]
 * del catálogo, adaptada para el contexto de selección:
 * - Borde de color primario cuando el juego está seleccionado.
 * - Insignia "✓" superpuesta en la esquina superior derecha al estar seleccionado.
 * - Botón "Seleccionar" / "✓ Seleccionado" en la parte inferior de la tarjeta.
 * - Opacidad reducida y botón desactivado para los juegos no disponibles.
 *
 * @param game     datos del juego a mostrar.
 * @param selected `true` si este juego es el actualmente elegido en el wizard.
 * @param onToggle callback que alterna la selección (seleccionar si no estaba, deseleccionar si lo estaba).
 */
@Composable
private fun WizardGameCard(
    game: GameItemUi,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val borderColor = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent

    ElevatedCard(
        onClick = { if (game.disponible) onToggle() },
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(BorderStroke(2.dp, borderColor), RoundedCornerShape(16.dp))
            .alpha(if (game.disponible) 1f else 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Imagen con insignia de selección superpuesta
            Box {
                AsyncImage(
                    model = resolveGameThumbnailUrl(game.rutaImagen),
                    contentDescription = "Imagen de ${game.nombre}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.ic_image_placeholder),
                    error = painterResource(R.drawable.ic_image_placeholder)
                )
                if (selected) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "✓",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Text(
                text = game.nombre,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Text(
                text = "${game.categoria}  |  Jugadores: ${game.numJugadores}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = if (game.disponible) "Disponible" else "No disponible",
                style = MaterialTheme.typography.labelLarge,
                color = if (game.disponible) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                }
            )

            if (!game.descripcion.isNullOrBlank()) {
                Text(
                    text = game.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Botón de selección: solo activo si el juego está disponible
            if (game.disponible) {
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth(),
                    colors = if (selected) {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        )
                    }
                ) {
                    Text(if (selected) "✓ Seleccionado" else "Seleccionar")
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Mapa visual de mesas
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cuadrícula de mesas del local.
 *
 * - **Verde** → libre en la fecha+turno seleccionados (presentes en [freeTableIds]).
 * - **Gris**  → operativa pero ocupada en ese turno.
 * - **Borde** de color primario → mesa actualmente seleccionada.
 *
 * Solo las mesas libres son interactivas.
 */
@Composable
private fun TableMapGrid(
    allTables: List<ReservationTableOption>,
    freeTableIds: Set<Long>,
    selectedTableNumero: Int?,
    onSelectTable: (Int, Long) -> Unit
) {
    val columns = 3
    val rows = (allTables.size + columns - 1) / columns
    val sorted = allTables.sortedBy { it.numero }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (col in 0 until columns) {
                    val idx = rowIndex * columns + col
                    if (idx < sorted.size) {
                        val table = sorted[idx]
                        val isFree = table.id in freeTableIds
                        val isSelected = table.numero == selectedTableNumero

                        val bgColor = when {
                            isSelected -> MaterialTheme.colorScheme.primary
                            isFree    -> Color(0xFF4CAF50)
                            else      -> MaterialTheme.colorScheme.surfaceVariant
                        }
                        val textColor = when {
                            isSelected -> MaterialTheme.colorScheme.onPrimary
                            isFree    -> Color.White
                            else      -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bgColor)
                                .then(
                                    if (isFree) Modifier.clickable {
                                        onSelectTable(table.numero, table.id)
                                    } else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "🏓",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    text = "${table.numero}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = textColor,
                                    fontWeight = FontWeight.Bold
                                )
                                if (!isFree) {
                                    Text(
                                        text = "Ocupada",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = textColor
                                    )
                                }
                            }
                        }
                    } else {
                        // Celda vacía para completar la última fila
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Leyenda de colores del mapa de mesas. */
@Composable
private fun TableMapLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        TableMapLegendItem(color = Color(0xFF4CAF50), label = "Libre")
        TableMapLegendItem(color = MaterialTheme.colorScheme.surfaceVariant, label = "Ocupada")
        TableMapLegendItem(color = MaterialTheme.colorScheme.primary, label = "Seleccionada")
    }
}

@Composable
private fun TableMapLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color)
        )
        Text(
            text = " $label",
            style = MaterialTheme.typography.labelSmall
        )
    }
}
