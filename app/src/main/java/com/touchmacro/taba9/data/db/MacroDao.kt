package com.touchmacro.taba9.data.db

import androidx.room.*
import com.touchmacro.taba9.data.model.MacroEntity
import kotlinx.coroutines.flow.Flow

/**
 * Room DAO - Makro veri erişim nesnesi
 */
@Dao
interface MacroDao {

    @Query("SELECT * FROM macros ORDER BY lastUsedAt DESC")
    fun getAllMacros(): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE gameProfile = :profile ORDER BY lastUsedAt DESC")
    fun getMacrosByProfile(profile: String): Flow<List<MacroEntity>>

    @Query("SELECT * FROM macros WHERE id = :id LIMIT 1")
    suspend fun getMacroById(id: Long): MacroEntity?

    @Query("SELECT DISTINCT gameProfile FROM macros WHERE gameProfile != ''")
    fun getAllGameProfiles(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMacro(macro: MacroEntity): Long

    @Update
    suspend fun updateMacro(macro: MacroEntity)

    @Delete
    suspend fun deleteMacro(macro: MacroEntity)

    @Query("DELETE FROM macros WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE macros SET lastUsedAt = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: Long, timestamp: Long = System.currentTimeMillis())
}
