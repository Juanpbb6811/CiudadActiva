package com.ciudadactiva.app.data.api

import com.ciudadactiva.app.CiudadActivaApplication
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private var retrofit: Retrofit? = null
    private var lastBaseUrl: String = ""

    private fun getOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor())
            .addInterceptor(logging)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    fun getService(): CiudadActivaApiService {
        val currentBaseUrl = CiudadActivaApplication.sessionManager.getBaseUrl()
        if (retrofit == null || lastBaseUrl != currentBaseUrl) {
            lastBaseUrl = currentBaseUrl
            retrofit = Retrofit.Builder()
                .baseUrl(currentBaseUrl)
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit!!.create(CiudadActivaApiService::class.java)
    }
}
