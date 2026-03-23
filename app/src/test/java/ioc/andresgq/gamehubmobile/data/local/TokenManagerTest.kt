package ioc.andresgq.gamehubmobile.data.local

import ioc.andresgq.gamehubmobile.data.model.UserSession
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TokenManagerTest {

    @Test
    fun saveSession_persistsEntityInDao() = runTest {
        val fakeDao = FakeUserSessionDao()
        val tokenManager = TokenManager(fakeDao)
        val session = UserSession("token-1", "andres", "ADMIN")

        tokenManager.saveSession(session)

        assertEquals(1, fakeDao.upsertCalls)
        assertEquals(
            UserSessionEntity(
                id = 0,
                token = "token-1",
                username = "andres",
                userType = "ADMIN"
            ),
            fakeDao.storedSession
        )
    }

    @Test
    fun getSession_returnsMappedDomainModel() = runTest {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(
                id = 0,
                token = "token-2",
                username = "maria",
                userType = "USER"
            )
        }
        val tokenManager = TokenManager(fakeDao)

        val session = tokenManager.getSession()

        assertEquals(UserSession("token-2", "maria", "USER"), session)
    }

    @Test
    fun getSession_returnsNullWhenDaoIsEmpty() = runTest {
        val tokenManager = TokenManager(FakeUserSessionDao())

        val session = tokenManager.getSession()

        assertNull(session)
    }

    @Test
    fun clearSession_deletesSessionFromDao() = runTest {
        val fakeDao = FakeUserSessionDao().apply {
            storedSession = UserSessionEntity(0, "token-3", "pepe", "USER")
        }
        val tokenManager = TokenManager(fakeDao)

        tokenManager.clearSession()

        assertEquals(1, fakeDao.clearCalls)
        assertNull(fakeDao.storedSession)
    }

    private class FakeUserSessionDao : UserSessionDao {
        var storedSession: UserSessionEntity? = null
        var upsertCalls: Int = 0
        var clearCalls: Int = 0

        override suspend fun getSession(): UserSessionEntity? = storedSession

        override suspend fun upsert(session: UserSessionEntity) {
            upsertCalls += 1
            storedSession = session
        }

        override suspend fun clear() {
            clearCalls += 1
            storedSession = null
        }
    }
}

