package ioc.andresgq.gamehubmobile

import android.app.Application
import ioc.andresgq.gamehubmobile.di.AppContainer
import ioc.andresgq.gamehubmobile.di.DefaultAppContainer

class GameHubApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = DefaultAppContainer(this)
    }
}
