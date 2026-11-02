package com.touchmacro.taba9.data.model

import java.io.Serializable

/**
 * Ekran Yönü Türü (Samsung Galaxy Tab A9+)
 */
enum class ScreenOrientation : Serializable {
    LANDSCAPE, // Yatay (Standart tablet yönü: 1920x1200)
    PORTRAIT,  // Dikey (1200x1920)
    AUTO       // Otomatik algılama
}
