package ioc.andresgq.gamehubmobile.data.model

data class UserSession(
    val token: String,
    val username: String,
    val userType: String
)
