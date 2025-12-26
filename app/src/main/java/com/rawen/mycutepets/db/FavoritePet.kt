package com.rawen.mycutepets.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoritePet(
    @PrimaryKey val id: String,
    val url: String,
    val breedName: String,
    val isDog: Boolean
)