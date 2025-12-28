package com.rawen.mycutepets.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "adoption_requests")
data class AdoptionRequest(
    @PrimaryKey val id: String,
    val petId: String,
    val breedName: String,
    val isDog: Boolean,
    val applicantName: String,
    val phone: String,
    val note: String?,
    val createdAt: Long
)
