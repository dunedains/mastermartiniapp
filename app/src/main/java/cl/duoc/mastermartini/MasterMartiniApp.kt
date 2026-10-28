package cl.duoc.mastermartini

import android.app.Application

class MasterMartiniApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        container.notifications.createChannel()
    }
}
