package ioc.andresgq.gamehubmobile.data.remote.dto

/**
 * DTO de turno devuelto por el backend, incluyendo franja horaria.
 *
 * El servidor serializa [horaInicio] y [horaFin] como cadenas ISO-8601 ("HH:mm:ss").
 * Se guardan como [String] para evitar problemas de deserialización con Gson.
 *
 * @property id identificador único del turno.
 * @property nombre nombre del turno.
 * @property horaInicio hora de inicio del turno en formato "HH:mm:ss", o null si no la devuelve el servidor.
 * @property horaFin hora de fin del turno en formato "HH:mm:ss", o null si no la devuelve el servidor.
 */
data class ReservationTurnoDto(
    val id: Long,
    val nombre: String,
    val horaInicio: String? = null,
    val horaFin: String? = null
)

/**
 * DTO completo de mesa operativa para el wizard de reserva.
 *
 * @property id identificador único de la mesa en base de datos.
 * @property numero número visible de la mesa en el local.
 * @property capacidad capacidad máxima de jugadores.
 * @property operativa si la mesa puede recibir reservas.
 */
data class ReservationMesaOperativaDto(
    val id: Long = 0L,
    val numero: Int,
    val capacidad: Int = 0,
    val operativa: Boolean = true
)

