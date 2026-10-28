package cl.duoc.mastermartini.ui.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.model.QuoteLine
import cl.duoc.mastermartini.data.repository.ContentRepository
import cl.duoc.mastermartini.data.repository.QuoteRepository
import cl.duoc.mastermartini.domain.validation.FieldState
import cl.duoc.mastermartini.domain.validation.Validators
import cl.duoc.mastermartini.ui.screens.learn.normalized
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Formulario de la hoja "Agregar a cotización": formato + cantidad. */
data class AddToQuoteForm(
    val product: Product,
    val format: String? = null,
    val formatError: String? = null,
    val quantity: FieldState = FieldState("1"),
    val failedAttempts: Int = 0,
    val isEditing: Boolean = false,
)

data class CatalogUiState(
    val query: String = "",
    val category: String? = null,
    val categories: List<String> = emptyList(),
    val products: List<Product> = emptyList(),
    val lines: Map<String, QuoteLine> = emptyMap(),
    val sheet: AddToQuoteForm? = null,
) {
    val quoteCount: Int get() = lines.size
}

class CatalogViewModel(
    private val content: ContentRepository,
    private val quotes: QuoteRepository,
) : ViewModel() {

    private val filters = MutableStateFlow(Triple("", null as String?, null as AddToQuoteForm?))

    val state: StateFlow<CatalogUiState> = combine(filters, quotes.lines) { (query, category, sheet), lines ->
        val q = query.normalized()
        CatalogUiState(
            query = query,
            category = category,
            categories = content.productCategories(),
            products = content.products().filter {
                (category == null || it.category == category) && (q.isBlank() || it.name.normalized().contains(q))
            },
            lines = lines.associateBy { it.productId },
            sheet = sheet,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CatalogUiState())

    fun onQuery(q: String) = filters.update { it.copy(first = q) }
    fun onCategory(c: String?) = filters.update { it.copy(second = c) }

    fun openSheet(product: Product) {
        val existing = state.value.lines[product.id]
        val form = AddToQuoteForm(
            product = product,
            // Si hay un solo formato lo dejamos preseleccionado.
            format = existing?.format ?: product.formats.singleOrNull(),
            quantity = FieldState(existing?.quantity?.toString() ?: "1"),
            isEditing = existing != null,
        )
        filters.update { it.copy(third = form) }
    }

    fun closeSheet() = filters.update { it.copy(third = null) }

    fun onFormat(f: String) = updateSheet { it.copy(format = f, formatError = null) }
    fun onQuantity(v: String) = updateSheet { it.copy(quantity = it.quantity.update(v.filter(Char::isDigit).take(3)) { q -> Validators.quantity(q) }.copy(touched = true)) }
    fun incQuantity(delta: Int) = updateSheet {
        val n = ((it.quantity.value.toIntOrNull() ?: 0) + delta).coerceIn(1, 999).toString()
        it.copy(quantity = it.quantity.update(n) { q -> Validators.quantity(q) })
    }

    fun confirmSheet() {
        val form = filters.value.third ?: return
        val formatError = Validators.selected(form.format, "Elige un formato").error
        val qty = form.quantity.touch { Validators.quantity(it) }
        if (formatError != null || qty.error != null) {
            updateSheet { it.copy(formatError = formatError, quantity = qty, failedAttempts = it.failedAttempts + 1) }
            return
        }
        viewModelScope.launch {
            quotes.setLine(form.product.id, form.product.name, form.format!!, qty.value.toInt())
            closeSheet()
        }
    }

    fun removeFromQuote(productId: String) {
        viewModelScope.launch { quotes.removeProduct(productId); closeSheet() }
    }

    private fun updateSheet(block: (AddToQuoteForm) -> AddToQuoteForm) =
        filters.update { f -> f.copy(third = f.third?.let(block)) }
}
