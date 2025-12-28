package com.rawen.mycutepets.data

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface PetApiService {
    @GET("v1/images/search?limit=10")
    suspend fun getRandomCats(): List<CatResponse>

    @GET("api/breeds/image/random/10")
    suspend fun getRandomDogs(): DogResponse

    @GET("v1/breeds")
    suspend fun getCatBreeds(): List<CatBreedItem>

    @GET("v1/images/search")
    suspend fun getCatsByBreed(@Query("breed_ids") breedId: String, @Query("limit") limit: Int = 10): List<CatResponse>

    @GET("api/breeds/list/all")
    suspend fun getDogBreeds(): DogBreedsResponse

    @GET("api/breed/{breed}/images/random/10")
    suspend fun getDogImagesByBreed(@Path(value = "breed", encoded = true) breedPath: String): DogResponse
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

data class CatBreedItem(
    val id: String,
    val name: String
)

data class DogBreedsResponse(
    val message: Map<String, List<String>>,
    val status: String
)
