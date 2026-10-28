package cl.duoc.mastermartini.ui.screens.learn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.data.model.LearnKind
import cl.duoc.mastermartini.device.ContactHelper
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.EmptyState
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.Pill
import cl.duoc.mastermartini.ui.components.ScreenTitle
import cl.duoc.mastermartini.ui.components.VideoThumb
import kotlinx.coroutines.launch

@Composable
fun LearnScreen(
    onOpenContent: (String) -> Unit,
    vm: LearnViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.fillMaxSize()) {
        ScreenTitle("Aprende", Modifier.padding(horizontal = 20.dp, vertical = 12.dp))
        SearchField(s.query, vm::onQuery, "Buscar receta o video", Modifier.padding(horizontal = 20.dp))
        Spacer(Modifier.height(12.dp))
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
            LearnKind.entries.forEachIndexed { i, kind ->
                SegmentedButton(
                    selected = s.kind == kind,
                    onClick = { vm.onKind(kind) },
                    shape = SegmentedButtonDefaults.itemShape(i, LearnKind.entries.size),
                    colors = SegmentedButtonDefaults.colors(
                        activeContainerColor = MaterialTheme.colorScheme.surface,
                        inactiveContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) { Text(kind.label) }
            }
        }
        Spacer(Modifier.height(12.dp))
        CategoryChips(s.categories, s.category, vm::onCategory)
        Spacer(Modifier.height(8.dp))

        // Transición lateral al cambiar de pestaña.
        AnimatedContent(
            targetState = s.kind,
            transitionSpec = {
                val dir = if (targetState.ordinal > initialState.ordinal) 1 else -1
                (slideInHorizontally { it * dir / 3 } + fadeIn()) togetherWith (slideOutHorizontally { -it * dir / 3 } + fadeOut())
            },
            label = "learnTab",
        ) { _ ->
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize(),
            ) {
                if (s.items.isEmpty()) item { EmptyState("No encontramos resultados. Prueba otra búsqueda o categoría.") }
                items(s.items, key = { it.id }) { item ->
                    LearnRow(item, Modifier.animateItem()) { onOpenContent(item.id) }
                }
            }
        }
    }
}

@Composable
fun SearchField(value: String, onChange: (String) -> Unit, placeholder: String, modifier: Modifier = Modifier) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier.fillMaxWidth(),
        placeholder = { Text(placeholder) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (value.isNotEmpty()) IconButton(onClick = { onChange("") }) { Icon(Icons.Filled.Close, "Limpiar búsqueda") }
        },
        singleLine = true,
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline,
        ),
    )
}

@Composable
fun CategoryChips(categories: List<String>, selected: String?, onSelect: (String?) -> Unit) {
    LazyRow(contentPadding = PaddingValues(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item { CategoryChip("Todo", selected == null) { onSelect(null) } }
        items(categories) { c -> CategoryChip(c, selected == c) { onSelect(c) } }
    }
}

@Composable
private fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, Modifier.padding(vertical = 8.dp)) },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surface,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
        ),
    )
}

@Composable
private fun LearnRow(item: LearnItem, modifier: Modifier, onClick: () -> Unit) {
    MMCard(modifier, onClick = onClick) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            VideoThumb(item.thumbColor, showPlay = item.kind == LearnKind.VIDEO)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleSmall)
                Text(
                    "${item.category} · ${item.duration} · ${item.difficulty.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Detalle de video/receta/curso: pasos, insumos usados (→ catálogo) y compartir (recurso nativo). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LearnDetailScreen(
    onBack: () -> Unit,
    onOpenCatalog: () -> Unit,
    vm: LearnDetailViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val item = vm.state.item
    val context = LocalContext.current
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(item?.kind?.label?.dropLast(1) ?: "Contenido") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                actions = {
                    if (item != null) IconButton(onClick = {
                        val ok = ContactHelper.share(
                            context, item.title,
                            "Mira \"${item.title}\" en la app Master Martini (${item.category} · ${item.duration}).",
                        )
                        if (!ok) scope.launch { snackbar.showSnackbar("No hay apps para compartir") }
                    }) { Icon(Icons.Outlined.Share, "Compartir") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        if (item == null) {
            EmptyState("Contenido no encontrado", Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(item.thumbColor), MaterialTheme.shapes.large),
                contentAlignment = Alignment.Center,
            ) {
                if (item.kind == LearnKind.VIDEO) {
                    Box(
                        Modifier.size(64.dp).background(Color.White, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Icon(Icons.Filled.PlayArrow, "Reproducir", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(34.dp)) }
                }
            }
            Text(item.title, style = MaterialTheme.typography.headlineSmall)
            Text(
                "${item.author} · ${item.category} · ${item.duration} · ${item.difficulty.label}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(item.description, style = MaterialTheme.typography.bodyLarge)
            Text("Paso a paso", style = MaterialTheme.typography.titleMedium)
            item.steps.forEachIndexed { i, step ->
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(26.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) { Text("${i + 1}", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.labelMedium) }
                    Spacer(Modifier.width(10.dp))
                    Text(step, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 2.dp))
                }
            }
            if (vm.state.products.isNotEmpty()) {
                MMCard(container = MaterialTheme.colorScheme.primaryContainer) {
                    Column(Modifier.padding(16.dp)) {
                        Pill("Insumos de este contenido", Color.Transparent, MaterialTheme.colorScheme.primary)
                        Text(
                            vm.state.products.joinToString(" · ") { it.name },
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(vertical = 6.dp),
                        )
                        TextButton(onClick = onOpenCatalog, contentPadding = PaddingValues(0.dp)) {
                            Text("Ver en catálogo", textDecoration = TextDecoration.Underline)
                            Spacer(Modifier.width(4.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, Modifier.size(18.dp))
                        }
                    }
                }
            }
        }
    }
}
