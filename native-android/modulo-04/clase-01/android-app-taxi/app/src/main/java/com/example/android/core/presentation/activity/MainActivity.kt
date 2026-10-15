package com.example.android.core.presentation.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.android.commons.presentation.ToastHost
import com.example.android.commons.presentation.ToastMessage
import com.example.android.commons.presentation.ToastType
import com.example.android.features.home.presentation.HomeRoute
import com.example.android.features.menu.presentation.MenuRoute
import com.example.android.features.signin.presentation.SignInGenerateOtpRoute
import com.example.android.features.signin.presentation.SignInValidateOtpRoute
import com.example.android.features.signup.presentation.SignUpStep1Route
import com.example.android.features.signup.presentation.SignUpStep2Route
import com.example.android.features.splash.presentation.SplashRoute
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant

sealed class Route(val path: String) {
    data object Splash : Route("splash")

    data object AuthGraph : Route("auth_graph")
    data object SignInGenerate : Route("sign_in/generate")
    data object SignInValidate : Route("sign_in/validate/{phone}/{expiresAt}") {
        const val KEY_PHONE = "phone"
        const val KEY_EXPIRES_AT = "expiresAt"
        fun build(phone: String, expiresAtUtcMillis: Long) = "sign_in/validate/$phone/$expiresAtUtcMillis"
    }

    data object SignUpGraph : Route("sign_up_graph")
    data object SignUpStep1 : Route("sign_up/step1")
    data object SignUpStep2 : Route("sign_up/step2")

    data object Home : Route("home")
    data object Menu : Route("menu")
}

private const val DEEPLINK_HOME = "app-taxi://passenger/home"
private const val SESSION_EXPIRED_MESSAGE = "Tu sesión expiró. Inicia sesión nuevamente"
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

        setContent { AppRoot() }
    }
}

@Composable
private fun AppRoot(vm: MainViewModel = hiltViewModel()) {
    val nav = rememberNavController()
    var sessionExpired by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        vm.sessionExpired.collectLatest {
            nav.navigateClearingBackStack(Route.AuthGraph.path)
            sessionExpired = true
            delay(SESSION_EXPIRED_TOAST_MS)
            sessionExpired = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = nav, startDestination = Route.Splash.path) {

            composable(Route.Splash.path) {
                SplashRoute(
                    onGoSignIn = { nav.navigateClearingBackStack(Route.AuthGraph.path) },
                    onGoSignUp = { nav.navigateClearingBackStack(Route.SignUpGraph.path) },
                    onGoHome = { nav.navigateClearingBackStack(Route.Home.path) }
                )
            }

            navigation(route = Route.AuthGraph.path, startDestination = Route.SignInGenerate.path) {

                composable(Route.SignInGenerate.path) {
                    SignInGenerateOtpRoute(
                        onGoValidate = { phone, expiresAtIso ->
                            val expiresAtUtcMillis = runCatching { Instant.parse(expiresAtIso).toEpochMilli() }
                                .getOrDefault(0L)
                            nav.navigate(Route.SignInValidate.build(phone, expiresAtUtcMillis))
                        }
                    )
                }

                composable(
                    route = Route.SignInValidate.path,
                    arguments = listOf(
                        navArgument(Route.SignInValidate.KEY_PHONE) { type = NavType.StringType },
                        navArgument(Route.SignInValidate.KEY_EXPIRES_AT) { type = NavType.LongType }
                    )
                ) { entry ->
                    SignInValidateOtpRoute(
                        phone = entry.arguments?.getString(Route.SignInValidate.KEY_PHONE).orEmpty(),
                        expiresAtUtcMillis = entry.arguments?.getLong(Route.SignInValidate.KEY_EXPIRES_AT) ?: 0L,
                        onGoHome = { nav.navigateClearingBackStack(Route.Home.path) },
                        onGoSignUp = { nav.navigateClearingBackStack(Route.SignUpGraph.path) }
                    )
                }
            }

            navigation(route = Route.SignUpGraph.path, startDestination = Route.SignUpStep1.path) {

                composable(Route.SignUpStep1.path) {
                    SignUpStep1Route(onNext = { nav.navigate(Route.SignUpStep2.path) })
                }

                composable(Route.SignUpStep2.path) {
                    SignUpStep2Route(onFinish = { nav.navigateClearingBackStack(Route.Home.path) })
                }
            }

            composable(Route.Home.path) {
                HomeRoute(
                    onOpenMenu = { nav.navigate(Route.Menu.path) { launchSingleTop = true } },
                    onLoggedOut = { nav.navigateClearingBackStack(Route.AuthGraph.path) }
                )
            }

            composable(Route.Menu.path) {
                MenuRoute(
                    onBack = { nav.popBackStack() },
                    onMenuClick = { menu -> if (menu.deeplink == DEEPLINK_HOME) nav.popBackStack() }
                )
            }
        }

        ToastHost(
            toast = if (sessionExpired) ToastMessage(ToastType.Error, SESSION_EXPIRED_MESSAGE) else null,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

private fun NavController.navigateClearingBackStack(route: String) {
    navigate(route) {
        popUpTo(graph.id) { inclusive = true }
        launchSingleTop = true
    }
}
