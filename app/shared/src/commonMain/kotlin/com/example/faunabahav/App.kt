package com.example.faunabahav

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import coil3.SingletonImageLoader
import com.example.faunabahav.data.session.rememberSessionStorage
import com.example.faunabahav.data.settings.SettingsKeys
import com.example.faunabahav.data.settings.rememberSettingsStorage
import com.example.faunabahav.di.AppContainer
import com.example.faunabahav.ui.auth.AuthViewModel
import com.example.faunabahav.ui.image.AppImageLoaderFactory
import com.example.faunabahav.ui.navigation.Destination
import com.example.faunabahav.ui.navigation.ResponsiveScaffold
import com.example.faunabahav.ui.navigation.isAdmin
import com.example.faunabahav.ui.screens.alerts.AlertsScreen
import com.example.faunabahav.ui.screens.analytics.AnalyticsScreen
import com.example.faunabahav.ui.screens.dashboard.DashboardScreen
import com.example.faunabahav.ui.screens.devices.DevicesScreen
import com.example.faunabahav.ui.screens.feedback.FeedbackScreen
import com.example.faunabahav.ui.screens.inference.InferenceScreen
import com.example.faunabahav.ui.screens.login.LoginScreen
import com.example.faunabahav.ui.screens.observations.ObservationsScreen
import com.example.faunabahav.ui.screens.settings.SettingsScreen
import com.example.faunabahav.ui.screens.signup.SignUpScreen
import com.example.faunabahav.ui.theme.FaunaBahavTheme

@Composable
fun App() {
    val settingsStorage = rememberSettingsStorage()
    var darkModeEnabled by remember {
        mutableStateOf(settingsStorage.getBoolean(SettingsKeys.DARK_MODE, false))
    }

    FaunaBahavTheme(darkTheme = darkModeEnabled) {
        val sessionStorage = rememberSessionStorage()
        val container = remember(sessionStorage) { AppContainer(sessionStorage = sessionStorage) }
        remember { SingletonImageLoader.setSafe(AppImageLoaderFactory(container.httpClient)) }

        val authViewModel: AuthViewModel = viewModel(
            factory = viewModelFactory { initializer { AuthViewModel(container.authRepository) } },
        )
        val isAuthenticated by authViewModel.isAuthenticated.collectAsStateWithLifecycle()

        when (isAuthenticated) {
            null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            false -> {
                var showSignUp by remember { mutableStateOf(false) }

                if (showSignUp) {
                    SignUpScreen(
                        viewModel = authViewModel,
                        onNavigateToLogin = {
                            authViewModel.resetSubmitState()
                            showSignUp = false
                        },
                    )
                } else {
                    LoginScreen(
                        viewModel = authViewModel,
                        onNavigateToSignUp = {
                            authViewModel.resetSubmitState()
                            showSignUp = true
                        },
                    )
                }
            }

            true -> {
                var destination by remember { mutableStateOf(Destination.Dashboard) }
                val currentUser by authViewModel.currentUser.collectAsStateWithLifecycle()
                if (destination.adminOnly && !currentUser.isAdmin) {
                    destination = Destination.Dashboard
                }

                ResponsiveScaffold(
                    selected = destination,
                    onSelect = { destination = it },
                    user = currentUser,
                    deviceRepository = container.deviceRepository,
                    onLogout = authViewModel::logout,
                ) {
                    when (destination) {
                        Destination.Dashboard -> DashboardScreen(
                            repository = container.dashboardRepository,
                            alertRepository = container.alertRepository,
                            deviceRepository = container.deviceRepository,
                            analyticsRepository = container.analyticsRepository,
                            observationRepository = container.observationRepository,
                            baseUrl = container.baseUrl,
                            onNavigate = { destination = it },
                        )
                        Destination.Observations -> ObservationsScreen(
                            repository = container.observationRepository,
                            deviceRepository = container.deviceRepository,
                            baseUrl = container.baseUrl,
                        )
                        Destination.Alerts -> AlertsScreen(
                            repository = container.alertRepository,
                            deviceRepository = container.deviceRepository,
                            feedbackRepository = container.feedbackRepository,
                            baseUrl = container.baseUrl,
                            currentUser = currentUser,
                            onOpenObservation = { destination = Destination.Observations },
                        )
                        Destination.Analytics -> AnalyticsScreen(
                            repository = container.analyticsRepository,
                            observationRepository = container.observationRepository,
                        )
                        Destination.Devices -> DevicesScreen(container.deviceRepository)
                        Destination.Feedback -> FeedbackScreen(container.feedbackRepository)
                        Destination.Inference -> InferenceScreen(
                            repository = container.observationRepository,
                            dashboardRepository = container.dashboardRepository,
                            alertRepository = container.alertRepository,
                            deviceRepository = container.deviceRepository,
                            baseUrl = container.baseUrl,
                            onViewAllObservations = { destination = Destination.Observations },
                        )
                        Destination.Settings -> SettingsScreen(
                            user = currentUser,
                            onLogout = authViewModel::logout,
                            settingsStorage = settingsStorage,
                            darkModeEnabled = darkModeEnabled,
                            onDarkModeChange = { darkModeEnabled = it },
                        )
                    }
                }
            }
        }
    }
}
