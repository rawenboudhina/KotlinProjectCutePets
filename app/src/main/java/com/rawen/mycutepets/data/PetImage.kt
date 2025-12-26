package com.rawen.mycutepets.data


data class PetImage(
    val id: String = "",                    // ID unique de l'image
    val url: String,                        // Lien direct vers la photo
    val breedName: String = "Race inconnue", // Nom de la race (si disponible)
    val isDog: Boolean = false              // true = chien, false = chat
)