package ioc.andresgq.gamehubmobile.data.local

import ioc.andresgq.gamehubmobile.data.model.UserSession

class TokenManager(
    private val userSessionDao: UserSessionDao
) {
    suspend fun saveSession(session: UserSession) {
        userSessionDao.upsert(
            UserSessionEntity(
                token = session.token,
                username = session.username,
                userType = session.userType
            )
        )
    }

    suspend fun getSession(): UserSession? {
        return userSessionDao.getSession()?.let {
            UserSession(
                token = it.token,
                username = it.username,
                userType = it.userType
            )
        }
    }

    suspend fun clearSession() {
        userSessionDao.clear()
    }
}
