package com.rawen.mycutepets.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rawen.mycutepets.data.BreedEntity
import com.rawen.mycutepets.data.AdoptionRequest
import com.rawen.mycutepets.data.AdoptionListing
@Database(entities = [FavoritePet::class, BreedEntity::class, AdoptionRequest::class, AdoptionListing::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun breedDao(): BreedDao
    abstract fun adoptionRequestDao(): AdoptionRequestDao
    abstract fun adoptionListingDao(): AdoptionListingDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `breeds` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `imageUrl` TEXT,
                        `description` TEXT,
                        `temperament` TEXT,
                        `origin` TEXT,
                        `lifeSpan` TEXT,
                        `isDog` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `adoption_requests` (
                        `id` TEXT NOT NULL,
                        `petId` TEXT NOT NULL,
                        `breedName` TEXT NOT NULL,
                        `isDog` INTEGER NOT NULL,
                        `applicantName` TEXT NOT NULL,
                        `phone` TEXT NOT NULL,
                        `note` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `adoption_listings` (
                        `id` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `breedName` TEXT NOT NULL,
                        `isDog` INTEGER NOT NULL,
                        `imageUrl` TEXT,
                        `createdAt` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .fallbackToDestructiveMigration()  // Option pour dev, comme dans PDF page 7
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
