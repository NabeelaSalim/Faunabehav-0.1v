package com.example.faunabahav.ui.screens.feedback

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.faunabahav.data.repository.FeedbackRepository
import com.example.faunabahav.model.Feedback
import com.example.faunabahav.ui.components.PrimaryButton
import com.example.faunabahav.ui.components.UiStateContent
import com.example.faunabahav.ui.navigation.rememberIsWideScreen
import com.example.faunabahav.ui.screens.observations.EmptyState
import com.example.faunabahav.ui.state.SubmitState
import com.example.faunabahav.ui.theme.DangerRed
import com.example.faunabahav.ui.theme.PrimaryGreen

@Composable
fun FeedbackScreen(
    repository: FeedbackRepository,
    modifier: Modifier = Modifier,
    viewModel: FeedbackViewModel = viewModel(
        factory = viewModelFactory { initializer { FeedbackViewModel(repository) } },
    ),
) {
    val listState by viewModel.uiState.collectAsStateWithLifecycle()
    val submitState by viewModel.submitState.collectAsStateWithLifecycle()

    var eventIdText by remember { mutableStateOf("") }
    var userIdText by remember { mutableStateOf("") }
    var correctedBehaviour by remember { mutableStateOf("") }

    LaunchedEffect(submitState) {
        if (submitState is SubmitState.Success) {
            eventIdText = ""
            userIdText = ""
            correctedBehaviour = ""
        }
    }

    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Feedback", style = MaterialTheme.typography.headlineSmall)
        HorizontalDivider(Modifier.padding(vertical = 12.dp))

        FeedbackForm(
            eventIdText = eventIdText,
            onEventIdChange = { eventIdText = it.filter(Char::isDigit) },
            userIdText = userIdText,
            onUserIdChange = { userIdText = it.filter(Char::isDigit) },
            correctedBehaviour = correctedBehaviour,
            onCorrectedBehaviourChange = { correctedBehaviour = it },
            submitState = submitState,
            onSubmit = {
                val eventId = eventIdText.toIntOrNull()
                val userId = userIdText.toIntOrNull()
                if (eventId != null && userId != null && correctedBehaviour.isNotBlank()) {
                    viewModel.submitFeedback(eventId, userId, correctedBehaviour)
                }
            },
        )

        Spacer(Modifier.height(20.dp))
        Text("Past feedback", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        UiStateContent(state = listState, modifier = Modifier.height(400.dp), onRetry = viewModel::load) { feedbackList ->
            if (feedbackList.isEmpty()) {
                EmptyState("No feedback submitted yet")
            } else {
                Column {
                    feedbackList.forEach { feedback -> FeedbackCard(feedback) }
                }
            }
        }
    }
}

@Composable
private fun FeedbackForm(
    eventIdText: String,
    onEventIdChange: (String) -> Unit,
    userIdText: String,
    onUserIdChange: (String) -> Unit,
    correctedBehaviour: String,
    onCorrectedBehaviourChange: (String) -> Unit,
    submitState: SubmitState<Feedback>,
    onSubmit: () -> Unit,
) {
    val isSubmitting = submitState is SubmitState.Submitting
    val canSubmit = eventIdText.toIntOrNull() != null && userIdText.toIntOrNull() != null &&
        correctedBehaviour.isNotBlank()

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp)) {
            Text("Submit corrected feedback", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(
                "Help improve detection accuracy by correcting a logged behaviour.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(16.dp))

            if (rememberIsWideScreen()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = eventIdText,
                        onValueChange = onEventIdChange,
                        label = { Text("Event ID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                    OutlinedTextField(
                        value = userIdText,
                        onValueChange = onUserIdChange,
                        label = { Text("User ID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                    )
                }
            } else {
                OutlinedTextField(
                    value = eventIdText,
                    onValueChange = onEventIdChange,
                    label = { Text("Event ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = userIdText,
                    onValueChange = onUserIdChange,
                    label = { Text("User ID") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                )
            }
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = correctedBehaviour,
                onValueChange = onCorrectedBehaviourChange,
                label = { Text("Corrected behaviour") },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))

            when (submitState) {
                is SubmitState.Error -> Text(
                    submitState.message,
                    color = DangerRed,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                is SubmitState.Success -> Text(
                    "Feedback submitted",
                    color = PrimaryGreen,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp),
                )

                else -> Unit
            }

            PrimaryButton(
                text = "Submit",
                onClick = onSubmit,
                enabled = canSubmit && !isSubmitting,
                loading = isSubmitting,
            )
        }
    }
}

@Composable
private fun FeedbackCard(feedback: Feedback) {
    Card(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(40.dp)
                    .background(PrimaryGreen.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(Icons.Filled.Feedback, contentDescription = null, tint = PrimaryGreen)
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Event #${feedback.eventId} · User #${feedback.userId}", fontWeight = FontWeight.Bold)
                Text(feedback.correctedBehaviour, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}
