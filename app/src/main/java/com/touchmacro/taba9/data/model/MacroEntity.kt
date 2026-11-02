package com.touchmacro.taba9.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

/**
 * Room Veritabanı Varlığı - Touch Macro Kaydı
 */
@Entity(tableName = "macros")
data class MacroEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val gameProfile: String = "Genel Oyun",
    val description: String = "",
    
    // Tekrar Sayısı: 0 = Kullanıcı durdurana kadar sonsuz tekrar, >0 = Belirli tekrar sayısı
    val loopCount: Int = 0,
    
    // Makro adımları listesi
    val steps: List<MacroStep> = emptyList(),
    
    // Hedef yön (Yatay/Dikey)
    val targetOrientation: ScreenOrientation = ScreenOrientation.LANDSCAPE,
    
    // Ekran yönü değiştiğinde davranış (true = güvenle durdur, false = koordinatları uyarla)
    val stopOnOrientationChange: Boolean = true,
    
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsedAt: Long = System.currentTimeMillis()
) : Serializable {

    val isInfiniteLoop: Boolean
        get() = loopCount == 0

    val totalDurationPerLoopMs: Long
        get() = steps.sumOf { it.durationMs + it.delayAfterMs }
}
