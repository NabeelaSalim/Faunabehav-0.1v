package com.example.faunabahav.ui.screens.signup

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.faunabahav.ui.components.EmailTextField
import com.example.faunabahav.ui.components.PasswordTextField
import com.example.faunabahav.ui.components.PrimaryButton
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.theme.DangerRed

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private const val MIN_PASSWORD_LENGTH = 6

@Composable
fun SignUpCard(
    submitState: SubmitState<*>,
    onSignUp: (email: String, password: String, displayName: String, rememberMe: Boolean) -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var touched by remember { mutableStateOf(false) }

    val isSubmitting = submitState is SubmitState.Submitting
    val nameError = if (touched && fullName.isBlank()) "Full name is required" else null
    val emailError = when {
        !touched -> null
        email.isBlank() -> "Email is required"
        !EMAIL_REGEX.matches(email) -> "Enter a valid email address"
        else -> null
    }
    val passwordError = when {
        !touched -> null
        password.isEmpty() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "Password must be at least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }
    val confirmPasswordError = if (touched && confirmPassword != password) "Passwords do not match" else null

    fun submit() {
        touched = true
        val valid = fullName.isNotBlank() && EMAIL_REGEX.matches(email) &&
            password.length >= MIN_PASSWORD_LENGTH && confirmPassword == password
        if (valid) {
            onSignUp(email, password, fullName, false)
        }
    }

    Card(
        modifier = modifier.widthIn(max = 420.dp),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(Modifier.fillMaxWidth().padding(32.dp)) {
            Text("Create Account", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(4.dp))
            Text(
                "Sign up to get started with FaunaBehav",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full name") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                isError = nameError != null,
                supportingText = nameError?.let { { Text(it) } },
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
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
                imeAction = ImeAction.Next,
            )
            Spacer(Modifier.height(12.dp))
            PasswordTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = "Confirm password",
                errorText = confirmPasswordError,
                imeAction = ImeAction.Done,
            )

            Spacer(Modifier.height(16.dp))

            if (submitState is SubmitState.Error) {
                Text(
                    submitState.message,
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }

            PrimaryButton(
                text = "Create Account",
                onClick = ::submit,
                enabled = !isSubmitting,
                loading = isSubmitting,
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Text("Already have an account?", style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onNavigateToLogin) {
                    Text("Sign In")
                }
            }
        }
    }
}
