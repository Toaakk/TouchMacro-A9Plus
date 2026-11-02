package com.touchmacro.taba9.util

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Build
import android.view.WindowManager
import com.touchmacro.taba9.data.model.ScreenOrientation

/**
 * Samsung Galaxy Tab A9+ (11.0" WUXGA 1920x1200) Donanım ve Ekran Optimizatörü
 */
object GalaxyTabOptimizer {
    const val TAB_A9_WIDTH_PX = 1920
    const val TAB_A9_HEIGHT_PX = 1200
    const val MIN_RECOMMENDED_DURATION_MS = 50L

    /**
     * Güncel ekran çözünürlüğünü döndürür
     */
    fun getScreenDimensions(context: Context): Point {
        val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
        val point = Point()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bounds = wm.currentWindowMetrics.bounds
            point.x = bounds.width()
            point.y = bounds.height()
        } else {
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealSize(point)
        }
        return point
    }

    /**
     * Güncel ekran yönü
     */
    fun getCurrentOrientation(context: Context): ScreenOrientation {
        val orientation = context.resources.configuration.orientation
        return if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ScreenOrientation.LANDSCAPE
        } else {
            ScreenOrientation.PORTRAIT
        }
    }

    /**
     * Koordinatların ekran sınırları içinde olduğunu doğrular ve sınırlandırır
     */
    fun clampCoordinates(x: Float, y: Float, screenWidth: Int, screenHeight: Int): Pair<Float, Float> {
        val safeX = x.coerceIn(0f, (screenWidth - 1).toFloat())
        val safeY = y.coerceIn(0f, (screenHeight - 1).toFloat())
        return Pair(safeX, safeY)
    }

    /**
     * Ekran yönü değiştiğinde koordinat adaptasyonu
     * (Yatay 1920x1200 <-> Dikey 1200x1920)
     */
    fun adaptCoordinates(
        x: Float,
        y: Float,
        sourceOrientation: ScreenOrientation,
        targetOrientation: ScreenOrientation
    ): Pair<Float, Float> {
        if (sourceOrientation == targetOrientation || sourceOrientation == ScreenOrientation.AUTO) {
            return Pair(x, y)
        }

        return if (sourceOrientation == ScreenOrientation.LANDSCAPE && targetOrientation == ScreenOrientation.PORTRAIT) {
            // 1920x1200 -> 1200x1920 (Orantısal dönüşüm)
            val mappedX = (x / TAB_A9_WIDTH_PX.toFloat()) * TAB_A9_HEIGHT_PX.toFloat()
            val mappedY = (y / TAB_A9_HEIGHT_PX.toFloat()) * TAB_A9_WIDTH_PX.toFloat()
            Pair(mappedX, mappedY)
        } else if (sourceOrientation == ScreenOrientation.PORTRAIT && targetOrientation == ScreenOrientation.LANDSCAPE) {
            // 1200x1920 -> 1920x1200
            val mappedX = (x / TAB_A9_HEIGHT_PX.toFloat()) * TAB_A9_WIDTH_PX.toFloat()
            val mappedY = (y / TAB_A9_WIDTH_PX.toFloat()) * TAB_A9_HEIGHT_PX.toFloat()
            Pair(mappedX, mappedY)
        } else {
            Pair(x, y)
        }
    }
}
