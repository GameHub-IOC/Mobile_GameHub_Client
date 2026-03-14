package ioc.andresgq.gamehubmobile.ui.navigation

import android.net.Uri

object AppDestinations {
    const val SessionGate = "session_gate"
    const val Login = "login"
    const val HomeRoutePattern = "home/{userType}"

    fun homeRoute(userType: String): String = "home/${Uri.encode(userType)}"
}
