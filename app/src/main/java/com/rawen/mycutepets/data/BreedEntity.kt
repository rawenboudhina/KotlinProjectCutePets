package com.rawen.mycutepets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breeds")
data class BreedEntity(
    @PrimaryKey val id: String,
    val name: String,
    val imageUrl: String?,  // URL stockée ici, pas l'image binaire
    val description: String? = null,
    val temperament: String? = null,
    val origin: String? = null,
    val lifeSpan: String? = null,
    val isDog: Boolean
)