package com.rawen.mycutepets.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.rawen.mycutepets.data.BreedEntity

@Dao
interface BreedDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(breeds: List<BreedEntity>)

    @Query("SELECT * FROM breeds WHERE isDog = :isDog")
    suspend fun getBreeds(isDog: Boolean): List<BreedEntity>

    @Query("DELETE FROM breeds WHERE isDog = :isDog")
    suspend fun clearBreeds(isDog: Boolean)
}