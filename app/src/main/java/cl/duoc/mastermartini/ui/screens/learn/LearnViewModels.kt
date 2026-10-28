package cl.duoc.mastermartini.ui.screens.learn

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.data.model.Product
import cl.duoc.mastermartini.data.repository.ContentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.Normalizer

data class LearnUiState(
    val query: String = "",
    val kind: LearnKind = LearnKind.VIDEO,
    val category: String? = null,
    val categories: List<String> = emptyList(),
    val items: List<LearnItem> = emptyList(),
)

/** Filtro de "Aprende": búsqueda + pestaña (videos/recetas/cursos) + categoría. */
class LearnViewModel(
    savedState: SavedStateHandle,
    private val content: ContentRepository,
) : ViewModel() {

    private val initialKind = savedState.get<String>("tab")?.let { t -> LearnKind.entries.firstOrNull { it.name == t } }
        ?: LearnKind.VIDEO

    private val _state = MutableStateFlow(compute(LearnUiState(kind = initialKind)))
    val state: StateFlow<LearnUiState> = _state.asStateFlow()

    fun onQuery(q: String) = _state.update { compute(it.copy(query = q)) }
    fun onKind(k: LearnKind) = _state.update { compute(it.copy(kind = k)) }
    fun onCategory(c: String?) = _state.update { compute(it.copy(category = c)) }

    private fun compute(s: LearnUiState): LearnUiState {
        val ofKind = content.learnItems().filter { it.kind == s.kind }
        val q = s.query.normalized()
        val filtered = ofKind.filter { item ->
            (s.category == null || item.category == s.category) &&
                (q.isBlank() || item.title.normalized().contains(q) || item.category.normalized().contains(q))
        }
        return s.copy(categories = content.learnItems().map { it.category }.distinct(), items = filtered)
    }
}

/** Ignora mayúsculas y tildes al buscar ("pasteleria" encuentra "Pastelería"). */
internal fun String.normalized(): String =
    Normalizer.normalize(lowercase().trim(), Normalizer.Form.NFD).replace(Regex("\\p{M}"), "")

data class LearnDetailUiState(val item: LearnItem? = null, val products: List<Product> = emptyList())

class LearnDetailViewModel(
    savedState: SavedStateHandle,
    content: ContentRepository,
) : ViewModel() {
    val state: LearnDetailUiState = run {
        val item = savedState.get<String>("id")?.let(content::learnItem)
        LearnDetailUiState(item, item?.productIds?.mapNotNull(content::product).orEmpty())
    }
}
