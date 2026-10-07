package com.sos.studentonstudy.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.sos.studentonstudy.ui.BottomItem
import com.sos.studentonstudy.ui.SosBackground
import com.sos.studentonstudy.ui.SosBottomBar
import com.sos.studentonstudy.ui.auth.LoginScreen
import com.sos.studentonstudy.ui.auth.RegisterScreen
import com.sos.studentonstudy.ui.dashboard.DashboardScreen
import com.sos.studentonstudy.ui.explore.ExploreScreen
import com.sos.studentonstudy.ui.profile.ProfileScreen
import com.sos.studentonstudy.ui.request.RequestFormScreen
import com.sos.studentonstudy.ui.request.RequestFormViewModel

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val HOME = "home"
    const val EXPLORE = "explore"
    const val PROFILE = "profile"
    const val REQUEST_FORM = "request_form?${RequestFormViewModel.REQUEST_ID_ARG}={${RequestFormViewModel.REQUEST_ID_ARG}}"

    fun requestForm(requestId: Long = RequestFormViewModel.NEW_REQUEST_ID) =
        "request_form?${RequestFormViewModel.REQUEST_ID_ARG}=$requestId"
}

private data class TopLevelDestination(val route: String, val label: String, val icon: ImageVector)

// Figma also has Chat and Recent tabs; they come after the rubric scope.
private val topLevelDestinations = listOf(
    TopLevelDestination(Routes.HOME, "Home", Icons.Outlined.Home),
    TopLevelDestination(Routes.EXPLORE, "Explore", Icons.Outlined.Explore),
    TopLevelDestination(Routes.PROFILE, "Profile", Icons.Outlined.Person)
)

@Composable
fun SosNavHost(isLoggedIn: Boolean, navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val showBottomBar = topLevelDestinations.any { it.route == currentRoute }

    Scaffold(
        containerColor = SosBackground,
        bottomBar = {
            if (showBottomBar) {
                SosBottomBar {
                    topLevelDestinations.forEach { destination ->
                        BottomItem(destination.label, destination.icon, currentRoute == destination.route) {
                            navController.navigateToTopLevel(destination.route)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.TopCenter) {
            NavHost(
                navController = navController,
                startDestination = if (isLoggedIn) Routes.HOME else Routes.LOGIN,
                modifier = Modifier.widthIn(max = 480.dp).fillMaxSize()
            ) {
                composable(Routes.LOGIN) {
                    LoginScreen(
                        onLoggedIn = { navController.enterApp() },
                        onRegister = { navController.navigate(Routes.REGISTER) { launchSingleTop = true } }
                    )
                }
                composable(Routes.REGISTER) {
                    RegisterScreen(
                        onRegistered = { navController.enterApp() },
                        onLogin = { navController.popBackStack() }
                    )
                }
                composable(Routes.HOME) {
                    DashboardScreen(
                        onAddRequest = { navController.navigate(Routes.requestForm()) },
                        onOpenRequest = { navController.navigate(Routes.requestForm(it)) },
                        onSeeAll = { navController.navigateToTopLevel(Routes.EXPLORE) }
                    )
                }
                composable(Routes.EXPLORE) {
                    ExploreScreen(
                        onAddRequest = { navController.navigate(Routes.requestForm()) },
                        onOpenRequest = { navController.navigate(Routes.requestForm(it)) }
                    )
                }
                composable(
                    Routes.REQUEST_FORM,
                    arguments = listOf(navArgument(RequestFormViewModel.REQUEST_ID_ARG) {
                        type = NavType.LongType
                        defaultValue = RequestFormViewModel.NEW_REQUEST_ID
                    })
                ) { entry ->
                    RequestFormScreen(onDone = {
                        // Guard against a double pop when save and back fire together.
                        if (navController.currentBackStackEntry == entry) navController.popBackStack()
                    })
                }
                composable(Routes.PROFILE) {
                    ProfileScreen(onLoggedOut = { navController.leaveApp() })
                }
            }
        }
    }
}

/** Switches bottom-bar tabs, keeping Home as the root and saving each tab's state. */
private fun NavHostController.navigateToTopLevel(route: String) = navigate(route) {
    popUpTo(Routes.HOME) { saveState = true }
    launchSingleTop = true
    restoreState = true
}

/** After login/register: Home becomes the root so Back exits the app instead of returning to Login. */
private fun NavHostController.enterApp() = navigate(Routes.HOME) {
    popUpTo(graph.id) { inclusive = true }
}

/** After logout: clear the whole back stack so Back cannot reopen private screens. */
private fun NavHostController.leaveApp() = navigate(Routes.LOGIN) {
    popUpTo(graph.id) { inclusive = true }
}
