@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.example.faunabahav.filepicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import kotlinx.browser.document
import org.khronos.webgl.ArrayBuffer
import org.khronos.webgl.Int8Array
import org.khronos.webgl.get
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.events.Event
import org.w3c.files.File
import org.w3c.files.FileReader
import org.w3c.files.get

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
        val listener: (Event) -> Unit = {
            val file: File? = input.files?.get(0)
            input.value = ""
            if (file == null) {
                onResult(null)
            } else {
                val reader = FileReader()
                reader.onload = {
                    val arrayBuffer = reader.result as ArrayBuffer
                    val typedArray = Int8Array(arrayBuffer)
                    val bytes = ByteArray(typedArray.length) { i -> typedArray[i] }
                    onResult(PickedFile(bytes, file.name, file.type))
                }
                reader.readAsArrayBuffer(file)
            }
        }
        input.addEventListener("change", listener)
        document.body?.appendChild(input)
        onDispose {
            input.removeEventListener("change", listener)
            input.parentNode?.removeChild(input)
        }
    }

    return remember(input) { FilePickerLauncher { input.click() } }
}