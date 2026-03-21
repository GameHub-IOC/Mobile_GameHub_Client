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
        val token = runBlocking { tokenManager.getSession()?.token }
        val request = chain.request().newBuilder().apply {
            token?.let { addHeader("Authorization", "Bearer $it") }
        }.build()
        chain.proceed(request)
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
     * Implementación de la interfaz [AuthApi] generada por Retrofit.
     *
     * Esta instancia encapsula las llamadas HTTP relacionadas con autenticación.
     */
    private val authApi = retrofit.create(AuthApi::class.java)

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
    ).build()

    /**
     * Componente responsable de guardar, recuperar y eliminar la sesión local.
     *
     * Internamente utiliza el DAO expuesto por [appDatabase].
     */
    private val tokenManager = TokenManager(appDatabase.userSessionDao())

    /**
     * Repositorio de autenticación expuesto por el contenedor.
     *
     * Se construye combinando la API remota [authApi] y el almacenamiento local
     * proporcionado por [tokenManager].
     */
    override val authRepository: AuthRepository = AuthRepository(
        authApi = authApi,
        tokenManager = tokenManager
    )
}