package ioc.andresgq.gamehubmobile.data.repository

import ioc.andresgq.gamehubmobile.data.remote.ReservationApi
import ioc.andresgq.gamehubmobile.data.remote.ReservationRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationAdminRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationJuegoRefDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationListItemDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaOperativaDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationMesaRefDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoListDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUserRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationJuegoListDto
import ioc.andresgq.gamehubmobile.data.remote.dto.ReservationUsuarioRefDto
import ioc.andresgq.gamehubmobile.domain.reservation.CreateReservationCommand
import ioc.andresgq.gamehubmobile.domain.reservation.UserRole
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ReservationRepositoryTest {

    @Test
    fun getMyReservations_success_mapsList() = runTest {
        val fakeApi = FakeReservationApi().apply {
            myReservations = listOf(
                ReservationListItemDto(
                    id = 10L,
                    fecha = "2026-05-01",
                    estado = "CONFIRMADA",
                    mesa = ReservationMesaRefDto(4),
                    turno = ReservationTurnoListDto(id = 2L, nombre = "Tarde"),
                    juego = ReservationJuegoListDto(id = 8L, nombre = "Catan"),
                    usuario = ReservationUsuarioRefDto(nombre = "pepe")
                )
            )
        }
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.getMyReservations()

        assertTrue(result.isSuccess)
        val first = result.getOrNull().orEmpty().first()
        assertEquals(10L, first.id)
        assertEquals("CONFIRMADA", first.estado)
        assertEquals(4, first.mesaNumero)
        assertEquals("Tarde", first.turnoNombre)
    }

    @Test
    fun getAdminReservations_success_mapsUserName() = runTest {
        val fakeApi = FakeReservationApi().apply {
            adminReservations = listOf(
                ReservationListItemDto(
                    id = 20L,
                    fecha = "2026-05-03",
                    mesa = ReservationMesaRefDto(2),
                    usuario = ReservationUsuarioRefDto(nombre = "maria")
                )
            )
        }
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.getAdminReservations()

        assertTrue(result.isSuccess)
        assertEquals("maria", result.getOrNull().orEmpty().first().usuarioNombre)
    }

    @Test
    fun getTurnOptions_success_mapsAndSortsTurns() = runTest {
        val fakeApi = FakeReservationApi().apply {
            turns = listOf(
                ReservationTurnoDto(id = 3L, nombre = "Noche"),
                ReservationTurnoDto(id = 1L, nombre = "Manana")
            )
        }
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.getTurnOptions()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals(1L, result.getOrNull()?.first()?.id)
        assertEquals("Manana", result.getOrNull()?.first()?.nombre)
    }

    @Test
    fun getOperationalTableOptions_success_mapsAndSortsTables() = runTest {
        val fakeApi = FakeReservationApi().apply {
            tables = listOf(
                ReservationMesaOperativaDto(numero = 8),
                ReservationMesaOperativaDto(numero = 2)
            )
        }
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.getOperationalTableOptions()

        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()?.size)
        assertEquals(2, result.getOrNull()?.first()?.numero)
    }

    @Test
    fun createReservation_forUser_mapsExpectedRequest() = runTest {
        val fakeApi = FakeReservationApi()
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.createReservation(
            CreateReservationCommand(
                role = UserRole.USER,
                fecha = "2026-04-15",
                turnoId = 2L,
                mesaNumero = 8,
                juegoNombre = "Catan"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals(
            ReservationUserRequestDto(
                fecha = "2026-04-15",
                mesa = ReservationMesaRefDto(8),
                turno = ioc.andresgq.gamehubmobile.data.remote.dto.ReservationTurnoRefDto(2L),
                juego = ReservationJuegoRefDto("Catan")
            ),
            fakeApi.lastUserRequest
        )
        assertNull(fakeApi.lastAdminRequest)
    }

    @Test
    fun createReservation_forAdmin_mapsExpectedRequest() = runTest {
        val fakeApi = FakeReservationApi()
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.createReservation(
            CreateReservationCommand(
                role = UserRole.ADMIN,
                fecha = "2026-04-15",
                turnoId = 3L,
                mesaNumero = 4,
                usuarioNombre = "ana"
            )
        )

        assertTrue(result.isSuccess)
        assertEquals("ana", fakeApi.lastAdminRequest?.usuario?.nombre)
        assertEquals(4, fakeApi.lastAdminRequest?.mesa?.numero)
        assertEquals(3L, fakeApi.lastAdminRequest?.turno?.id)
        assertNull(fakeApi.lastAdminRequest?.juego)
    }

    @Test
    fun createReservation_forAdmin_withoutUser_failsBeforeCallingApi() = runTest {
        val fakeApi = FakeReservationApi()
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(fakeApi)
        )

        val result = repository.createReservation(
            CreateReservationCommand(
                role = UserRole.ADMIN,
                fecha = "2026-04-15",
                turnoId = 3L,
                mesaNumero = 4,
                usuarioNombre = "   "
            )
        )

        assertTrue(result.isFailure)
        assertEquals(
            "El administrador debe indicar el usuario de la reserva",
            result.exceptionOrNull()?.message
        )
        assertNull(fakeApi.lastUserRequest)
        assertNull(fakeApi.lastAdminRequest)
    }

    @Test
    fun createReservation_withInvalidDate_failsValidation() = runTest {
        val repository = ReservationRepository(
            remoteDataSource = ReservationRemoteDataSource(FakeReservationApi())
        )

        val result = repository.createReservation(
            CreateReservationCommand(
                role = UserRole.USER,
                fecha = "15/04/2026",
                turnoId = 1L,
                mesaNumero = 2
            )
        )

        assertTrue(result.isFailure)
        assertEquals(
            "La fecha debe tener formato yyyy-MM-dd",
            result.exceptionOrNull()?.message
        )
    }

    private class FakeReservationApi : ReservationApi {
        var lastUserRequest: ReservationUserRequestDto? = null
        var lastAdminRequest: ReservationAdminRequestDto? = null
        var myReservations: List<ReservationListItemDto> = emptyList()
        var adminReservations: List<ReservationListItemDto> = emptyList()
        var turns: List<ReservationTurnoDto> = emptyList()
        var tables: List<ReservationMesaOperativaDto> = emptyList()

        override suspend fun getMyReservations(): List<ReservationListItemDto> = myReservations

        override suspend fun getAdminReservations(): List<ReservationListItemDto> = adminReservations

        override suspend fun getTurns(): List<ReservationTurnoDto> = turns

        override suspend fun getOperationalTables(): List<ReservationMesaOperativaDto> = tables

        override suspend fun getFreeTables(fecha: String, turnoId: Long): List<ReservationMesaOperativaDto> = tables

        override suspend fun createReservationAsUser(request: ReservationUserRequestDto) {
            lastUserRequest = request
        }

        override suspend fun createReservationAsAdmin(request: ReservationAdminRequestDto) {
            lastAdminRequest = request
        }
    }
}

