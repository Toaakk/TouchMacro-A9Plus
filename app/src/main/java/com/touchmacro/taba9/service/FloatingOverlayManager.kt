package com.touchmacro.taba9.service

import android.content.Context
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import com.touchmacro.taba9.data.model.MacroEntity
import com.touchmacro.taba9.engine.EngineStatus
import kotlinx.coroutines.*

/**
 * TYPE_ACCESSIBILITY_OVERLAY kullanarak ekran üstünde yüzen mini kontrol paneli
 * ve tam ekran şeffaf dokunma noktası yakalayıcı (Point Picker)
 */
class FloatingOverlayManager(private val service: TouchMacroAccessibilityService) {

    private val windowManager = service.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val mainHandler = Handler(Looper.getMainLooper())
    private val overlayScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    private var floatingPanelRoot: View? = null
    private var pickerOverlayRoot: View? = null
    private var activeMacro: MacroEntity? = null

    // UI Referansları
    private var statusBadge: TextView? = null
    private var stepInfoView: TextView? = null
    private var loopInfoView: TextView? = null
    private var btnPlayPause: TextView? = null

    // Panel sürükleme koordinatları
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f

    init {
        // Motor durumunu dinle ve yüzen paneli otomatik güncelle
        overlayScope.launch {
            service.macroEngine.progress.collect { progress ->
                mainHandler.post {
                    updateUiFromProgress(progress.status, progress.currentStepDescription, progress.currentLoop, progress.totalLoops, progress.currentStepIndex, progress.totalSteps, progress.errorMessage)
                }
            }
        }
    }

    /**
     * Yüzen mini HUD kontrol panelini gösterir
     */
    fun showFloatingPanel(macro: MacroEntity? = null) {
        if (macro != null) {
            activeMacro = macro
        }

        if (floatingPanelRoot != null) {
            return
        }

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 40
            y = 120
        }

        // Mini HUD Container
        val card = CardView(service).apply {
            radius = 28f
            cardElevation = 18f
            setCardBackgroundColor(0xEE1E293B.toInt()) // Koyu slate arka plan (#1E293B)
        }

        val container = LinearLayout(service).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(20, 16, 20, 16)
        }

        // 1. Satır: Başlık & Sürükleme Tutamacı & Durum Rozeti
        val titleRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val titleView = TextView(service).apply {
            text = activeMacro?.name ?: "Touch Macro"
            setTextColor(Color.WHITE)
            textSize = 13f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        statusBadge = TextView(service).apply {
            text = "HAZIR"
            textSize = 10f
            setTextColor(0xFF38BDF8.toInt())
            setPadding(10, 4, 10, 4)
            setBackgroundColor(0x3338BDF8)
        }

        titleRow.addView(titleView)
        titleRow.addView(statusBadge)
        container.addView(titleRow)

        // 2. Satır: Döngü ve Adım Bilgisi
        loopInfoView = TextView(service).apply {
            text = "Döngü: 0 / ${if (activeMacro?.isInfiniteLoop == true) "Sonsuz" else activeMacro?.loopCount ?: 0}"
            setTextColor(0xFF94A3B8.toInt())
            textSize = 11f
            setPadding(0, 6, 0, 2)
        }
        container.addView(loopInfoView)

        stepInfoView = TextView(service).apply {
            text = "Bekleniyor..."
            setTextColor(0xFFE2E8F0.toInt())
            textSize = 11f
            setPadding(0, 0, 0, 8)
        }
        container.addView(stepInfoView)

        // 3. Satır: Butonlar (Başlat/Duraklat, Durdur, Nokta Seç, Kapat)
        val buttonRow = LinearLayout(service).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }

        fun createButton(label: String, bgColor: Int, onClick: () -> Unit): TextView {
            return TextView(service).apply {
                text = label
                textSize = 12f
                setTextColor(Color.WHITE)
                setTypeface(null, android.graphics.Typeface.BOLD)
                setPadding(16, 10, 16, 10)
                setBackgroundColor(bgColor)
                gravity = Gravity.CENTER
                setOnClickListener { onClick() }
                val lp = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                lp.setMargins(4, 0, 4, 0)
                layoutParams = lp
            }
        }

        btnPlayPause = createButton("▶ Başlat", 0xFF10B981.toInt()) {
            val status = service.macroEngine.progress.value.status
            if (status == EngineStatus.RUNNING) {
                service.macroEngine.pauseMacro()
            } else if (status == EngineStatus.PAUSED) {
                service.macroEngine.resumeMacro()
            } else {
                activeMacro?.let { macro ->
                    service.macroEngine.startMacro(macro)
                } ?: Toast.makeText(service, "Çalıştırılacak makro seçilmedi", Toast.LENGTH_SHORT).show()
            }
        }

        val btnStop = createButton("⏹ Durdur", 0xFFEF4444.toInt()) {
            service.macroEngine.stopMacro("Kullanıcı tarafından durduruldu")
        }

        val btnPicker = createButton("🎯 Nokta", 0xFF0284C7.toInt()) {
            showPointPicker { x, y ->
                Toast.makeText(service, "Nokta yakalandı: X=${x.toInt()}, Y=${y.toInt()}", Toast.LENGTH_SHORT).show()
            }
        }

        buttonRow.addView(btnPlayPause)
        buttonRow.addView(btnStop)
        buttonRow.addView(btnPicker)
        container.addView(buttonRow)

        card.addView(container)

        // Paneli serbestçe sürükleme dinleyicisi
        card.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = params.x
                    initialY = params.y
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(card, params)
                    true
                }
                else -> false
            }
        }

        floatingPanelRoot = card
        windowManager.addView(card, params)
    }

    private fun updateUiFromProgress(
        status: EngineStatus,
        stepDesc: String,
        currentLoop: Int,
        totalLoops: Int,
        stepIndex: Int,
        totalSteps: Int,
        errorMessage: String?
    ) {
        val badge = statusBadge ?: return

        when (status) {
            EngineStatus.RUNNING -> {
                badge.text = "ÇALIŞIYOR"
                badge.setTextColor(0xFF10B981.toInt())
                btnPlayPause?.text = "⏸ Duraklat"
                btnPlayPause?.setBackgroundColor(0xFFF59E0B.toInt())
            }
            EngineStatus.PAUSED -> {
                badge.text = "DURAKLATILDI"
                badge.setTextColor(0xFFF59E0B.toInt())
                btnPlayPause?.text = "▶ Devam"
                btnPlayPause?.setBackgroundColor(0xFF10B981.toInt())
            }
            EngineStatus.STOPPED -> {
                badge.text = "DURDURULDU"
                badge.setTextColor(0xFFEF4444.toInt())
                btnPlayPause?.text = "▶ Başlat"
                btnPlayPause?.setBackgroundColor(0xFF10B981.toInt())
            }
            EngineStatus.COMPLETED -> {
                badge.text = "TAMAMLANDI"
                badge.setTextColor(0xFF38BDF8.toInt())
                btnPlayPause?.text = "▶ Başlat"
                btnPlayPause?.setBackgroundColor(0xFF10B981.toInt())
            }
            EngineStatus.ERROR -> {
                badge.text = "HATA"
                badge.setTextColor(0xFFEF4444.toInt())
                btnPlayPause?.text = "▶ Yeniden Başlat"
                btnPlayPause?.setBackgroundColor(0xFF10B981.toInt())
                if (!errorMessage.isNullOrEmpty()) {
                    Toast.makeText(service, "Makro Hatası: $errorMessage", Toast.LENGTH_LONG).show()
                }
            }
            EngineStatus.IDLE -> {
                badge.text = "HAZIR"
                badge.setTextColor(0xFF38BDF8.toInt())
                btnPlayPause?.text = "▶ Başlat"
                btnPlayPause?.setBackgroundColor(0xFF10B981.toInt())
            }
        }

        loopInfoView?.text = "Döngü: $currentLoop / ${if (totalLoops == 0) "Sonsuz" else totalLoops.toString()} (Adım: $stepIndex/$totalSteps)"
        stepInfoView?.text = stepDesc
    }

    /**
     * Oyun açıkken ekranda bir noktaya dokunulduğunda koordinatını yakalayan şeffaf katman
     */
    fun showPointPicker(onPointPicked: (Float, Float) -> Unit) {
        if (pickerOverlayRoot != null) return

        val overlayType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            overlayType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )

        val pickerFrame = FrameLayout(service).apply {
            setBackgroundColor(0x33000000)
        }

        val hintText = TextView(service).apply {
            text = "Ekranda dokunmak istediğiniz noktaya dokunun\n(X / Y Koordinatı otomatik yakalanır)"
            setTextColor(Color.WHITE)
            textSize = 15f
            gravity = Gravity.CENTER
            setBackgroundColor(0xCC000000.toInt())
            setPadding(32, 20, 32, 20)
            val lp = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.CENTER_HORIZONTAL
            ).apply {
                topMargin = 100
            }
            layoutParams = lp
        }

        pickerFrame.addView(hintText)

        pickerFrame.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                val pickedX = event.rawX
                val pickedY = event.rawY
                onPointPicked(pickedX, pickedY)
                hidePointPicker()
                true
            } else {
                false
            }
        }

        pickerOverlayRoot = pickerFrame
        windowManager.addView(pickerFrame, params)
    }

    fun hidePointPicker() {
        pickerOverlayRoot?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
            pickerOverlayRoot = null
        }
    }

    fun hideFloatingPanel() {
        floatingPanelRoot?.let {
            try {
                windowManager.removeView(it)
            } catch (e: Exception) {}
            floatingPanelRoot = null
        }
    }

    fun hideAll() {
        hideFloatingPanel()
        hidePointPicker()
        overlayScope.cancel()
    }
}
