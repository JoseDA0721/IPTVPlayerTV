package com.example.iptvplayertv.di

import com.example.iptvplayertv.data.local.dao.AccountInfoDao
import com.example.iptvplayertv.data.local.dao.ContentCountsDao
import com.example.iptvplayertv.data.remote.XtreamApi
import com.example.iptvplayertv.data.repository.LiveTvRepository
import com.example.iptvplayertv.data.repository.LiveTvRepositoryImpl
import com.example.iptvplayertv.data.repository.XtreamRepository
import com.example.iptvplayertv.data.repository.XtreamRepositoryImp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.Strictness
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Dns
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.InetAddress
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setStrictness(Strictness.LENIENT)// <--- ESTO ES LA CLAVE: Permite JSON mal formado
            .create()
    }
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
            .dns { hostname ->
                try {
                    Dns.SYSTEM.lookup(hostname)
                } catch (_: Exception) {
                    // Intentar con DNS de Google como fallback
                    InetAddress.getAllByName(hostname).toList()
                }
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit { // <--- Agrega parámetro gson
        return Retrofit.Builder()
            .baseUrl("http://placeholder.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson)) // <--- Usa la instancia lenient
            .build()
    }

    @Provides
    @Singleton
    fun provideXtreamApi(retrofit: Retrofit): XtreamApi {
        return retrofit.create(XtreamApi::class.java)
    }

    // ✅ FIX: Inyectar los DAOs correctamente
    @Provides
    @Singleton
    fun provideXtreamRepository(
        api: XtreamApi,
        contentCountsDao: ContentCountsDao,
        accountInfoDao: AccountInfoDao
    ): XtreamRepository {
        return XtreamRepositoryImp(
            api = api,
            contentCountsDao = contentCountsDao,
            accountInfoDao = accountInfoDao
        )
    }

    @Provides
    @Singleton
    fun provideLiveTvRepository(api: XtreamApi): LiveTvRepository {
        return LiveTvRepositoryImpl(api)
    }
}