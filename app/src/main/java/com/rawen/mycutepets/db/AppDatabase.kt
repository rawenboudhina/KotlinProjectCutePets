package com.rawen.mycutepets.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rawen.mycutepets.data.BreedEntity
@Database(entities = [FavoritePet::class, BreedEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun breedDao(): BreedDao

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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()  // Option pour dev, comme dans PDF page 7
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}