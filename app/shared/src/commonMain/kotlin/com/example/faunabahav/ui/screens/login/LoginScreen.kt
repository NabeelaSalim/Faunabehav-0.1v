package com.example.faunabahav.ui.screens.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.faunabahav.ui.auth.AuthViewModel
import com.example.faunabahav.ui.navigation.rememberIsWideScreen

@Composable
fun LoginScreen(viewModel: AuthViewModel, onNavigateToSignUp: () -> Unit, modifier: Modifier = Modifier) {
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()
    val backgroundColor = MaterialTheme.colorScheme.background

    if (rememberIsWideScreen()) {
        Row(modifier.fillMaxSize()) {
            LoginHeroSection(modifier = Modifier.weight(1f).fillMaxSize())
            Box(
                modifier = Modifier.weight(1f).fillMaxSize().background(backgroundColor),
                contentAlignment = Alignment.Center,
            ) {
                LoginCard(
                    submitState = submitState,
                    onSignIn = viewModel::login,
                    onNavigateToSignUp = onNavigateToSignUp,
                    modifier = Modifier.padding(32.dp),
                )
            }
        }
    } else {
        Column(
            modifier
                .fillMaxSize()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState()),
        ) {
            LoginHeroSection(modifier = Modifier.fillMaxWidth().height(280.dp))
            Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                LoginCard(
                    submitState = submitState,
                    onSignIn = viewModel::login,
                    onNavigateToSignUp = onNavigateToSignUp,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
