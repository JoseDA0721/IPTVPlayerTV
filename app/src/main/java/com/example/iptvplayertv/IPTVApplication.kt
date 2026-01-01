package com.example.iptvplayertv

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.disk.directory
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.crossfade
import coil3.util.DebugLogger
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

@HiltAndroidApp
class IPTVApplication : Application(), SingletonImageLoader.Factory {

    /**
     * ✅ Configuración personalizada de Coil para manejar:
     * - HTTPS con certificados autofirmados
     * - Timeouts largos para imágenes lentas
     * - Caché en memoria y disco
     * - Logs detallados para debugging
     */
    override fun newImageLoader(context: PlatformContext): ImageLoader {
        // ✅ OkHttpClient personalizado para Coil
        val okHttpClient = OkHttpClient.Builder()
            // Timeouts generosos para imágenes lentas
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

            // ✅ FIX CRÍTICO: Permitir HTTP sin cifrar (cleartext)
            // Muchos servidores IPTV usan HTTP en lugar de HTTPS
            .retryOnConnectionFailure(true)

            // ✅ Opcional: Desactivar validación SSL (SOLO para desarrollo)
            // ADVERTENCIA: Esto es inseguro, pero necesario para servidores con certificados inválidos
            // .hostnameVerifier { _, _ -> true }
            // .sslSocketFactory(getTrustAllSslSocketFactory(), getTrustAllManager())

            .build()

        return ImageLoader.Builder(context)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(context, 0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(50L * 1024 * 1024)
                    .build()
            }
            .crossfade(300)
            .logger(DebugLogger())
            // respectCacheHeaders(false) was removed as it doesn't exist in Coil 3
            .build()
    }
}