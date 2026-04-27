package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * Cuerpo para crear una reserva como usuario normal.
 *
 * Forma esperada (según contrato Swagger):
 * {
 *   "fecha": "yyyy-MM-dd",
 *   "mesa": { "id": 4 },
 *   "turno": { "id": 2 },
 *   "juego": { "nombre": "Catan" }
 * }
 *
 * @property fecha fecha de la reserva.
 * @property mesa referencia de mesa por id para reservas.
 * @property turno referencia de turno por id para reservas.
 * @property juego referencia de juego por nombre para reservas.
 */
data class ReservationUserRequestDto(
    val fecha: String,
    val mesa: ReservationMesaIdRefDto,
    val turno: ReservationTurnoRefDto,
    val juego: ReservationJuegoRefDto? = null
)

/**
 * Cuerpo para crear una reserva como administrador.
 * Amplía el request de usuario incluyendo el usuario objetivo de la reserva.
 *
 * Forma esperada (según contrato Swagger):
 * {
 *   "fecha": "yyyy-MM-dd",
 *   "mesa": { "id": 4 },
 *   "turno": { "id": 2 },
 *   "juego": { "nombre": "Catan" },
 *   "usuario": { "nombre": "usuarioObjetivo" }
 * }
 *
 * @property fecha fecha de la reserva
 * @property mesa referencia de mesa por id para reservas
 * @property turno referencia de turno por id para reservas
 * @property juego referencia de juego por nombre para reservas
 * @property usuario referencia de usuario por nombre para reservas
 */
data class ReservationAdminRequestDto(
    val fecha: String,
    val mesa: ReservationMesaIdRefDto,
    val turno: ReservationTurnoRefDto,
    val juego: ReservationJuegoRefDto? = null,
    val usuario: ReservationUsuarioRefDto
)

/**
 * Referencia mínima de mesa por id para envío en POST /reservas.
 * El servidor identifica la mesa por su id de base de datos.
 *
 * @property id id único de la mesa.
 */
data class ReservationMesaIdRefDto(
    val id: Long
)

/**
 * Referencia de mesa usada en respuestas de reservas (listados).
 * Contiene todos los campos que el servidor puede devolver dentro de un objeto Reserva.
 *
 * @property id id único de la mesa.
 * @property numero número visible de la mesa en el local.
 * @property capacidad capacidad máxima de jugadores.
 * @property operativa estado operativo de la mesa.
 */
data class ReservationMesaRefDto(
    val id: Long? = null,
    val numero: Int? = null,
    val capacidad: Int? = null,
    val operativa: Boolean? = null
)

/** Referencia mínima de turno por identificador.
 *
 * @property id identificador único del turno.
 */
data class ReservationTurnoRefDto(
    val id: Long
)

/** Referencia mínima de juego por nombre según contrato actual del backend.
 *
 * @property nombre nombre del juego.
 */
data class ReservationJuegoRefDto(
    val nombre: String
)

/** Referencia mínima de usuario por nombre.
 *
 * @property nombre nombre del usuario.
 */
data class ReservationUsuarioRefDto(
    val nombre: String
)

