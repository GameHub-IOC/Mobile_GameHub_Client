package ioc.andresgq.gamehubmobile

import android.app.Application
import ioc.andresgq.gamehubmobile.di.AppContainer
import ioc.andresgq.gamehubmobile.di.DefaultAppContainer

/**
 * Punto de entrada global de la aplicación.
 *
 * Esta clase extiende [Application] para inicializar dependencias compartidas
 * en cuanto el proceso de la app se crea. En este caso, construye un
 * [AppContainer] concreto que actuará como contenedor de dependencias para
 * el resto de componentes de la aplicación.
 */
class GameHubApplication : Application() {

    /**
     * Contenedor de dependencias de la aplicación.
     *
     * Se inicializa en [onCreate] con una implementación por defecto
     * ([DefaultAppContainer]) y solo puede modificarse desde esta clase.
     */
    lateinit var container: AppContainer
        private set

    /**
     * Inicializa el estado global de la aplicación al arrancar el proceso.
     *
     * Primero delega en la implementación base de [Application] y después
     * crea el contenedor de dependencias usando el contexto de aplicación
     * actual.
     */
    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
