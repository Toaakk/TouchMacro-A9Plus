package com.touchmacro.taba9.data.model

import java.io.Serializable

/**
 * Makro Adım Türleri
 */
enum class StepType : Serializable {
    TAP,        // Tek dokunma (Click/Tap)
    LONG_PRESS, // Basılı tutma (Press & Hold)
    RELEASE,    // Bırakma / Serbest bırakma
    SWIPE,      // Kaydırma / Sürükleme (Drag/Swipe)
    WAIT        // Bekleme / Gecikme adımı
}
