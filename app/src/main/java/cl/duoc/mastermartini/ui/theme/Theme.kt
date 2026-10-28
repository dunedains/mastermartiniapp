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

// ---------- Paleta de marca: lujo italiano (Rosso Corsa, nero, oro) sobre grises estilo Apple ----------
val MMRosso = Color(0xFFD40000)        // Rosso Corsa
val MMRossoDeep = Color(0xFF9B0000)
val MMRossoSoft = Color(0xFFFCE8E8)
val MMNero = Color(0xFF111113)         // fondo de tarjetas destacadas
val MMOro = Color(0xFFC9A55C)          // champagne / oro
val MMOroSoft = Color(0xFFF6EFDF)
val MMVerde = Color(0xFF008C45)        // verde de la bandera italiana
val MMVerdeSoft = Color(0xFFE2F4EA)
val MMGroupedBg = Color(0xFFF2F2F7)    // fondo agrupado de iOS
val MMFill = Color(0xFFE9E9EE)         // botones cápsula "OBTENER"
val MMText = Color(0xFF1D1D1F)
val MMTextMuted = Color(0xFF6E6E73)
val MMSeparator = Color(0xFFD8D8DD)

private val BrandScheme = lightColorScheme(
    primary = MMRosso,
    onPrimary = Color.White,
    primaryContainer = MMRossoSoft,
    onPrimaryContainer = MMRossoDeep,
    secondary = MMNero,
    onSecondary = Color.White,
    secondaryContainer = MMFill,
    onSecondaryContainer = MMText,
    tertiary = MMOro,
    onTertiary = MMNero,
    tertiaryContainer = MMOroSoft,
    onTertiaryContainer = Color(0xFF5C4515),
    background = MMGroupedBg,
    onBackground = MMText,
    surface = Color.White,
    onSurface = MMText,
    surfaceVariant = MMFill,
    onSurfaceVariant = MMTextMuted,
    surfaceContainer = Color.White,
    surfaceContainerLow = Color.White,
    surfaceContainerHigh = MMFill,
    surfaceContainerHighest = MMFill,
    outline = MMSeparator,
    outlineVariant = MMSeparator,
    error = Color(0xFFFF3B30),
)

/** Esquema "Alto contraste" activable en Mi cuenta (accesibilidad). */
private val HighContrastScheme = lightColorScheme(
    primary = Color(0xFF8A0000),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFD9D6),
    onPrimaryContainer = Color(0xFF3A0002),
    secondary = Color.Black,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6E6E6),
    onSecondaryContainer = Color.Black,
    tertiary = Color(0xFF6B4E00),
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
    surfaceContainerHighest = Color(0xFFEDEDED),
    outline = Color.Black,
    outlineVariant = Color(0xFF444444),
    error = Color(0xFF8C0009),
)

/** Colores extra que Material 3 no trae (estado "agregado", badges, tarjeta destacada). */
@Immutable
data class ExtraColors(val success: Color, val successContainer: Color, val badge: Color, val hero: Color)

val LocalExtraColors = staticCompositionLocalOf {
    ExtraColors(MMVerde, MMVerdeSoft, MMOro, MMNero)
}

// Tipografía tipo SF Pro: sans-serif, títulos grandes muy gruesos y con tracking negativo.
private val Sans = FontFamily.SansSerif

private val MMTypography = Typography(
    displaySmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 41.sp, letterSpacing = (-0.6).sp),
    headlineMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.ExtraBold, fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.6).sp),
    headlineSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.4).sp),
    titleLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 22.sp, lineHeight = 28.sp, letterSpacing = (-0.3).sp),
    titleMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 25.sp, letterSpacing = (-0.2).sp),
    titleSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    bodyLarge = TextStyle(fontFamily = Sans, fontSize = 17.sp, lineHeight = 22.sp),
    bodyMedium = TextStyle(fontFamily = Sans, fontSize = 15.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = Sans, fontSize = 13.sp, lineHeight = 18.sp),
    labelLarge = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 21.sp),
    labelMedium = TextStyle(fontFamily = Sans, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp),
    labelSmall = TextStyle(fontFamily = Sans, fontWeight = FontWeight.Bold, fontSize = 11.sp, lineHeight = 13.sp, letterSpacing = 1.2.sp),
)

// Esquinas generosas, como las tarjetas de la App Store.
private val MMShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(22.dp),
    extraLarge = RoundedCornerShape(28.dp),
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
    val extra = if (highContrast) ExtraColors(Color(0xFF0B4D17), Color(0xFFD5EED6), Color(0xFF6B4E00), Color.Black)
    else ExtraColors(MMVerde, MMVerdeSoft, MMOro, MMNero)
    val density = LocalDensity.current
    // "Texto grande": aumenta 20 % la escala de fuente sobre la del sistema.
    val scaledDensity = if (largeText) Density(density.density, density.fontScale * 1.2f) else density

    CompositionLocalProvider(LocalDensity provides scaledDensity, LocalExtraColors provides extra) {
        MaterialTheme(colorScheme = scheme, typography = MMTypography, shapes = MMShapes, content = content)
    }
}
