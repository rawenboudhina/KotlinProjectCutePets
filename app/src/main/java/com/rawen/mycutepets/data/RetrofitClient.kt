package com.rawen.mycutepets.data

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val CAT_BASE_URL = "https://api.thecatapi.com/"
    private const val DOG_BASE_URL = "https://dog.ceo/"

    private val catClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request().newBuilder()
                .addHeader("x-api-key", "DEMO-API-KEY")  // ← Clé demo officielle qui marche pour tests !
                .build()
            chain.proceed(request)
        }
        .build()

    val catApi: PetApiService = Retrofit.Builder()
        .baseUrl(CAT_BASE_URL)
        .client(catClient)  // ← Utilise le client avec header
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PetApiService::class.java)

    val dogApi: PetApiService = Retrofit.Builder()
        .baseUrl(DOG_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PetApiService::class.java)
}