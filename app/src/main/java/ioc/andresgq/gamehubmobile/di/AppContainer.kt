package ioc.andresgq.gamehubmobile.di

import android.content.Context
import androidx.room.Room
import ioc.andresgq.gamehubmobile.BuildConfig
import ioc.andresgq.gamehubmobile.data.local.AppDatabase
import ioc.andresgq.gamehubmobile.data.local.GameLocalDataSource
import ioc.andresgq.gamehubmobile.data.local.TokenManager
import ioc.andresgq.gamehubmobile.data.remote.AuthApi
import ioc.andresgq.gamehubmobile.data.remote.GameApi
import ioc.andresgq.gamehubmobile.data.remote.GameRemoteDataSource
import ioc.andresgq.gamehubmobile.data.remote.ReservationApi
import ioc.andresgq.gamehubmobile.data.remote.ReservationRemoteDataSource
import ioc.andresgq.gamehubmobile.data.repository.AuthRepository
import ioc.andresgq.gamehubmobile.data.repository.GameRepository
import ioc.andresgq.gamehubmobile.data.repository.ReservationRepository
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Contrato del contenedor de dependencias de la aplicación.
 *
 * Esta interfaz expone los repositorios y servicios compartidos que pueden ser
 * consumidos por otras capas de la app. Su objetivo es centralizar la creación
 * de dependencias y facilitar una forma simple de inyección manual.
 */
interface AppContainer {

    /**
     * Repositorio encargado de la autenticación y gestión de sesión del usuario.
     */
    val authRepository: AuthRepository

    /**
     * Repositorio encargado del acceso al catálogo de juegos.
     */
    val gameRepository: GameRepository

    /**
     * Repositorio encargado de crear reservas.
     */
    val reservationRepository: ReservationRepository
}

/**
 * Implementación por defecto del contenedor de dependencias de la aplicación.
 *
 * Esta clase construye y conecta los principales componentes de infraestructura:
 * - cliente HTTP con OkHttp,
 * - interceptor de autenticación,
 * - logging de red,
 * - instancia de Retrofit,
 * - base de datos local con Room,
 * - gestor de tokens,
 * - y repositorios.
 *
 * El contenedor recibe un [Context] para poder crear la base de datos usando el
 * contexto de aplicación y así evitar fugas de memoria.
 *
 * @param context contexto desde el que se inicializan dependencias que requieren acceso al sistema.
 * @constructor Crea un contenedor de dependencias para la aplicación.
 * @property authRepository repositorio de autenticación.
 * @property gameRepository repositorio de juegos.
 * @property reservationRepository repositorio de reservas.
 * @property tokenManager gestor de tokens de autenticación.
 * @property gameRemoteDataSource fuente remota de juegos.
 * @property gameLocalDataSource fuente local de juegos.
 * @property reservationRemoteDataSource fuente remota de reservas.
 * @property loggingInterceptor interceptor de logging para peticiones y respuestas HTTP.
 * @property authInterceptor interceptor que añade el token de autenticación a las peticiones.
 * @property okHttpClient cliente HTTP configurado con interceptores.
 * @property retrofit instancia de Retrofit para interactuar con la API.
 * @property authApi servicio de autenticación de la API.
 * @property gameApi servicio de juegos de la API.
 * @property reservationApi servicio de reservas de la API.
 * @property appDatabase base de datos local de la aplicación.
 */
class DefaultAppContainer(context: Context) : AppContainer {

    /**
     * Interceptor de logging para peticiones y respuestas HTTP.
     *
     * Se configura con nivel [HttpLoggingInterceptor.Level.BODY], lo que permite
     * registrar cabeceras, cuerpos de petición y respuesta. Es útil en desarrollo
     * para depurar la comunicación con la API.
     */
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    /**
     * Interceptor que añade automáticamente el token JWT a las peticiones HTTP.
     *
     * Antes de continuar la cadena de interceptores, recupera la sesión actual
     * desde [tokenManager] y, si existe un token, lo añade a la cabecera
     * `Authorization` con el formato `Bearer <token>`.
     *
     * Se usa `runBlocking` porque el interceptor de OkHttp no es suspendido y
     * necesita obtener el token de forma síncrona en el momento de construir
     * la petición.
     */
    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val path = originalRequest.url.encodedPath

        val isAuthEndpoint =
            path.endsWith("/auth/login") || path.endsWith("/auth/register")

        val alreadyHasAuthorization = originalRequest.header("Authorization") != null

        if (isAuthEndpoint || alreadyHasAuthorization) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val token = runBlocking {
            tokenManager.getSession()?.token?.trim().orEmpty()
        }

        if (token.isBlank()) {
            return@Interceptor chain.proceed(originalRequest)
        }

        val authenticatedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()

        chain.proceed(authenticatedRequest)
    }

    /**
     * Cliente HTTP principal de la aplicación.
     *
     * Incluye:
     * - [authInterceptor] para adjuntar el token a las peticiones,
     * - [loggingInterceptor] para registrar el tráfico de red.
     */
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .build()

    /**
     * Instancia de Retrofit configurada para acceder a la API remota.
     *
     * Usa la URL base definida en [BuildConfig.API_BASE_URL], el cliente HTTP
     * configurado en [okHttpClient] y Gson como convertidor JSON.
     */
    private val retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.API_BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    /**
     * Implementación de [AuthApi] generada por Retrofit.
     */
    private val authApi = retrofit.create(AuthApi::class.java)

    /**
     * Implementación de [GameApi] generada por Retrofit.
     */
    private val gameApi = retrofit.create(GameApi::class.java)

    /**
     * Implementación de [ReservationApi] generada por Retrofit.
     */
    private val reservationApi = retrofit.create(ReservationApi::class.java)

    /**
     * Base de datos local de la aplicación construida con Room.
     *
     * Se crea con el nombre `gamehub.db` y se apoya en la clase [AppDatabase]
     * para definir entidades y DAOs.
     */
    private val appDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "gamehub.db"
    )
        .addMigrations(AppDatabase.MIGRATION_1_2, AppDatabase.MIGRATION_2_3)
        .build()

    /**
     * Componente responsable de guardar, recuperar y eliminar la sesión local.
     *
     * Internamente utiliza el DAO expuesto por [appDatabase].
     */
    private val tokenManager = TokenManager(appDatabase.userSessionDao())

    /** Fuente de datos remota del catálogo de juegos. */
    private val gameRemoteDataSource = GameRemoteDataSource(gameApi)

    /** Fuente de datos local del catálogo de juegos. */
    private val gameLocalDataSource = GameLocalDataSource(appDatabase.gameDao())

    /** Fuente de datos remota para reservas. */
    private val reservationRemoteDataSource = ReservationRemoteDataSource(reservationApi)

    /**
     * Repositorio de autenticación expuesto por el contenedor.
     */
    override val authRepository: AuthRepository = AuthRepository(
        authApi = authApi,
        tokenManager = tokenManager
    )

    /**
     * Repositorio de juegos expuesto por el contenedor.
     */
    override val gameRepository: GameRepository = GameRepository(
        gameRemoteDataSource = gameRemoteDataSource,
        gameLocalDataSource = gameLocalDataSource
    )

    /**
     * Repositorio de reservas expuesto por el contenedor.
     */
    override val reservationRepository: ReservationRepository = ReservationRepository(
        remoteDataSource = reservationRemoteDataSource
    )
}