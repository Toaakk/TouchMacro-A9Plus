package com.touchmacro.taba9.data.model

import java.io.Serializable

/**
 * Tek bir makro adımını temsil eden veri sınıfı
 * (Dokun, Basılı Tut, Bırak, Kaydır, Bekle)
 */
data class MacroStep(
    val id: String = java.util.UUID.randomUUID().toString(),
    val order: Int = 0,
    val type: StepType = StepType.TAP,
    val label: String = "Adım",
    
    // Başlangıç Koordinatları (Samsung Galaxy Tab A9+ 1920x1200 uzayı)
    val x: Float = 0f,
    val y: Float = 0f,
    
    // Bitiş Koordinatları (Kaydırma/Swipe adımları için)
    val endX: Float = 0f,
    val endY: Float = 0f,
    
    // Eylemin süresi (ms) - Android dokunma süresi (Asgari 50ms)
    val durationMs: Long = 100L,
    
    // Bu adımdan sonra beklenecek ek süre (ms)
    val delayAfterMs: Long = 300L,
    
    // Adımın kaydedildiği anki ekran yönü
    val orientation: ScreenOrientation = ScreenOrientation.LANDSCAPE
) : Serializable {

    init {
        // Android dispatchGesture minimum süre garantisi kontrolü
        require(durationMs >= MIN_GESTURE_DURATION_MS) {
            "Dokunma süresi en az $MIN_GESTURE_DURATION_MS ms olmalıdır."
        }
    }

    companion object {
        const val MIN_GESTURE_DURATION_MS = 50L
        const val DEFAULT_TAP_DURATION_MS = 100L
        const val DEFAULT_LONG_PRESS_MS = 800L
        const val DEFAULT_SWIPE_MS = 400L
        const val DEFAULT_DELAY_MS = 250L

        fun createTap(x: Float, y: Float, order: Int, label: String = "Dokun"): MacroStep {
            return MacroStep(
                order = order,
                type = StepType.TAP,
                label = label,
                x = x,
                y = y,
                durationMs = DEFAULT_TAP_DURATION_MS,
                delayAfterMs = DEFAULT_DELAY_MS
            )
        }

        fun createLongPress(x: Float, y: Float, order: Int, duration: Long = DEFAULT_LONG_PRESS_MS): MacroStep {
            return MacroStep(
                order = order,
                type = StepType.LONG_PRESS,
                label = "Basılı Tut ($duration ms)",
                x = x,
                y = y,
                durationMs = duration.coerceAtLeast(MIN_GESTURE_DURATION_MS),
                delayAfterMs = DEFAULT_DELAY_MS
            )
        }

        fun createSwipe(startX: Float, startY: Float, endX: Float, endY: Float, order: Int, duration: Long = DEFAULT_SWIPE_MS): MacroStep {
            return MacroStep(
                order = order,
                type = StepType.SWIPE,
                label = "Kaydır",
                x = startX,
                y = startY,
                endX = endX,
                endY = endY,
                durationMs = duration.coerceAtLeast(MIN_GESTURE_DURATION_MS),
                delayAfterMs = DEFAULT_DELAY_MS
            )
        }

        fun createWait(delayMs: Long, order: Int): MacroStep {
            return MacroStep(
                order = order,
                type = StepType.WAIT,
                label = "Bekle ($delayMs ms)",
                x = 0f,
                y = 0f,
                durationMs = MIN_GESTURE_DURATION_MS,
                delayAfterMs = delayMs
            )
        }
    }
}
