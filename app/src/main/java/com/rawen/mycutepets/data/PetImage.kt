// File: data/PetImage.kt
package com.rawen.mycutepets.data

data class PetImage(
    val id: String,                  // ID unique de l'image (obligatoire)
    val url: String,                 // URL directe de l'image
    val width: Int? = null,          // Optionnel : dimensions pour Coil ou affichage
    val height: Int? = null,
    val breedId: String? = null,     // ID de la race (utile pour recharger ou filtrer)
    val breedName: String = "Inconnu", // Nom affiché de la race
    val isDog: Boolean = false,       // true = chien, false = chat
    val localPath: String? = null
)
