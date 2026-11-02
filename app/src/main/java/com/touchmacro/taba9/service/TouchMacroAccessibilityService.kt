package com.touchmacro.taba9.service

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.widget.Toast
import com.touchmacro.taba9.data.model.MacroEntity
import com.touchmacro.taba9.data.model.ScreenOrientation
import com.touchmacro.taba9.engine.MacroEngine
import com.touchmacro.taba9.util.GalaxyTabOptimizer

/**
 * Touch Macro Gerçek Android Erişilebilirlik Hizmeti
 * - dispatchGesture() ile donanım düzeyinde dokunma/kaydırma simülasyonu
 * - TYPE_ACCESSIBILITY_OVERLAY ile oyun üzerinde yüzen HUD paneli ve dokunma noktası seçici
 * - Ekran kilidi algılayıcı ve güvenli durdurma mekanizması
 * - Ekran yönü değiştiğinde güvenli durdurma
 */
class TouchMacroAccessibilityService : AccessibilityService() {

    private val TAG = "TouchMacroService"
    private val mainHandler = Handler(Looper.getMainLooper())

    lateinit var macroEngine: MacroEngine
        private set

    private var overlayManager: FloatingOverlayManager? = null
    private var lastOrientation: ScreenOrientation = ScreenOrientation.LANDSCAPE

    // Ekran kilitlenmesini dinleyen alıcı
    private val screenLockReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                Log.d(TAG, "Ekran kapandı/kilitlendi! Makro güvenliği devreye giriyor.")
                macroEngine.stopSafety("Ekran kilitlendi")
                mainHandler.post {
                    Toast.makeText(this@TouchMacroAccessibilityService, "Güvenlik: Tablet ekranı kilitlendiği için makro durduruldu.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        macroEngine = MacroEngine { this }
        overlayManager = FloatingOverlayManager(this)

        val filter = IntentFilter(Intent.ACTION_SCREEN_OFF)
        registerReceiver(screenLockReceiver, filter)

        lastOrientation = GalaxyTabOptimizer.getCurrentOrientation(this)
        Log.i(TAG, "TouchMacroAccessibilityService başlatıldı.")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        Log.i(TAG, "Erişilebilirlik hizmeti bağlandı ve dokunma jestleri göndermeye hazır.")
        isServiceRunning = true
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // İsteğe bağlı pencere olayları
    }

    override fun onInterrupt() {
        Log.w(TAG, "Erişilebilirlik hizmeti kesintiye uğradı.")
        macroEngine.stopSafety("Hizmet kesintiye uğradı")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        val currentOrientation = if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            ScreenOrientation.LANDSCAPE
        } else {
            ScreenOrientation.PORTRAIT
        }

        if (currentOrientation != lastOrientation) {
            Log.d(TAG, "Ekran yönü değişti: $lastOrientation -> $currentOrientation")
            lastOrientation = currentOrientation
            macroEngine.onOrientationChanged(currentOrientation)
        }
    }

    fun showFloatingControls(macro: MacroEntity? = null) {
        overlayManager?.showFloatingPanel(macro)
    }

    fun hideFloatingControls() {
        overlayManager?.hideFloatingPanel()
    }

    fun startPointPicker(onPointPicked: (Float, Float) -> Unit) {
        overlayManager?.showPointPicker(onPointPicked)
    }

    override fun onDestroy() {
        super.onDestroy()
        isServiceRunning = false
        try {
            unregisterReceiver(screenLockReceiver)
        } catch (e: Exception) {
            // Zaten kaydı silinmiş olabilir
        }
        macroEngine.release()
        overlayManager?.hideAll()
        instance = null
        Log.i(TAG, "TouchMacroAccessibilityService sonlandırıldı.")
    }

    companion object {
        var instance: TouchMacroAccessibilityService? = null
            private set

        var isServiceRunning: Boolean = false
            private set
    }
}
