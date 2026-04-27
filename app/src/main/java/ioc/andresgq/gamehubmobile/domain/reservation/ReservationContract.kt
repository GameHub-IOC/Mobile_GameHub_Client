package ioc.andresgq.gamehubmobile.domain.reservation

/**
 * Rol de sesión que condiciona la experiencia de reservas.
 */
enum class UserRole {
    USER,
    ADMIN;

    companion object {
        fun fromRaw(value: String?): UserRole {
            return if (value.equals(ADMIN.name, ignoreCase = true)) ADMIN else USER
        }
    }
}

/**
 * Constantes de negocio para flujo de reservas.
 */
object ReservationContract {
    const val DATE_PATTERN = "yyyy-MM-dd"
}

