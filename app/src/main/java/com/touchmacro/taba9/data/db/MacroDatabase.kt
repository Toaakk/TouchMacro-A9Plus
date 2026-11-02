package com.touchmacro.taba9.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.touchmacro.taba9.data.model.MacroEntity

@Database(entities = [MacroEntity::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class MacroDatabase : RoomDatabase() {

    abstract fun macroDao(): MacroDao

    companion object {
        @Volatile
        private var INSTANCE: MacroDatabase? = null

        fun getInstance(context: Context): MacroDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MacroDatabase::class.java,
                    "touch_macro_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
