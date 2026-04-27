package ioc.andresgq.gamehubmobile.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
                            tabNavController.navigate(tab.route) {
                                popUpTo(tabNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Text(tab.label.take(1)) },
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
                    onGameClick = onGameClick
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
                ProfileTabScreen(
                    profileState = profileState,
                    logoutState = logoutState,
                    onReloadProfile = profileViewModel::loadProfile,
                    onLogout = profileViewModel::logout,
                    onCloseApp = onCloseApp
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
    onGameClick: (Long) -> Unit
) {
    GameListScreen(
        catalogState = catalogState,
        onGameClick = onGameClick
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
                    OutlinedTextField(
                        value = draft.fecha,
                        onValueChange = onDateChange,
                        label = { Text("Fecha (yyyy-MM-dd)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
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

            // Paso 3: mesa concreta. Solo se muestran mesas libres devueltas por backend.
            ReservationWizardStep.TABLE -> {
                item { Text("Selecciona una mesa") }
                when (tableOptionsState) {
                    UiState.Idle -> item {
                        Text("Selecciona fecha y turno para cargar mesas libres")
                    }

                    UiState.Loading -> item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) { CircularProgressIndicator() }
                    }

                    is UiState.Error -> {
                        item {
                            Text(
                                text = tableOptionsState.message,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        item {
                            OutlinedButton(onClick = onReloadTableOptions) {
                                Text("Reintentar")
                            }
                        }
                    }

                    is UiState.Success -> {
                        if (tableOptionsState.data.isEmpty()) {
                            item { Text("No hay mesas operativas en este momento") }
                        } else {
                            items(tableOptionsState.data, key = { it.numero }) { tableOption ->
                                FilterChip(
                                    selected = draft.mesaNumero == tableOption.numero,
                                    onClick = { onSelectTable(tableOption.numero, tableOption.id) },
                                    label = { Text("Mesa ${tableOption.numero}") }
                                )
                            }
                        }
                    }
                }
            }

            // Paso 4: selección opcional de juego. El filtro ayuda a reducir el
            // catálogo sin abandonar el flujo principal de reserva.
            ReservationWizardStep.GAME -> {
                item { Text("Juego opcional") }
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
                        items(visibleGames, key = { it.id }) { game ->
                            val availabilityLabel = if (game.disponible) "Disponible" else "No disponible"
                            val observationsLabel = game.observaciones
                                ?.trim()
                                .takeUnless { it.isNullOrBlank() }
                                ?: "Sin observaciones"
                            val chipLabel = buildString {
                                append(game.nombre)
                                append(" · #")
                                append(game.id)
                                append(" · ")
                                append(game.categoria)
                                append(" · ")
                                append(availabilityLabel)
                                append(" · ")
                                append(observationsLabel)
                            }
                            val selected = when {
                                selectedGameId != null -> selectedGameId == game.id
                                else -> selectedGameName == game.nombre
                            }
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) {
                                        onSelectGame(null, null, null)
                                    } else {
                                        onSelectGame(game.nombre, game.id, chipLabel)
                                    }
                                },
                                label = { Text(chipLabel) }
                            )
                                            // Paso final: resumen previo al envío. Si el rol es ADMIN se solicita
                                            // además el usuario destino de la reserva.
                        }
                    }

                    else -> {
                        item { Text("Carga el catalogo para elegir juego") }
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
    profileState: UiState<ProfileInfo>,
    logoutState: UiState<Unit>,
    onReloadProfile: () -> Unit,
    onLogout: () -> Unit,
    onCloseApp: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Perfil", style = MaterialTheme.typography.titleLarge)

        when (profileState) {
            UiState.Idle, UiState.Loading -> CircularProgressIndicator()
            is UiState.Error -> Text(profileState.message, color = MaterialTheme.colorScheme.error)
            is UiState.Success -> {
                Text("Usuario: ${profileState.data.username}")
                Text("Rol: ${profileState.data.userType}")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onReloadProfile) { Text("Recargar") }
            Button(onClick = onLogout, enabled = logoutState !is UiState.Loading) {
                Text(if (logoutState is UiState.Loading) "Cerrando..." else "Cerrar sesion")
            }
            OutlinedButton(onClick = onCloseApp) { Text("Salir") }
        }

        if (logoutState is UiState.Error) {
            Text(logoutState.message, color = MaterialTheme.colorScheme.error)
        }
    }
}