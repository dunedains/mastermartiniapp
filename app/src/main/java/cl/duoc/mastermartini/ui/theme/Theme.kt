package cl.duoc.mastermartini.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.shape.RoundedCornerShape

// ---------- Paleta de marca (tomada del mockup) ----------
val MMRed = Color(0xFF9E2A2B)
val MMRedSoft = Color(0xFFF3E1DE)
val MMCream = Color(0xFFF5EFE6)
val MMBeige = Color(0xFFEDE3D6)
val MMBrown = Color(0xFF3B2A22)
val MMText = Color(0xFF2B201B)
val MMTextMuted = Color(0xFF6E6159)
val MMOutline = Color(0xFFDCCFBF)
val MMGold = Color(0xFFD4A24C)
val MMGreen = Color(0xFF2E6B3A)
val MMGreenSoft = Color(0xFFE3F0E3)

private val BrandScheme = lightColorScheme(
    primary = MMRed,
    onPrimary = Color.White,
    primaryContainer = MMRedSoft,
    onPrimaryContainer = MMRed,
    secondary = MMBrown,
    onSecondary = Color.White,
    secondaryContainer = MMBeige,
    onSecondaryContainer = MMText,
    tertiary = MMGold,
    onTertiary = MMBrown,
    background = MMCream,
    onBackground = MMText,
    surface = Color.White,
    onSurface = MMText,
    surfaceVariant = MMBeige,
    onSurfaceVariant = MMTextMuted,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = MMBeige,
    outline = MMOutline,
    outlineVariant = MMOutline,
    error = Color(0xFFB3261E),
)

/** Esquema "Alto contraste" activable en Mi cuenta (accesibilidad). */
private val HighContrastScheme = lightColorScheme(
    primary = Color(0xFF6B0F12),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9D6),
    onPrimaryContainer = Color(0xFF3A0002),
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E6E6),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF7A5200),
    onTertiary = Color.White,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFEDEDED),
    onSurfaceVariant = Color(0xFF1F1F1F),
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = Color(0xFFEDEDED),
    outline = Color.Black,
    outlineVariant = Color(0xFF444444),
    error = Color(0xFF8C0009),
)

/** Colores extra que Material 3 no trae (estado "agregado", badges). */
@Immutable
data class ExtraColors(val success: Color, val successContainer: Color, val badge: Color, val hero: Color)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(MMGreen, MMGreenSoft, MMGold, MMBrown)
}

private val Serif = FontFamily.Serif
private val Sans = FontFamily.SansSerif

private val MMTypography = Typography(
    displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 30.sp, lineHeight = 36.sp),
    headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 18.sp, lineHeight = 24.sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 14.sp, letterSpacing = 0.6.sp),
)

private val MMShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(18.dp),
)

@Composable
private fun ColorScheme.animated(): ColorScheme {
    // Transición suave al activar/desactivar alto contraste.
    val spec = tween<Color>(400)
    val p by animateColorAsState(primary, spec, label = "primary")
    val bg by animateColorAsState(background, spec, label = "bg")
    val onBg by animateColorAsState(onBackground, spec, label = "onBg")
    val onSurf by animateColorAsState(onSurface, spec, label = "onSurf")
    val onVar by animateColorAsState(onSurfaceVariant, spec, label = "onVar")
    val out by animateColorAsState(outline, spec, label = "outline")
    return copy(primary = p, background = bg, onBackground = onBg, onSurface = onSurf, onSurfaceVariant = onVar, outline = out)
}

@Composable
fun MasterMartiniTheme(
    largeText: Boolean = false,
    highContrast: Boolean = false,
    content: @Composable () -> Unit,
) {
    val scheme = (if (highContrast) HighContrastScheme else BrandScheme).animated()
    val extra = if (highContrast) ExtraColors(Color(0xFF0B4D17), Color(0xFFD5EED6), Color(0xFF7A5200), Color.Black)
    else ExtraColors(MMGreen, MMGreenSoft, MMGold, MMBrown)
    val density = LocalDensity.current
    // "Texto grande": aumenta 20 % la escala de fuente sobre la del sistema.
    val scaledDensity = if (largeText) Density(density.density, density.fontScale * 1.2f) else density

    CompositionLocalProvider(LocalDensity provides scaledDensity, LocalExtraColors provides extra) {
        MaterialTheme(colorScheme = scheme, typography = MMTypography, shapes = MMShapes, content = content)
    }
}
