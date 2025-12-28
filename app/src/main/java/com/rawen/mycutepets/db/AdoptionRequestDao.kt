package com.rawen.mycutepets.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rawen.mycutepets.data.AdoptionRequest

@Dao
interface AdoptionRequestDao {
    @Query("SELECT * FROM adoption_requests ORDER BY createdAt DESC")
    suspend fun getAll(): List<AdoptionRequest>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(request: AdoptionRequest)

    @Delete
    suspend fun delete(request: AdoptionRequest)
}
