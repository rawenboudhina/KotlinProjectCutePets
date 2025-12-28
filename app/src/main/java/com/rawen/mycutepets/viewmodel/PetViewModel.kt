package com.rawen.mycutepets.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rawen.mycutepets.data.Breed
import com.rawen.mycutepets.data.PetImage
import com.rawen.mycutepets.data.RetrofitClient
import com.rawen.mycutepets.db.AppDatabase
import com.rawen.mycutepets.db.FavoritePet
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis
import java.io.File
import java.security.MessageDigest
import okhttp3.OkHttpClient
import okhttp3.Request

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private var loadJob: Job? = null

    private val _pets = mutableStateOf<List<PetImage>>(emptyList())
    val pets: State<List<PetImage>> = _pets

    private val _breeds = mutableStateOf<List<Breed>>(emptyList())  // List<Breed> pour UI
    val breeds: State<List<Breed>> = _breeds

    val isLoadingBreeds = mutableStateOf(false)

    private val _pagedPets = mutableStateOf<List<PetImage>>(emptyList())
    val pagedPets: State<List<PetImage>> = _pagedPets

    private val _favorites = mutableStateOf<List<PetImage>>(emptyList())
    val favorites: State<List<PetImage>> = _favorites

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isDogMode = mutableStateOf(true) // true = chiens, false = chats
    val isDogMode: State<Boolean> = _isDogMode

    private val _pageSize = mutableStateOf(10)
    val pageSize: State<Int> = _pageSize

    private var currentPage = 0
    private val _pageIndex = mutableStateOf(0)
    val pageIndex: State<Int> = _pageIndex

    private var selectedBreedId: String? = null
    private var selectedBreedName: String = "Toutes les races"
    private val httpClient = OkHttpClient()

    init {
        loadPets()
        loadFavorites()
    }

    fun refreshBreeds() {
        fetchBreeds(_isDogMode.value)
    }

    fun togglePetType() {
        _isDogMode.value = !_isDogMode.value
        loadJob?.cancel()
        _pets.value = emptyList()
        currentPage = 0
        _pageIndex.value = 0
        selectedBreedId = null
        selectedBreedName = "Toutes les races"
        loadPets()
    }

    fun setPetType(isDog: Boolean) {
        _isDogMode.value = isDog
        loadJob?.cancel()
        _pets.value = emptyList()
        currentPage = 0
        _pageIndex.value = 0
        selectedBreedId = null
        selectedBreedName = "Toutes les races"
        loadPets()
    }

    fun fetchBreeds(isDog: Boolean) {
        setPetType(isDog)
        viewModelScope.launch {
            isLoadingBreeds.value = true
            try {
                val breedsList = mutableListOf<Breed>()
                if (isDog) {
                    val resp = RetrofitClient.dogApi.getDogBreeds()
                    val paths = buildList {
                        resp.message.forEach { (breed, subs) ->
                            if (subs.isEmpty()) add(breed)
                            else subs.forEach { sub -> add("$breed/$sub") }
                        }
                    }.sorted()
                    for (path in paths) {
                        val name = path.split("/").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                        var imageUrl: String? = null
                        try {
                            val imgResp = RetrofitClient.dogApi.getDogImagesByBreed(path)
                            imageUrl = imgResp.message.firstOrNull()
                        } catch (_: Exception) {}
                        breedsList.add(Breed(id = path, name = name, imageUrl = imageUrl, isDog = true))
                    }
                } else {
                    val resp = RetrofitClient.catApi.getCatBreeds().sortedBy { it.name }
                    for (item in resp) {
                        var imageUrl: String? = null
                        try {
                            val imgResp = RetrofitClient.catApi.getCatsByBreed(item.id, limit = 1)
                            imageUrl = imgResp.firstOrNull()?.url
                        } catch (_: Exception) {}
                        breedsList.add(Breed(id = item.id, name = item.name, imageUrl = imageUrl, isDog = false))
                    }
                }
                _breeds.value = breedsList
                // Préchargement des images des races
                prefetchImages(_breeds.value.mapNotNull { it.imageUrl })
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingBreeds.value = false
            }
        }
    }


    fun selectBreed(id: String, name: String) {
        selectedBreedId = id
        selectedBreedName = name
        currentPage = 0
        _pageIndex.value = 0
        loadPets()
    }

    fun loadMore() {
        loadPets(append = true)
    }

    fun nextPage() {
        val nextStart = (currentPage + 1) * _pageSize.value
        if (nextStart < _pets.value.size) {
            currentPage += 1
            updatePagedPets()
        } else {
            loadMore()
        }
    }

    fun prevPage() {
        if (currentPage > 0) {
            currentPage -= 1
            updatePagedPets()
        }
    }

    fun hasNextPage(): Boolean {
        val nextStart = (currentPage + 1) * _pageSize.value
        return nextStart < _pets.value.size
    }

    fun hasPrevPage(): Boolean {
        return currentPage > 0
    }

    private fun loadPets(append: Boolean = false) {
        val requestMode = _isDogMode.value
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                val newPets = if (requestMode) {
                    val breedId = selectedBreedId
                    if (breedId.isNullOrEmpty()) {
                        val response = RetrofitClient.dogApi.getRandomDogs()
                        var counter = 0
                        response.message.map { url ->
                            PetImage(
                                id = "dog_${counter++}_${currentTimeMillis()}",
                                url = url,
                                breedName = selectedBreedName,
                                isDog = true
                            )
                        }
                    } else {
                        val response = RetrofitClient.dogApi.getDogImagesByBreed(breedId)
                        var counter = 0
                        response.message.map { url ->
                            PetImage(
                                id = "dog_${counter++}_${currentTimeMillis()}",
                                url = url,
                                breedName = selectedBreedName,
                                isDog = true
                            )
                        }
                    }
                } else {
                    val breedId = selectedBreedId
                    if (breedId.isNullOrEmpty()) {
                        val response = RetrofitClient.catApi.getRandomCats()
                        response.map { cat ->
                            val breedName = cat.breeds.firstOrNull()?.name ?: selectedBreedName
                            PetImage(
                                id = cat.id,
                                url = cat.url,
                                breedName = breedName,
                                isDog = false
                            )
                        }
                    } else {
                        val response = RetrofitClient.catApi.getCatsByBreed(breedId)
                        response.map { cat ->
                            val breedName = cat.breeds.firstOrNull()?.name ?: selectedBreedName
                            PetImage(
                                id = cat.id,
                                url = cat.url,
                                breedName = breedName,
                                isDog = false
                            )
                        }
                    }
                }

                if (_isDogMode.value != requestMode) return@launch

                val localPets = ensureLocalFiles(newPets)
                if (append) {
                    _pets.value = _pets.value + localPets
                } else {
                    _pets.value = localPets
                    currentPage = 0
                    _pageIndex.value = 0
                }
                prefetchImages(_pets.value.map { it.url })
                updatePagedPets()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleFavorite(pet: PetImage) {
        viewModelScope.launch {
            val fav = FavoritePet(pet.id, pet.url, pet.breedName, pet.isDog)
            if (db.favoriteDao().isFavorite(pet.id)) {
                db.favoriteDao().delete(fav)
            } else {
                db.favoriteDao().insert(fav)
            }
            loadFavorites()
        }
    }

    fun adoptPet(pet: PetImage) {
        viewModelScope.launch {
            val fav = FavoritePet(pet.id, pet.url, pet.breedName, pet.isDog)
            if (!db.favoriteDao().isFavorite(pet.id)) {
                db.favoriteDao().insert(fav)
            }
            loadFavorites()
        }
    }

    fun isFavorite(petId: String): Boolean {
        return _favorites.value.any { it.id == petId }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val favList = db.favoriteDao().getAll()
            _favorites.value = favList.map {
                PetImage(
                    id = it.id,
                    url = it.url,
                    breedName = it.breedName,
                    isDog = it.isDog
                )
            }
        }
    }

    private fun updatePagedPets() {
        val start = currentPage * _pageSize.value
        val end = (start + _pageSize.value).coerceAtMost(_pets.value.size)
        _pagedPets.value = if (start in 0 until end) _pets.value.subList(start, end) else emptyList()
        _pageIndex.value = currentPage
    }

    private fun prefetchImages(urls: List<String>) {
        val context = getApplication<Application>()
        val imageLoader = Coil.imageLoader(context)
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .diskCachePolicy(CachePolicy.ENABLED)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .build()
            imageLoader.enqueue(request)
        }
    }

    private suspend fun ensureLocalFiles(pets: List<PetImage>): List<PetImage> {
        return withContext(Dispatchers.IO) {
            val dir = File(getApplication<Application>().filesDir, "images")
            if (!dir.exists()) dir.mkdirs()
            pets.map { pet ->
                val path = downloadImageIfMissing(pet.url, dir)
                pet.copy(localPath = path)
            }
        }
    }

    private fun downloadImageIfMissing(url: String, dir: File): String? {
        return try {
            val name = md5(url) + ".jpg"
            val outFile = File(dir, name)
            if (outFile.exists()) return outFile.absolutePath
            val request = Request.Builder().url(url).build()
            httpClient.newCall(request).execute().use { resp ->
                if (!resp.isSuccessful) return null
                val body = resp.body ?: return null
                outFile.outputStream().use { os ->
                    body.byteStream().copyTo(os)
                }
                outFile.absolutePath
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun md5(s: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(s.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
