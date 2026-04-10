package ioc.andresgq.gamehubmobile.testutil

import ioc.andresgq.gamehubmobile.data.local.UserSessionDao
import ioc.andresgq.gamehubmobile.data.local.UserSessionEntity
import ioc.andresgq.gamehubmobile.data.remote.AuthApi
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.LoginResponseDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterRequestDto
import ioc.andresgq.gamehubmobile.data.remote.dto.RegisterResponseDto

class FakeAuthApi : AuthApi {
    var loginThrowable: Throwable? = null
    var registerThrowable: Throwable? = null

    var loginResponse: LoginResponseDto = LoginResponseDto(
        token = "token-default",
        nombre = "default-user",
        rol = "USER"
    )
    var registerResponse: RegisterResponseDto = RegisterResponseDto(
        id = 0L,
        nombre = "default-user",
        rol = "USER"
    )

    var lastLoginRequest: LoginRequestDto? = null
    var lastRegisterRequest: RegisterRequestDto? = null

    override suspend fun login(request: LoginRequestDto): LoginResponseDto {
        lastLoginRequest = request
        loginThrowable?.let { throw it }
        return loginResponse
    }

    override suspend fun register(request: RegisterRequestDto): RegisterResponseDto {
        lastRegisterRequest = request
        registerThrowable?.let { throw it }
        return registerResponse
    }
}

class FakeUserSessionDao : UserSessionDao {
    var storedSession: UserSessionEntity? = null
    var upsertCalls: Int = 0
    var clearCalls: Int = 0
    var clearThrowable: Throwable? = null

    override suspend fun getSession(): UserSessionEntity? = storedSession

    override suspend fun upsert(session: UserSessionEntity) {
        upsertCalls += 1
        storedSession = session
    }

    override suspend fun clear() {
        clearThrowable?.let { throw it }
        clearCalls += 1
        storedSession = null
    }
}

