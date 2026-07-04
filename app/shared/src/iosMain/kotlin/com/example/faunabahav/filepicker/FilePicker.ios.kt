package com.example.faunabahav.filepicker

import androidx.compose.runtime.Composable

/** iOS is out of active scope (Android + Web only) — no native picker wired up yet. */
@Composable
actual fun rememberFilePicker(onResult: (PickedFile?) -> Unit): FilePickerLauncher =
    FilePickerLauncher { onResult(null) }