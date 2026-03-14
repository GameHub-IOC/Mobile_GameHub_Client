package ioc.andresgq.gamehubmobile.di

import android.content.Context
import androidx.room.Room
import ioc.andresgq.gamehubmobile.BuildConfig
import ioc.andresgq.gamehubmobile.data.local.AppDatabase
import ioc.andresgq.gamehubmobile.data.local.TokenManager
import ioc.andresgq.gamehubmobile.data.remote.AuthApi
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

interface AppContainer {
    val authRepository: AuthRepository
}

class DefaultAppContainer(context: Context) : AppContainer {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = Interceptor { chain ->
        val token = runBlocking { tokenManager.getSession()?.token }
        val request = chain.request().newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(request)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val authApi = retrofit.create(AuthApi::class.java)

    private val appDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "gamehub.db"
    ).build()

    private val tokenManager = TokenManager(appDatabase.userSessionDao())

    override val authRepository: AuthRepository = AuthRepository(
        authApi = authApi,
        tokenManager = tokenManager
    )
}
