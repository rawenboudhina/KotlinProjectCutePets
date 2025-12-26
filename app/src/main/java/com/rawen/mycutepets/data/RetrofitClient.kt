import com.rawen.mycutepets.data.PetApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val CAT_BASE_URL = "https://api.thecatapi.com/"
    private const val DOG_BASE_URL = "https://dog.ceo/"

    val catApi: PetApiService = Retrofit.Builder()
        .baseUrl(CAT_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PetApiService::class.java)

    val dogApi: PetApiService = Retrofit.Builder()
        .baseUrl(DOG_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(PetApiService::class.java)
}

