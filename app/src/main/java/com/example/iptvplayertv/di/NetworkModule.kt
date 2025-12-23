package com.example.iptvplayertv.di

import com.example.iptvplayertv.data.remote.XtreamApi
import com.example.iptvplayertv.data.repository.LiveTvRepository
import com.example.iptvplayertv.data.repository.LiveTvRepositoryImpl
import com.example.iptvplayertv.data.repository.XtreamRepository
import com.example.iptvplayertv.data.repository.XtreamRepositoryImpl
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
            .dns(object : okhttp3.Dns {
                override fun lookup(hostname: String): List<java.net.InetAddress> {
                    return try {
                        okhttp3.Dns.SYSTEM.lookup(hostname)
                    } catch (e: Exception) {
                        // Intentar con DNS de Google como fallback
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
            .baseUrl("http://placeholder.com/") // Base URL temporal
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideXtreamApi(retrofit: Retrofit): XtreamApi {
        return retrofit.create(XtreamApi::class.java)
    }

    @Provides
    @Singleton
    fun provideXtreamRepository(api: XtreamApi): XtreamRepository {
        return XtreamRepositoryImpl(api)
    }

    @Provides
    @Singleton
    fun provideLiveTvRepository(api: XtreamApi): LiveTvRepository {
        return LiveTvRepositoryImpl(api)
    }
}