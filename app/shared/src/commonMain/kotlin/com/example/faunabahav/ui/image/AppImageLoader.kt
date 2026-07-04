package com.example.faunabahav.ui.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.annotation.ExperimentalCoilApi
import coil3.network.ktor3.KtorNetworkFetcherFactory
import io.ktor.client.HttpClient

/**
 * Coil3's JVM/Android targets can auto-discover the Ktor fetcher via ServiceLoader, but
 * Kotlin/JS and Wasm have no ServiceLoader — so the fetcher is wired up explicitly here to work
 * identically on every target, reusing the app's existing HttpClient instead of creating a
 * second one just for images.
 */
@OptIn(ExperimentalCoilApi::class)
class AppImageLoaderFactory(private val httpClient: HttpClient) : SingletonImageLoader.Factory {
    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader.Builder(context)
            .components {
                add(KtorNetworkFetcherFactory(httpClient))
            }
            .build()
}
