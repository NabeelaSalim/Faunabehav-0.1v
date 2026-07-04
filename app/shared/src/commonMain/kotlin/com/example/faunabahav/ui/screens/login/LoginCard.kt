package com.example.faunabahav.ui.screens.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.components.EmailTextField
import com.example.faunabahav.ui.components.PasswordTextField
import com.example.faunabahav.ui.components.PrimaryButton
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.theme.DangerRed

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")

@Composable
fun LoginCard(
    submitState: SubmitState<*>,
    onSignIn: (email: String, password: String, rememberMe: Boolean) -> Unit,
    onNavigateToSignUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var touched by remember { mutableStateOf(false) }

    val isSubmitting = submitState is SubmitState.Submitting
    val emailError = when {
        !touched -> null
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email) -> "Enter a valid email address"
        else -> null
    }
    val passwordError = if (touched && password.isEmpty()) "Password is required" else null

    fun submit() {
        touched = true
        if (email.isNotBlank() && EMAIL_REGEX.matches(email) && password.isNotBlank()) {
            onSignIn(email, password, rememberMe)
        }
    }

    Card(
        modifier = modifier.widthIn(max = 420.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(32.dp)) {
            Text("Welcome Back", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign in to continue to FaunaBehav",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            EmailTextField(
                value = email,
                onValueChange = { email = it },
                errorText = emailError,
            )
            Spacer(Modifier.height(12.dp))
            PasswordTextField(
                value = password,
                onValueChange = { password = it },
                errorText = passwordError,
                imeAction = ImeAction.Done,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = rememberMe, onCheckedChange = { rememberMe = it })
                    Text("Remember me", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = { /* UI only for now */ }) {
                    Text("Forgot password?")
                }
            }

            Spacer(Modifier.height(8.dp))

            if (submitState is SubmitState.Error) {
                Text(
                    submitState.message,
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            PrimaryButton(
                text = "Sign In",
                onClick = ::submit,
                enabled = !isSubmitting,
                loading = isSubmitting,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Don't have an account?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToSignUp) {
                    Text("Sign Up")
                }
            }
        }
    }
}
