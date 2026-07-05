@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.faunabahav.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.DragEvent
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get

private fun readFile(file: File, onResult: (PickedFile?) -> Unit) {
    val reader = FileReader()
    reader.onload = {
        val arrayBuffer = reader.result as ArrayBuffer
        val typedArray = Int8Array(arrayBuffer)
        val bytes = ByteArray(typedArray.length) { i -> typedArray[i] }
        onResult(PickedFile(bytes, file.name, file.type))
    }
    reader.readAsArrayBuffer(file)
}

/**
 * Compose for Web/Wasm renders the whole UI onto a single `<canvas>`, so there's no real per-
 * composable DOM node a browser "drop" event can target — the drop-zone drawn on screen has no
 * corresponding HTML element. The closest honest approximation is a document-wide drag/drop
 * listener: while this composable (the Upload screen) is on screen, dropping a file anywhere in
 * the browser window is accepted, since the upload flow is the only drop target that makes sense
 * on this screen anyway.
 */
@Composable
actual fun rememberFilePicker(onResult: (PickedFile?) -> Unit): FilePickerLauncher {
    val input = remember {
        (document.createElement("input") as HTMLInputElement).apply {
            type = "file"
            accept = "image/*,video/*"
            style.display = "none"
        }
    }

    DisposableEffect(input) {
        val changeListener: (Event) -> Unit = {
            val file: File? = input.files?.get(0)
            input.value = ""
            if (file == null) onResult(null) else readFile(file, onResult)
        }
        val dragOverListener: (Event) -> Unit = { it.preventDefault() }
        val dropListener: (Event) -> Unit = { event ->
            event.preventDefault()
            val file = (event as? DragEvent)?.dataTransfer?.files?.get(0)
            if (file != null) readFile(file, onResult)
        }

        input.addEventListener("change", changeListener)
        document.body?.appendChild(input)
        document.addEventListener("dragover", dragOverListener)
        document.addEventListener("drop", dropListener)
        onDispose {
            input.removeEventListener("change", changeListener)
            input.parentNode?.removeChild(input)
            document.removeEventListener("dragover", dragOverListener)
            document.removeEventListener("drop", dropListener)
        }
    }

    return remember(input) { FilePickerLauncher { input.click() } }
}