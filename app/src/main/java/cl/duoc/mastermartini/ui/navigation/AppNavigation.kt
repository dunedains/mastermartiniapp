package cl.duoc.mastermartini.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import cl.duoc.mastermartini.ui.screens.account.AccountScreen
import cl.duoc.mastermartini.ui.screens.account.EditProfileScreen
import cl.duoc.mastermartini.ui.screens.auth.LoginScreen
import cl.duoc.mastermartini.ui.screens.auth.RegisterScreen
import cl.duoc.mastermartini.ui.screens.catalog.CatalogScreen
import cl.duoc.mastermartini.ui.screens.catalog.QuoteFormScreen
import cl.duoc.mastermartini.ui.screens.home.HomeScreen
import cl.duoc.mastermartini.ui.screens.learn.LearnDetailScreen
import cl.duoc.mastermartini.ui.screens.learn.LearnScreen
import cl.duoc.mastermartini.ui.screens.services.BookingScreen
import cl.duoc.mastermartini.ui.screens.services.ServicesScreen

/** Todas las rutas de la app en un solo lugar. */
object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val LEARN = "learn?tab={tab}"
    const val CATALOG = "catalog"
    const val SERVICES = "services"
    const val ACCOUNT = "account"
    const val LEARN_DETAIL = "learn/detail/{id}"
    const val QUOTE = "quote"
    const val BOOKING = "booking/{id}"
    const val EDIT_PROFILE = "profile/edit"

    fun learn(tab: String? = null) = if (tab == null) "learn" else "learn?tab=$tab"
    fun learnDetail(id: String) = "learn/detail/$id"
    fun booking(id: String) = "booking/$id"
}

private data class Tab(val route: String, val navRoute: String, val label: String, val icon: ImageVector, val selectedIcon: ImageVector)

private val TABS = listOf(
    Tab(Routes.HOME, Routes.HOME, "Inicio", Icons.Outlined.Home, Icons.Filled.Home),
    Tab(Routes.LEARN, Routes.learn(), "Aprende", Icons.Outlined.PlayCircle, Icons.Filled.PlayCircle),
    Tab(Routes.CATALOG, Routes.CATALOG, "Catálogo", Icons.Outlined.ShoppingBag, Icons.Filled.ShoppingBag),
    Tab(Routes.SERVICES, Routes.SERVICES, "Servicios", Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    Tab(Routes.ACCOUNT, Routes.ACCOUNT, "Cuenta", Icons.Outlined.Person, Icons.Filled.Person),
)

// ---------- Transiciones entre pantallas ----------
private const val DURATION = 320
private fun AnimatedContentTransitionScope<NavBackStackEntry>.pushEnter(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURATION)) + fadeIn(tween(DURATION))
private fun AnimatedContentTransitionScope<NavBackStackEntry>.pushExit(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Start, tween(DURATION), targetOffset = { it / 4 }) + fadeOut(tween(DURATION))
private fun AnimatedContentTransitionScope<NavBackStackEntry>.popEnter(): EnterTransition =
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURATION), initialOffset = { it / 4 }) + fadeIn(tween(DURATION))
private fun AnimatedContentTransitionScope<NavBackStackEntry>.popExit(): ExitTransition =
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.End, tween(DURATION)) + fadeOut(tween(DURATION))

@Composable
fun AppNavigation(isLogged: Boolean, navController: NavHostController = rememberNavController()) {
    val snackbar = remember { SnackbarHostState() }
    // Se fija al iniciar: login/logout después navegan explícitamente (no recrean el grafo).
    val startDestination = remember { if (isLogged) Routes.HOME else Routes.LOGIN }
    val backStack by navController.currentBackStackEntryAsState()
    val currentDestination = backStack?.destination
    val showBottomBar = currentDestination?.route in TABS.map { it.route }

    // Navegar entre pestañas sin apilar pantallas y conservando su estado.
    fun goToTab(route: String) = navController.navigate(route) {
        // Inicio siempre queda en la base de la pila después del login.
        popUpTo(Routes.HOME) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }

    fun goHomeClearingAuth() = navController.navigate(Routes.HOME) {
        popUpTo(navController.graph.id) { inclusive = true }
    }

    Scaffold(
        // Los insets (barra de estado) los maneja cada pantalla; así no se duplican
        // en las pantallas de detalle que tienen su propio TopAppBar.
        contentWindowInsets = WindowInsets(0),
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
            ) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    TABS.forEach { tab ->
                        val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                        val scale by animateFloatAsState(if (selected) 1.15f else 1f, label = "tabIcon")
                        NavigationBarItem(
                            selected = selected,
                            onClick = { goToTab(tab.navRoute) },
                            icon = {
                                Icon(
                                    if (selected) tab.selectedIcon else tab.icon,
                                    contentDescription = null,
                                    modifier = Modifier.scale(scale),
                                )
                            },
                            label = { Text(tab.label) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding),
            enterTransition = { fadeIn(tween(250)) },
            exitTransition = { fadeOut(tween(200)) },
        ) {
            // ----- Autenticación -----
            composable(Routes.LOGIN) {
                LoginScreen(
                    onLoggedIn = ::goHomeClearingAuth,
                    onGoToRegister = { navController.navigate(Routes.REGISTER) },
                )
            }
            composable(
                Routes.REGISTER,
                enterTransition = { pushEnter() }, exitTransition = { pushExit() },
                popEnterTransition = { popEnter() }, popExitTransition = { popExit() },
            ) {
                RegisterScreen(
                    onRegistered = ::goHomeClearingAuth,
                    onGoToLogin = {
                        if (!navController.popBackStack(Routes.LOGIN, inclusive = false)) {
                            navController.navigate(Routes.LOGIN) { popUpTo(Routes.REGISTER) { inclusive = true } }
                        }
                    },
                )
            }

            // ----- Pestañas -----
            composable(Routes.HOME) { TabFrame {
                HomeScreen(
                    onOpenContent = { navController.navigate(Routes.learnDetail(it)) },
                    onSeeAllRecipes = { goToTab(Routes.learn("RECIPE")) },
                    onOpenServices = { goToTab(Routes.SERVICES) },
                )
            } }
            composable(
                Routes.LEARN,
                arguments = listOf(navArgument("tab") { type = NavType.StringType; nullable = true; defaultValue = null }),
            ) { TabFrame {
                LearnScreen(onOpenContent = { navController.navigate(Routes.learnDetail(it)) })
            } }
            composable(Routes.CATALOG) { TabFrame {
                CatalogScreen(onSendQuote = { navController.navigate(Routes.QUOTE) })
            } }
            composable(Routes.SERVICES) { TabFrame {
                ServicesScreen(
                    onBookAdvisory = { navController.navigate(Routes.booking(it)) },
                    onOpenCourse = { navController.navigate(Routes.learnDetail(it)) },
                    snackbar = snackbar,
                )
            } }
            composable(Routes.ACCOUNT) { TabFrame {
                AccountScreen(
                    onEditProfile = { navController.navigate(Routes.EDIT_PROFILE) },
                    onGoToCatalog = { goToTab(Routes.CATALOG) },
                    onLoggedOut = {
                        navController.navigate(Routes.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
                    },
                    snackbar = snackbar,
                )
            } }

            // ----- Pantallas de detalle / formularios -----
            composable(
                Routes.LEARN_DETAIL,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
                enterTransition = { pushEnter() }, exitTransition = { pushExit() },
                popEnterTransition = { popEnter() }, popExitTransition = { popExit() },
            ) {
                LearnDetailScreen(
                    onBack = { navController.popBackStack() },
                    onOpenCatalog = { goToTab(Routes.CATALOG) },
                )
            }
            composable(
                Routes.QUOTE,
                enterTransition = { pushEnter() }, exitTransition = { pushExit() },
                popEnterTransition = { popEnter() }, popExitTransition = { popExit() },
            ) {
                QuoteFormScreen(
                    onBack = { navController.popBackStack() },
                    onDone = { navController.popBackStack() },
                )
            }
            composable(
                Routes.BOOKING,
                arguments = listOf(navArgument("id") { type = NavType.StringType }),
                enterTransition = { pushEnter() }, exitTransition = { pushExit() },
                popEnterTransition = { popEnter() }, popExitTransition = { popExit() },
            ) {
                BookingScreen(onBack = { navController.popBackStack() })
            }
            composable(
                Routes.EDIT_PROFILE,
                enterTransition = { pushEnter() }, exitTransition = { pushExit() },
                popEnterTransition = { popEnter() }, popExitTransition = { popExit() },
            ) {
                EditProfileScreen(onBack = { navController.popBackStack() })
            }
        }
    }
}

/** Las pestañas no tienen TopAppBar: respetan la barra de estado con este marco. */
@Composable
private fun TabFrame(content: @Composable () -> Unit) {
    Box(Modifier.statusBarsPadding()) { content() }
}
