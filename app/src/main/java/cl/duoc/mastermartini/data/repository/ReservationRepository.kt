package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.local.ReservationDao
import cl.duoc.mastermartini.data.local.ReservationEntity
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.Reservation
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

sealed interface ReserveResult {
    data object Success : ReserveResult
    data object AlreadyReserved : ReserveResult
    data object NotLogged : ReserveResult
}

/** Inscripciones a eventos y reservas de asesorías, guardadas en Room. */
@OptIn(ExperimentalCoroutinesApi::class)
class ReservationRepository(
    private val dao: ReservationDao,
    private val settings: SettingsDataStore,
) {
    val reservations: Flow<List<Reservation>> = settings.settings
        .map { it.loggedUserId }
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList())
            else dao.observeAll(id).map { list ->
                list.map { Reservation(it.id, it.serviceId, it.title, it.dateLabel, it.modality, it.createdAt) }
            }
        }

    /**
     * @param allowRepeat las asesorías se pueden reservar más de una vez; los eventos no.
     */
    suspend fun reserve(
        serviceId: String,
        title: String,
        dateLabel: String,
        modality: String,
        notes: String = "",
        allowRepeat: Boolean = false,
    ): ReserveResult {
        val userId = settings.settings.first().loggedUserId ?: return ReserveResult.NotLogged
        if (!allowRepeat && dao.countFor(userId, serviceId) > 0) return ReserveResult.AlreadyReserved
        dao.insert(ReservationEntity(userId = userId, serviceId = serviceId, title = title, dateLabel = dateLabel, modality = modality, notes = notes))
        return ReserveResult.Success
    }

    suspend fun cancel(id: Long) = dao.delete(id)
}
