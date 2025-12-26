package com.rawen.mycutepets.viewmodel

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rawen.mycutepets.data.PetImage
import com.rawen.mycutepets.db.AppDatabase
import com.rawen.mycutepets.db.FavoritePet
import kotlinx.coroutines.launch
import java.lang.System.currentTimeMillis

class PetViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _pets = mutableStateOf<List<PetImage>>(emptyList())
    val pets: State<List<PetImage>> = _pets

    private val _favorites = mutableStateOf<List<PetImage>>(emptyList())
    val favorites: State<List<PetImage>> = _favorites

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _isDogMode = mutableStateOf(true) // true = chiens, false = chats
    val isDogMode: State<Boolean> = _isDogMode

    init {
        loadPets()
        loadFavorites()
    }

    fun togglePetType() {
        _isDogMode.value = !_isDogMode.value
        _pets.value = emptyList()  // ← Cette ligne vide la liste actuelle
        loadPets()                 // Recharge depuis zéro avec le nouveau type
    }

    fun loadMore() {
        loadPets(append = true)
    }

    private fun loadPets(append: Boolean = false) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // On lit la valeur ACTUELLE à l'intérieur de la coroutine
                val currentIsDogMode = _isDogMode.value

                val newPets = if (currentIsDogMode) {
                    // CHIENS
                    val response = RetrofitClient.dogApi.getRandomDogs()
                    var counter = 0
                    response.message.map { url ->
                        PetImage(
                            id = "dog_${counter++}_${currentTimeMillis()}",
                            url = url,
                            breedName = "Chien adorable",
                            isDog = true
                        )
                    }
                } else {
                    // CHATS
                    val response = RetrofitClient.catApi.getRandomCats()
                    response.map { cat ->
                        val breedName = cat.breeds.firstOrNull()?.name ?: "Chat adorable 😻"
                        PetImage(
                            id = cat.id,
                            url = cat.url,
                            breedName = breedName,
                            isDog = false
                        )
                    }
                }

                if (append) {
                    _pets.value = _pets.value + newPets
                } else {
                    _pets.value = newPets
                }
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

    fun isFavorite(petId: String): Boolean {
        return _favorites.value.any { it.id == petId }
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            val favList = db.favoriteDao().getAll()
            _favorites.value = favList.map {
                PetImage(it.id, it.url, it.breedName, it.isDog)
            }
        }
    }
}