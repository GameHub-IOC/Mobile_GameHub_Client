@file:Suppress("unused")

package ioc.andresgq.gamehubmobile.ui.screens.reservation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import ioc.andresgq.gamehubmobile.domain.reservation.CreateReservationCommand
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationDraft
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationStepValidation
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationTableOption
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationTurnOption
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationWizardState
import ioc.andresgq.gamehubmobile.ui.model.reservation.ReservationWizardStep
import ioc.andresgq.gamehubmobile.ui.model.reservation.validateCurrentStep
import ioc.andresgq.gamehubmobile.ui.model.reservation.validateForSubmit
import ioc.andresgq.gamehubmobile.ui.state.UiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel del flujo principal de reservas.
 *
 * En esta fase encapsula el estado del wizard y la lógica de validación/envío,
 * sin acoplarlo todavía a una pantalla Compose definitiva.
 */
class ReservationFlowViewModel(
    private val reservationRepository: ReservationRepository,
    role: UserRole
) : ViewModel() {

    private val _wizardState = MutableStateFlow(ReservationWizardState(role = role))
    val wizardState: StateFlow<ReservationWizardState> = _wizardState.asStateFlow()

    private val _stepValidation = MutableStateFlow<ReservationStepValidation?>(null)
    val stepValidation: StateFlow<ReservationStepValidation?> = _stepValidation.asStateFlow()

    private val _submitState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val submitState: StateFlow<UiState<Unit>> = _submitState.asStateFlow()

    private val _submitFeedbackMessage = MutableStateFlow<String?>(null)
    val submitFeedbackMessage: StateFlow<String?> = _submitFeedbackMessage.asStateFlow()

    private val _needsTableRecovery = MutableStateFlow(false)
    val needsTableRecovery: StateFlow<Boolean> = _needsTableRecovery.asStateFlow()

    private val _turnOptionsState = MutableStateFlow<UiState<List<ReservationTurnOption>>>(UiState.Loading)
    val turnOptionsState: StateFlow<UiState<List<ReservationTurnOption>>> = _turnOptionsState.asStateFlow()

    private val _tableOptionsState = MutableStateFlow<UiState<List<ReservationTableOption>>>(UiState.Loading)
    val tableOptionsState: StateFlow<UiState<List<ReservationTableOption>>> = _tableOptionsState.asStateFlow()

    init {
        loadReservationOptions()
    }

    /**
     * Carga las opciones de turno y mesa disponibles.
     */
    fun loadReservationOptions() {
        viewModelScope.launch {
            _turnOptionsState.value = UiState.Loading
            _tableOptionsState.value = UiState.Idle

            val turnsResult = reservationRepository.getTurnOptions()

            _turnOptionsState.value = turnsResult.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "No se pudieron cargar los turnos") }
            )
        }
    }

    fun reloadTableOptions() {
        loadFreeTablesIfReady()
    }

    /**
     * Actualiza la fecha seleccionada en el wizard.
     * Limpia la mesa (numero e id) porque la disponibilidad puede haber cambiado.
     */
    fun updateDate(fecha: String) {
        updateDraft { copy(fecha = fecha.trim(), mesaNumero = null, mesaId = null) }
        loadFreeTablesIfReady()
    }

    /**
     * Actualiza el turno seleccionado en el wizard.
     * Limpia la mesa (numero e id) porque la disponibilidad depende del turno.
     */
    fun selectTurn(turnoId: Long, turnoNombre: String? = null) {
        val normalizedName = turnoNombre?.trim().takeUnless { it.isNullOrBlank() }
            ?: (_turnOptionsState.value as? UiState.Success)
                ?.data
                ?.firstOrNull { it.id == turnoId }
                ?.nombre

        updateDraft {
            copy(
                turnoId = turnoId,
                turnoNombre = normalizedName,
                mesaNumero = null,
                mesaId = null
            )
        }
        loadFreeTablesIfReady()
    }

    /**
     * Actualiza la mesa seleccionada en el wizard.
     * Guarda tanto el número (para validación contra la lista) como el id (para el POST).
     */
    fun selectTable(mesaNumero: Int, mesaId: Long) {
        updateDraft { copy(mesaNumero = mesaNumero, mesaId = mesaId) }
    }

    fun selectGame(gameName: String?, gameId: Long? = null, gameLabel: String? = null) {
        val normalizedName = gameName?.trim().takeUnless { it.isNullOrBlank() }
        val normalizedLabel = gameLabel?.trim().takeUnless { it.isNullOrBlank() }
        updateDraft {
            copy(
                juegoId = if (normalizedName == null) null else gameId,
                juegoNombre = normalizedName,
                juegoEtiqueta = if (normalizedName == null) null else normalizedLabel
            )
        }
    }

    /**
     * Actualiza el usuario seleccionado en el wizard.
     */
    fun selectUser(username: String) {
        updateDraft { copy(usuarioNombre = username.trim()) }
    }

    /**
     * Continua al siguiente paso del wizard.
     */
    fun continueToNextStep() {
        val baseValidation = validateCurrentStep(_wizardState.value)
        val validation = validateAgainstLoadedOptions(baseValidation)
        _stepValidation.value = validation.takeUnless { it.isValid }

        if (!validation.isValid) return

        val nextStep = _wizardState.value.currentStep.next() ?: return
        _wizardState.value = _wizardState.value.copy(currentStep = nextStep)
    }

    /**
     * Regresa al paso anterior del wizard.
     */
    fun goToPreviousStep() {
        val previousStep = _wizardState.value.currentStep.previous() ?: return
        _stepValidation.value = null
        _wizardState.value = _wizardState.value.copy(currentStep = previousStep)
    }

    /**
     * Envía la reserva actual.
     */
    fun submitReservation() {
        if (_submitState.value is UiState.Loading) {
            _submitFeedbackMessage.value = SUBMIT_IN_PROGRESS_MESSAGE
            return
        }

        val stateForSubmit = _wizardState.value.copy(currentStep = ReservationWizardStep.CONFIRMATION)
        val validation = validateForSubmit(stateForSubmit)
        _stepValidation.value = validation.takeUnless { it.isValid }

        if (!validation.isValid) {
            _submitState.value = UiState.Error(validation.message ?: "No se pudo validar la reserva")
            return
        }

        _submitFeedbackMessage.value = null
        _submitState.value = UiState.Loading

        viewModelScope.launch {
            val draft = _wizardState.value.draft
            val availabilityCheck = revalidateAvailabilityForSubmit(draft)
            if (!availabilityCheck.isValid) {
                _stepValidation.value = ReservationStepValidation(false, availabilityCheck.message)
                if (availabilityCheck.returnToTableStep) {
                    _needsTableRecovery.value = true
                    _wizardState.value = _wizardState.value.copy(currentStep = ReservationWizardStep.TABLE)
                }
                _submitState.value = UiState.Error(availabilityCheck.message)
                return@launch
            }

            val result = reservationRepository.createReservation(
                CreateReservationCommand(
                    role = _wizardState.value.role,
                    fecha = draft.fecha,
                    turnoId = draft.turnoId ?: 0L,
                    mesaId = draft.mesaId ?: 0L,
                    mesaNumero = draft.mesaNumero ?: 0,
                    juegoNombre = draft.juegoNombre,
                    usuarioNombre = draft.usuarioNombre
                )
            )
            _submitState.value = result.fold(
                onSuccess = {
                    _stepValidation.value = null
                    _submitFeedbackMessage.value = null
                    _needsTableRecovery.value = false
                    UiState.Success(Unit)
                },
                onFailure = { UiState.Error(it.message ?: "No se pudo crear la reserva") }
            )
        }
    }

    /**
     * Reinicia el flujo a su estado inicial.
     */
    fun resetFlow() {
        _wizardState.value = ReservationWizardState(role = _wizardState.value.role)
        _stepValidation.value = null
        _submitState.value = UiState.Idle
        _submitFeedbackMessage.value = null
        _needsTableRecovery.value = false
        loadReservationOptions()
    }

    /**
     * Carga las mesas libres para el turno seleccionado.
     */
    private fun loadFreeTablesIfReady() {
        val draft = _wizardState.value.draft
        val turnoId = draft.turnoId
        if (turnoId == null || draft.fecha.isBlank() || !DATE_REGEX.matches(draft.fecha)) {
            _tableOptionsState.value = UiState.Idle
            return
        }

        viewModelScope.launch {
            _tableOptionsState.value = UiState.Loading
            val result = reservationRepository.getFreeTableOptions(
                fecha = draft.fecha,
                turnoId = turnoId
            )
            _tableOptionsState.value = result.fold(
                onSuccess = { UiState.Success(it) },
                onFailure = { UiState.Error(it.message ?: "No se pudieron cargar las mesas libres") }
            )
        }
    }

    /**
     * Valida la disponibilidad de mesas para la reserva actual.
     */
    private suspend fun revalidateAvailabilityForSubmit(draft: ReservationDraft): SubmitAvailabilityCheck {
        val turnoId = draft.turnoId
            ?: return SubmitAvailabilityCheck(
                isValid = false,
                message = "Falta el turno seleccionado"
            )
        val mesaId = draft.mesaId
            ?: return SubmitAvailabilityCheck(
                isValid = false,
                message = "Falta la mesa seleccionada"
            )

        val freeTablesResult = reservationRepository.getFreeTableOptions(
            fecha = draft.fecha,
            turnoId = turnoId
        )

        return freeTablesResult.fold(
            onSuccess = { freeTables ->
                _tableOptionsState.value = UiState.Success(freeTables)
                // Verificamos que la mesa seleccionada (por id) sigue estando libre
                if (freeTables.none { it.id == mesaId }) {
                    SubmitAvailabilityCheck(
                        isValid = false,
                        message = TABLE_NO_LONGER_AVAILABLE_MESSAGE,
                        returnToTableStep = true
                    )
                } else {
                    SubmitAvailabilityCheck(isValid = true)
                }
            },
            onFailure = {
                SubmitAvailabilityCheck(
                    isValid = false,
                    message = it.message ?: FREE_TABLE_VALIDATION_ERROR_MESSAGE
                )
            }
        )
    }

    /**
     * Actualiza el borrador del wizard.
     */
    private fun updateDraft(transform: ReservationDraft.() -> ReservationDraft) {
        _stepValidation.value = null
        _submitFeedbackMessage.value = null
        _needsTableRecovery.value = false
        if (_submitState.value is UiState.Error) {
            _submitState.value = UiState.Idle
        }
        _wizardState.value = _wizardState.value.copy(draft = _wizardState.value.draft.transform())
    }

    /**
     * Valida la disponibilidad de mesas para la reserva actual.
     */
    private fun validateAgainstLoadedOptions(
        baseValidation: ReservationStepValidation
    ): ReservationStepValidation {
        val state = _wizardState.value
        if (state.currentStep != ReservationWizardStep.TABLE && !baseValidation.isValid) {
            return baseValidation
        }
        return when (state.currentStep) {
            ReservationWizardStep.TURN -> {
                if (!baseValidation.isValid) return baseValidation
                val turnId = state.draft.turnoId
                val turns = (_turnOptionsState.value as? UiState.Success)?.data.orEmpty()
                if (turnId != null && turns.isNotEmpty() && turns.none { it.id == turnId }) {
                    ReservationStepValidation(false, "El turno seleccionado ya no está disponible")
                } else {
                    baseValidation
                }
            }

            ReservationWizardStep.TABLE -> {
                val mesaId = state.draft.mesaId
                val tables = (_tableOptionsState.value as? UiState.Success)?.data.orEmpty()
                if (_tableOptionsState.value is UiState.Loading) {
                    ReservationStepValidation(false, "Espera a que carguen las mesas libres")
                } else if (_tableOptionsState.value is UiState.Error) {
                    ReservationStepValidation(false, "No se pudieron cargar las mesas libres")
                } else if (_tableOptionsState.value is UiState.Success && tables.isEmpty()) {
                    ReservationStepValidation(false, "No hay mesas libres para la fecha y turno seleccionados")
                } else if (!baseValidation.isValid) {
                    baseValidation
                } else if (mesaId != null && tables.isNotEmpty() && tables.none { it.id == mesaId }) {
                    ReservationStepValidation(false, "La mesa seleccionada ya no está disponible")
                } else {
                    baseValidation
                }
            }

            else -> baseValidation
        }
    }

    /**
     * Calcula el siguiente paso del wizard.
     */
    private fun ReservationWizardStep.next(): ReservationWizardStep? = when (this) {
        ReservationWizardStep.DATE -> ReservationWizardStep.TURN
        ReservationWizardStep.TURN -> ReservationWizardStep.TABLE
        ReservationWizardStep.TABLE -> ReservationWizardStep.GAME
        ReservationWizardStep.GAME -> ReservationWizardStep.CONFIRMATION
        ReservationWizardStep.CONFIRMATION -> null
    }

    /**
     * Calcula el paso anterior del wizard.
     */
    private fun ReservationWizardStep.previous(): ReservationWizardStep? = when (this) {
        ReservationWizardStep.DATE -> null
        ReservationWizardStep.TURN -> ReservationWizardStep.DATE
        ReservationWizardStep.TABLE -> ReservationWizardStep.TURN
        ReservationWizardStep.GAME -> ReservationWizardStep.TABLE
        ReservationWizardStep.CONFIRMATION -> ReservationWizardStep.GAME
    }

    /**
     * Expresión regular para validar fechas.
     */
    private companion object {
        val DATE_REGEX = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        const val TABLE_NO_LONGER_AVAILABLE_MESSAGE = "La mesa seleccionada ya no está disponible. Elige otra para continuar"
        const val FREE_TABLE_VALIDATION_ERROR_MESSAGE = "No se pudo validar la disponibilidad de mesas"
        const val SUBMIT_IN_PROGRESS_MESSAGE = "Ya estamos enviando la reserva. Espera un momento"
    }
}

/**
 * Respuesta de validación de disponibilidad de mesas.
 */
private data class SubmitAvailabilityCheck(
    val isValid: Boolean,
    val message: String = "",
    val returnToTableStep: Boolean = false
)

/**
 * Fábrica de ViewModel para el flujo principal de reservas.
 */
class ReservationFlowViewModelFactory(
    private val reservationRepository: ReservationRepository,
    private val role: UserRole
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return ReservationFlowViewModel(
            reservationRepository = reservationRepository,
            role = role
        ) as T
    }
}