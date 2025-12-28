package com.rawen.mycutepets.data

data class Breed(
    val id: String,
    val name: String,
    val imageUrl: String? = null,
    val description: String? = null,
    val temperament: String? = null,
    val origin: String? = null,
    val lifeSpan: String? = null,
    val localImagePath: String? = null,  // Ajouté pour le chemin local (e.g., "file:///path/to/image.jpg")
    val isDog: Boolean = false
) {
    companion object {
        fun allBreeds(isDog: Boolean) = Breed(
            id = "",
            name = "Toutes les races",
            imageUrl = if (isDog) "https://images.dog.ceo/breeds/labrador/n02099712_1222.jpg" else "https://cdn2.thecatapi.com/images/a8n4p600R.jpg",
            description = if (isDog) "Découvrez tous les chiens adorables" else "Découvrez tous les chats adorables",
            temperament = null,
            origin = null,
            lifeSpan = null,
            isDog = isDog
        )
    }
}
