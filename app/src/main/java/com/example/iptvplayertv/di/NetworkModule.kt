package com.example.iptvplayertv.di

import com.example.iptvplayertv.data.remote.XtreamApi
import com.example.iptvplayertv.data.repository.LiveTvRepository
import com.example.iptvplayertv.data.repository.LiveTvRepositoryImpl
import com.example.iptvplayertv.data.repository.XtreamRepository
import com.example.iptvplayertv.data.repository.XtreamRepositoryOptimized
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            // ✅ Optimización: Pool de conexiones más eficiente
            .connectionPool(
                okhttp3.ConnectionPool(
                    maxIdleConnections = 5,
                    keepAliveDuration = 5,
                    timeUnit = TimeUnit.MINUTES
                )
            )
            .dns(object : okhttp3.Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> {
                    return try {
                        okhttp3.Dns.SYSTEM.lookup(hostname)
                    } catch (e: Exception) {
                        java.net.InetAddress.getAllByName(hostname).toList()
                    }
                }
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://placeholder.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideXtreamApi(retrofit: Retrofit): XtreamApi {
        return retrofit.create(XtreamApi::class.java)
    }

    /**
     * ✅ IMPORTANTE: Proveer la versión optimizada
     */
    @Provides
    @Singleton
    fun provideXtreamRepository(api: XtreamApi): XtreamRepository {
        return XtreamRepositoryOptimized(api)
    }

    /**
     * ✅ También proveer como XtreamRepositoryOptimized para inyección directa
     */
    @Provides
    @Singleton
    fun provideXtreamRepositoryOptimized(api: XtreamApi): XtreamRepositoryOptimized {
        return XtreamRepositoryOptimized(api)
    }

    @Provides
    @Singleton
    fun provideLiveTvRepository(api: XtreamApi): LiveTvRepository {
        return LiveTvRepositoryImpl(api)
    }
}