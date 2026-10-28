package cl.duoc.mastermartini.data.repository

import cl.duoc.mastermartini.data.local.QuoteDao
import cl.duoc.mastermartini.data.local.QuoteEntity
import cl.duoc.mastermartini.data.local.QuoteItemEntity
import cl.duoc.mastermartini.data.local.SettingsDataStore
import cl.duoc.mastermartini.data.model.QuoteLine
import cl.duoc.mastermartini.data.model.SentQuote
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map

data class QuoteRequest(
    val contactName: String,
    val address: String,
    val comuna: String,
    val deliveryDate: String,
    val notes: String,
)

/** Cotización en curso (carrito) y cotizaciones enviadas. Persistidas en Room. */
@OptIn(ExperimentalCoroutinesApi::class)
class QuoteRepository(
    private val dao: QuoteDao,
    private val settings: SettingsDataStore,
) {
    private val userId: Flow<Long?> = settings.settings.map { it.loggedUserId }

    val lines: Flow<List<QuoteLine>> = userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else dao.observeItems(id).map { list -> list.map { QuoteLine(it.productId, it.productName, it.format, it.quantity) } }
    }

    val sentQuotes: Flow<List<SentQuote>> = userId.flatMapLatest { id ->
        if (id == null) flowOf(emptyList())
        else dao.observeQuotes(id).map { list ->
            list.map {
                SentQuote(
                    id = it.id,
                    code = "COT-%04d".format(it.id),
                    itemsSummary = it.itemsSummary,
                    itemCount = it.itemCount,
                    address = it.address,
                    comuna = it.comuna,
                    deliveryDate = it.deliveryDate,
                    createdAt = it.createdAt,
                )
            }
        }
    }

    private suspend fun requireUser(): Long? = settings.settings.first().loggedUserId

    /** Agrega o reemplaza el producto: un producto queda con un solo formato en la cotización. */
    suspend fun setLine(productId: String, productName: String, format: String, quantity: Int) {
        val id = requireUser() ?: return
        dao.removeProduct(id, productId)
        dao.upsertItem(QuoteItemEntity(id, productId, productName, format, quantity))
    }

    suspend fun removeProduct(productId: String) {
        val id = requireUser() ?: return
        dao.removeProduct(id, productId)
    }

    /** Guarda la cotización y vacía el carrito. Devuelve el código generado. */
    suspend fun send(request: QuoteRequest): String? {
        val id = requireUser() ?: return null
        val items = dao.getItems(id)
        if (items.isEmpty()) return null
        val summary = items.joinToString(" · ") { "${it.productName} ${it.format} x${it.quantity}" }
        val quoteId = dao.insertQuote(
            QuoteEntity(
                userId = id,
                itemsSummary = summary,
                itemCount = items.size,
                contactName = request.contactName.trim(),
                address = request.address.trim(),
                comuna = request.comuna.trim(),
                deliveryDate = request.deliveryDate,
                notes = request.notes.trim(),
            )
        )
        dao.clearItems(id)
        return "COT-%04d".format(quoteId)
    }
}
