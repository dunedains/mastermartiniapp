package cl.duoc.mastermartini

import android.content.Context
import cl.duoc.mastermartini.data.local.AppDatabase
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.FakeContentRepository
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.data.repository.ReservationRepository
import cl.duoc.mastermartini.data.repository.UserRepository
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.device.NotificationHelper

/**
 * Inyección de dependencias manual: crea una sola vez cada repositorio
 * y los comparte con los ViewModels (ver ui/AppViewModelProvider).
 */
class AppContainer(context: Context) {
    private val db = AppDatabase.get(context)
    val settings = SettingsDataStore(context)

    val content: ContentRepository = FakeContentRepository()
    val users = UserRepository(db.userDao(), settings)
    val quotes = QuoteRepository(db.quoteDao(), settings)
    val reservations = ReservationRepository(db.reservationDao(), settings)

    val notifications = NotificationHelper(context)
    val contact = ContactHelper
}
