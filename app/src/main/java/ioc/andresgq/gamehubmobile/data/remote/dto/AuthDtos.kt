package ioc.andresgq.gamehubmobile.data.remote.dto

data class LoginRequestDto(
    val nombre: String,
    val password: String
)

data class LoginResponseDto(
    val token: String,
    val nombre: String,
    val rol: String
)
