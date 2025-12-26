package com.rawen.mycutepets.data

import retrofit2.http.GET
import retrofit2.http.Path

interface PetApiService {
    // The Cat API - 10 images random (sans filtre breed pour plus de résultats)
    @GET("v1/images/search?limit=10")
    suspend fun getRandomCats(): List<CatResponse>

    // The Dog API - 10 images random
    @GET("api/breeds/image/random/10")
    suspend fun getRandomDogs(): DogResponse
}

data class CatResponse(
    val id: String,
    val url: String,
    val breeds: List<CatBreed> = emptyList()
)

data class CatBreed(
    val name: String = "Chat inconnu"
)

data class DogResponse(
    val message: List<String>,  // liste d'URLs
    val status: String
)