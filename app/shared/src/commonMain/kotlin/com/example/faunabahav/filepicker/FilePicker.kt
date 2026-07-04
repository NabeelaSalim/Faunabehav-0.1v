package com.example.faunabahav.filepicker

import androidx.compose.runtime.Composable

fun interface FilePickerLauncher {
    fun launch()
}

/**
 * Launches the platform's native file/media picker. [onResult] is invoked once with the picked
 * file, or `null` if the user dismissed the picker without choosing anything.
 */
@Composable
expect fun rememberFilePicker(onResult: (PickedFile?) -> Unit): FilePickerLauncher