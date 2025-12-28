package com.rawen.mycutepets.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rawen.mycutepets.data.Breed
import com.rawen.mycutepets.data.AdoptionRequest
import com.rawen.mycutepets.data.AdoptionListing
import com.rawen.mycutepets.data.PetImage
import com.rawen.mycutepets.data.RetrofitClient
import com.rawen.mycutepets.db.AppDatabase
import com.rawen.mycutepets.db.FavoritePet
import com.rawen.mycutepets.data.BreedEntity // Ajoutez cet import si nécessaire
import coil.Coil
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.lang.System.currentTimeMillis
import java.security.MessageDigest
import okhttp3.OkHttpClient
import okhttp3.Request

class PetViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getDatabase(application)
    private var loadJob: Job? = null
    private val _pets = mutableStateOf<List<PetImage>>(emptyList())
    val pets: State<List<PetImage>> = _pets
    private val _breeds = mutableStateOf<List<Breed>>(emptyList()) // List<Breed> pour UI
    val breeds: State<List<Breed>> = _breeds
    val isLoadingBreeds = mutableStateOf(false)
    private val _pagedPets = mutableStateOf<List<PetImage>>(emptyList())
    val pagedPets: State<List<PetImage>> = _pagedPets
    private val _favorites = mutableStateOf<List<PetImage>>(emptyList())
    val favorites: State<List<PetImage>> = _favorites
    private val _adoptionRequests = mutableStateOf<List<AdoptionRequest>>(emptyList())
    val adoptionRequests: State<List<AdoptionRequest>> = _adoptionRequests
    private val _adoptionListings = mutableStateOf<List<AdoptionListing>>(emptyList())
    val adoptionListings: State<List<AdoptionListing>> = _adoptionListings
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
    val currentBreedName: String get() = selectedBreedName
    private val httpClient = OkHttpClient()
    private val breedDao = db.breedDao() // Ajouté : pour accéder au DAO des breeds

    init {
        loadPets()
        loadFavorites()
        loadAdoptionRequests()
        loadAdoptionListings()
    }

    fun refreshBreeds() {
        fetchBreeds(_isDogMode.value, forceRefresh = true) // Modifié : force le refresh (clear DB + re-fetch API)
    }

    fun togglePetType() {
        _isDogMode.value = !_isDogMode.value
        loadJob?.cancel()
        _pets.value = emptyList()
        currentPage = 0
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
        loadPets()
    }

    fun fetchBreeds(isDog: Boolean, forceRefresh: Boolean = false) {
        viewModelScope.launch {
            isLoadingBreeds.value = true
            try {
                if (forceRefresh) {
                    breedDao.clearBreeds(isDog)
                }
                var entityBreeds = breedDao.getBreeds(isDog)
                if (entityBreeds.isEmpty()) {
                    entityBreeds = if (isDog) {
                        val resp = RetrofitClient.dogApi.getDogBreeds()
                        val paths = buildList {
                            resp.message.forEach { (breed, subs) ->
                                if (subs.isEmpty()) add(breed)
                                else subs.forEach { sub -> add("$breed/$sub") }
                            }
                        }.sorted()
                        paths.map { path ->
                            val name = path.split("/").joinToString(" ") { it.replaceFirstChar { c -> c.uppercase() } }
                            var imageUrl: String? = null
                            try {
                                val imgResp = RetrofitClient.dogApi.getDogImagesByBreed(path)
                                imageUrl = imgResp.message.firstOrNull()
                            } catch (_: Exception) {}
                            BreedEntity(
                                id = path,
                                name = name,
                                imageUrl = imageUrl,
                                description = null,
                                temperament = null,
                                origin = null,
                                lifeSpan = null,
                                isDog = true
                            )
                        }
                    } else {
                        val resp = RetrofitClient.catApi.getCatBreeds().sortedBy { it.name }
                        resp.map { item ->
                            var imageUrl: String? = null
                            try {
                                val imgResp = RetrofitClient.catApi.getCatsByBreed(item.id, limit = 1)
                                imageUrl = imgResp.firstOrNull()?.url
                            } catch (_: Exception) {}
                            BreedEntity(
                                id = item.id,
                                name = item.name,
                                imageUrl = imageUrl,
                                description = item.description ?: null,
                                temperament = item.temperament ?: null,
                                origin = item.origin ?: null,
                                lifeSpan = item.life_span ?: null,
                                isDog = false
                            )
                        }
                    }
                    breedDao.insertAll(entityBreeds)
                }
                val domainBreeds = entityBreeds.map {
                    Breed(
                        id = it.id,
                        name = it.name,
                        imageUrl = it.imageUrl,
                        description = it.description,
                        isDog = it.isDog
                    )
                }
                val allBreed = Breed.allBreeds(isDog)
                val fullList = listOf(allBreed) + domainBreeds
                val withLocal = ensureLocalBreedImages(fullList)
                _breeds.value = withLocal
                prefetchImages(withLocal.mapNotNull { it.imageUrl })
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoadingBreeds.value = false
            }
        }
    }

    // Nouvelle fonction : Télécharge les images des races localement (similaire à ensureLocalFiles)
    private suspend fun ensureLocalBreedImages(breeds: List<Breed>): List<Breed> {
        return withContext(Dispatchers.IO) {
            val dir = File(getApplication<Application>().filesDir, "breed_images")
            if (!dir.exists()) dir.mkdirs()
            breeds.map { breed ->
                val url = breed.imageUrl ?: return@map breed
                val path = downloadImageIfMissing(url, dir) // Réutilise la fonction existante
                breed.copy(localImagePath = path?.let { "file://$it" }) // Ajoute "file://" pour Coil
            }
        }
    }

    fun selectBreed(id: String, name: String) {
        selectedBreedId = id
        selectedBreedName = name
        _pets.value = emptyList()
        _pagedPets.value = emptyList()
        _isLoading.value = true
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
            val req = AdoptionRequest(
                id = "req_${currentTimeMillis()}_${md5(pet.id)}",
                petId = pet.id,
                breedName = pet.breedName,
                isDog = pet.isDog,
                applicantName = "Anonyme",
                phone = "",
                note = null,
                createdAt = currentTimeMillis()
            )
            db.adoptionRequestDao().insert(req)
            loadAdoptionRequests()
        }
    }

    fun isFavorite(petId: String): Boolean {
        return _favorites.value.any { it.id == petId }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val favList = db.favoriteDao().getAll()
            _favorites.value = favList.map { PetImage(
                id = it.id,
                url = it.url,
                breedName = it.breedName,
                isDog = it.isDog
            ) }
        }
    }

    private fun loadAdoptionRequests() {
        viewModelScope.launch {
            _adoptionRequests.value = db.adoptionRequestDao().getAll()
        }
    }

    private fun loadAdoptionListings() {
        viewModelScope.launch {
            _adoptionListings.value = db.adoptionListingDao().getAll()
        }
    }

    fun submitAdoptionRequest(petId: String, isDog: Boolean, breedName: String, name: String, phone: String, note: String?) {
        viewModelScope.launch {
            val req = AdoptionRequest(
                id = "req_${currentTimeMillis()}_${md5(petId + name)}",
                petId = petId,
                breedName = breedName,
                isDog = isDog,
                applicantName = name,
                phone = phone,
                note = note,
                createdAt = currentTimeMillis()
            )
            db.adoptionRequestDao().insert(req)
            loadAdoptionRequests()
        }
    }

    fun addAdoptionListing(title: String, imageUrl: String?, isDog: Boolean, breedName: String) {
        viewModelScope.launch {
            val listing = AdoptionListing(
                id = "listing_${currentTimeMillis()}_${md5(title)}",
                title = title,
                breedName = breedName,
                isDog = isDog,
                imageUrl = imageUrl,
                createdAt = currentTimeMillis()
            )
            db.adoptionListingDao().insert(listing)
            loadAdoptionListings()
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
        } catch (e: Exception) {
            null
        }
    }

    private fun md5(s: String): String {
        val md = MessageDigest.getInstance("MD5")
        val bytes = md.digest(s.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
