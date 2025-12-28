package com.rawen.mycutepets.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rawen.mycutepets.data.AdoptionListing

@Dao
interface AdoptionListingDao {
    @Query("SELECT * FROM adoption_listings ORDER BY createdAt DESC")
    suspend fun getAll(): List<AdoptionListing>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(listing: AdoptionListing)

    @Delete
    suspend fun delete(listing: AdoptionListing)
}
