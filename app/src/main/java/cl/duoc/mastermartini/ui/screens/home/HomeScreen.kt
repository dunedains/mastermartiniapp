package cl.duoc.mastermartini.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconToggleButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mastermartini.data.model.LearnItem
import cl.duoc.mastermartini.ui.AppViewModelProvider
import cl.duoc.mastermartini.ui.components.BrandHeader
import cl.duoc.mastermartini.ui.components.MMCard
import cl.duoc.mastermartini.ui.components.Pill
import cl.duoc.mastermartini.ui.components.SectionHeader
import cl.duoc.mastermartini.ui.theme.LocalExtraColors

@Composable
fun HomeScreen(
    onOpenContent: (String) -> Unit,
    onSeeAllRecipes: () -> Unit,
    onOpenServices: () -> Unit,
    vm: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory),
) {
    val s by vm.state.collectAsStateWithLifecycle()
    var appeared by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { appeared = true }

    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        item {
            BrandHeader(Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
                OutlinedIconToggleButton(
                    checked = s.largeText,
                    onCheckedChange = { vm.toggleLargeText() },
                    modifier = Modifier.semantics { contentDescription = "Texto grande" },
                ) { Text("Aa", fontWeight = FontWeight.Bold) }
            }
        }
        item {
            Column(Modifier.padding(horizontal = 20.dp)) {
                Text("Hola, ${s.user?.firstName.orEmpty()}", style = MaterialTheme.typography.headlineMedium)
                Text(
                    "Esto preparamos para tu ${s.user?.businessType?.label?.lowercase() ?: "negocio"}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            AnimatedVisibility(appeared, enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 3 }) {
                s.featured?.let { FeaturedVideoCard(it) { onOpenContent(it.id) } }
            }
        }
        item {
            Column {
                Box(Modifier.padding(start = 20.dp, end = 8.dp)) {
                    SectionHeader("Recetas de temporada", action = "Ver todas", onAction = onSeeAllRecipes)
                }
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(s.recipes, key = { it.id }) { recipe ->
                        RecipeCard(recipe) { onOpenContent(recipe.id) }
                    }
                }
            }
        }
        s.nextEvent?.let { event ->
            item {
                MMCard(Modifier.padding(horizontal = 20.dp), onClick = onOpenServices) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(
                            Modifier
                                .size(64.dp)
                                .clip(MaterialTheme.shapes.small)
                                .background(MaterialTheme.colorScheme.primary),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            val parts = event.dateLabel.orEmpty().split(" ")
                            Text(parts.getOrNull(2)?.take(3)?.uppercase().orEmpty(), color = Color.White, style = MaterialTheme.typography.labelSmall)
                            Text(parts.firstOrNull().orEmpty(), color = Color.White, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (s.nextEventReserved) "YA ESTÁS INSCRITA/O" else "PRÓXIMO EVENTO",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (s.nextEventReserved) LocalExtraColors.current.success else MaterialTheme.colorScheme.primary,
                            )
                            Text(event.title, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${event.detail.substringBefore(" ·")} · ${event.availableSeats} cupos disponibles",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedVideoCard(item: LearnItem, onClick: () -> Unit) {
    // Pulso suave en el botón de play para invitar a tocarlo.
    val pulse = rememberInfiniteTransition(label = "pulse")
    val scale by pulse.animateFloat(
        initialValue = 1f, targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "playScale",
    )
    val extra = LocalExtraColors.current
    Card(
        onClick = onClick,
        modifier = Modifier.padding(horizontal = 20.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = extra.hero, contentColor = Color.White),
    ) {
        Column(Modifier.padding(20.dp)) {
            if (item.isNew) Pill("Video nuevo", extra.badge, extra.hero)
            Box(Modifier.fillMaxWidth().height(90.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(58.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Filled.PlayArrow, "Reproducir video", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
                }
            }
            Text(item.title, style = MaterialTheme.typography.titleMedium)
            Text("${item.author} · ${item.duration}", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.8f))
        }
    }
}

@Composable
private fun RecipeCard(item: LearnItem, onClick: () -> Unit) {
    MMCard(Modifier.width(170.dp), onClick = onClick) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(96.dp)
                .background(Color(item.thumbColor))
        )
        Column(Modifier.padding(12.dp)) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, maxLines = 2, minLines = 2)
            Text(
                "${item.difficulty.label} · ${item.duration}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
