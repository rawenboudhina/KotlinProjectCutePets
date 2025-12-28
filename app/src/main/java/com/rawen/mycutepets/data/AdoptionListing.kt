package com.rawen.mycutepets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adoption_listings")
data class AdoptionListing(
    @PrimaryKey val id: String,
    val title: String,
    val breedName: String,
    val isDog: Boolean,
    val imageUrl: String?,
    val createdAt: Long
)
