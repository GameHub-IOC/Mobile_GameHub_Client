package ioc.andresgq.gamehubmobile.data.remote.dto

import org.junit.Assert.assertEquals
import org.junit.Test

class ReservationRequestDtosTest {

    @Test
    fun userRequest_containsExpectedStructure() {
        val request = ReservationUserRequestDto(
            fecha = "2026-04-10",
            mesa = ReservationMesaRefDto(numero = 5),
            turno = ReservationTurnoRefDto(id = 2L),
            juego = ReservationJuegoRefDto(nombre = "Catan")
        )

        assertEquals("2026-04-10", request.fecha)
        assertEquals(5, request.mesa.numero)
        assertEquals(2L, request.turno.id)
        assertEquals("Catan", request.juego?.nombre)
    }

    @Test
    fun adminRequest_containsTargetUser() {
        val request = ReservationAdminRequestDto(
            fecha = "2026-04-10",
            mesa = ReservationMesaRefDto(numero = 7),
            turno = ReservationTurnoRefDto(id = 3L),
            juego = ReservationJuegoRefDto(nombre = "Azul"),
            usuario = ReservationUsuarioRefDto(nombre = "maria")
        )

        assertEquals("maria", request.usuario.nombre)
        assertEquals(7, request.mesa.numero)
        assertEquals(3L, request.turno.id)
        assertEquals("Azul", request.juego?.nombre)
    }
}

