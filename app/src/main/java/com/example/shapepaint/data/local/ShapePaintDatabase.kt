package com.example.shapepaint.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ProjectEntity::class, ShapeEntity::class, StrokeEntity::class],
    version = 3,
    exportSchema = false
)
abstract class ShapePaintDatabase : RoomDatabase() {
    abstract fun projectDao(): ProjectDao

    companion object {
        @Volatile
        private var INSTANCE: ShapePaintDatabase? = null

        fun getInstance(context: Context): ShapePaintDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    ShapePaintDatabase::class.java,
                    "shape_paint.db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }
}
