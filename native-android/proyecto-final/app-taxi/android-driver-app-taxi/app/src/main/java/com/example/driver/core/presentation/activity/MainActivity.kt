package com.example.driver.core.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.driver.R
import com.example.driver.commons.presentation.MOTION_MEDIUM_MS
import com.example.driver.commons.presentation.ToastHost
import com.example.driver.commons.presentation.ToastMessage
import com.example.driver.commons.presentation.ToastType
import com.example.driver.core.presentation.theme.AndroidTheme
import com.example.driver.features.home.presentation.HomeRoute
import com.example.driver.features.signin.presentation.SignInRoute
import com.example.driver.features.splash.presentation.SplashRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

sealed class Route(val path: String) {
    data object Splash : Route("splash")
    data object SignIn : Route("sign_in")
    data object Home : Route("home")
}

private const val SESSION_EXPIRED_TOAST_MS = 3_000L

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = android.graphics.Color.TRANSPARENT,
                darkScrim = android.graphics.Color.TRANSPARENT
            )
        )
        window.isNavigationBarContrastEnforced = false

        setContent {
            AndroidTheme { AppRoot() }
        }
    }
}

@Composable
private fun AppRoot(vm: MainViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    var sessionExpired by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.sessionExpired.collectLatest {
            nav.navigateClearingBackStack(Route.SignIn.path)
            sessionExpired = true
            delay(SESSION_EXPIRED_TOAST_MS)
            sessionExpired = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(
            navController = nav,
            startDestination = Route.Splash.path,
            enterTransition = { forwardEnter() },
            exitTransition = { forwardExit() },
            popEnterTransition = { backEnter() },
            popExitTransition = { backExit() }
        ) {
            composable(Route.Splash.path) {
                SplashRoute(
                    onGoSignIn = { nav.navigateClearingBackStack(Route.SignIn.path) },
                    onGoHome = { nav.navigateClearingBackStack(Route.Home.path) }
                )
            }

            composable(Route.SignIn.path) {
                SignInRoute(onGoHome = { nav.navigateClearingBackStack(Route.Home.path) })
            }

            composable(Route.Home.path) {
                HomeRoute(onLoggedOut = { nav.navigateClearingBackStack(Route.SignIn.path) })
            }
        }

        ToastHost(
            toast = if (sessionExpired) {
                ToastMessage(ToastType.Error, stringResource(R.string.common_session_expired))
            } else {
                null
            },
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

private typealias NavTransitionScope = AnimatedContentTransitionScope<NavBackStackEntry>

private fun NavTransitionScope.leavingSplash() = initialState.destination.route == Route.Splash.path

private fun NavTransitionScope.forwardEnter(): EnterTransition =
    if (leavingSplash()) {
        fadeIn(tween(MOTION_MEDIUM_MS))
    } else {
        slideIntoContainer(SlideDirection.Start, tween(MOTION_MEDIUM_MS))
    }

private fun NavTransitionScope.forwardExit(): ExitTransition =
    if (leavingSplash()) {
        fadeOut(tween(MOTION_MEDIUM_MS))
    } else {
        slideOutOfContainer(SlideDirection.Start, tween(MOTION_MEDIUM_MS)) { it / 4 } + fadeOut(tween(MOTION_MEDIUM_MS))
    }

private fun NavTransitionScope.backEnter(): EnterTransition =
    slideIntoContainer(SlideDirection.End, tween(MOTION_MEDIUM_MS)) { it / 4 } + fadeIn(tween(MOTION_MEDIUM_MS))

private fun NavTransitionScope.backExit(): ExitTransition =
    slideOutOfContainer(SlideDirection.End, tween(MOTION_MEDIUM_MS))

private fun NavController.navigateClearingBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
