package com.touchmacro.taba9.engine

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.util.Log
import com.touchmacro.taba9.data.model.MacroEntity
import com.touchmacro.taba9.data.model.MacroStep
import com.touchmacro.taba9.data.model.ScreenOrientation
import com.touchmacro.taba9.data.model.StepType
import com.touchmacro.taba9.service.TouchMacroAccessibilityService
import com.touchmacro.taba9.util.GalaxyTabOptimizer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Makro Motorunun Çalışma Durumları
 */
enum class EngineStatus {
    IDLE,       // Boşta / Hazır
    RUNNING,    // Çalışıyor
    PAUSED,     // Duraklatıldı
    COMPLETED,  // Başarıyla tamamlandı
    STOPPED,    // Kullanıcı veya sistem tarafından durduruldu
    ERROR       // Hata oluştu
}

/**
 * UI ve Yüzen Panel için anlık ilerleme durumu
 */
data class MacroExecutionProgress(
    val status: EngineStatus = EngineStatus.IDLE,
    val macroName: String = "",
    val currentLoop: Int = 0,
    val totalLoops: Int = 0, // 0 = Sonsuz
    val currentStepIndex: Int = 0,
    val totalSteps: Int = 0,
    val currentStepDescription: String = "",
    val errorMessage: String? = null
)

/**
 * Kotlin Coroutines tabanlı Dokunma Makro Motoru
 * - dispatchGesture() tamamlama callback senkronizasyonu
 * - 50ms asgari süre ve jitter koruması
 * - Çakışan dokunma engelleme (Mutex)
 * - Ekran kilidi ve yön değişikliğinde güvenli durdurma
 * - Hataları kullanıcıya raporlama
 */
class MacroEngine(
    private val serviceProvider: () -> TouchMacroAccessibilityService?
) {
    private val TAG = "MacroEngine"
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var executionJob: Job? = null
    private val gestureMutex = Mutex() // Aynı anda çakışan dokunma gönderilmesini önler

    private val _progress = MutableStateFlow(MacroExecutionProgress())
    val progress: StateFlow<MacroExecutionProgress> = _progress.asStateFlow()

    private var activeMacro: MacroEntity? = null
    @Volatile private var isPaused = false
    private val pauseLock = Object()

    /**
     * Makroyu başlatır
     */
    fun startMacro(macro: MacroEntity) {
        if (_progress.value.status == EngineStatus.RUNNING) {
            Log.w(TAG, "Bir makro zaten çalışıyor, önce durdurun.")
            return
        }

        activeMacro = macro
        isPaused = false

        executionJob?.cancel()
        executionJob = scope.launch {
            runMacroLoop(macro)
        }
    }

    /**
     * Makroyu duraklatır
     */
    fun pauseMacro() {
        if (_progress.value.status == EngineStatus.RUNNING) {
            isPaused = true
            _progress.value = _progress.value.copy(
                status = EngineStatus.PAUSED,
                currentStepDescription = "Makro duraklatıldı"
            )
            Log.d(TAG, "Makro duraklatıldı.")
        }
    }

    /**
     * Duraklatılan makroyu sürdürür
     */
    fun resumeMacro() {
        if (isPaused) {
            isPaused = false
            synchronized(pauseLock) {
                pauseLock.notifyAll()
            }
            _progress.value = _progress.value.copy(
                status = EngineStatus.RUNNING,
                currentStepDescription = "Makro devam ettiriliyor"
            )
            Log.d(TAG, "Makro devam ettiriliyor.")
        }
    }

    /**
     * Makroyu güvenli şekilde tamamen durdurur
     */
    fun stopMacro(reason: String = "Kullanıcı tarafından durduruldu") {
        isPaused = false
        synchronized(pauseLock) {
            pauseLock.notifyAll()
        }
        executionJob?.cancel()
        executionJob = null
        _progress.value = _progress.value.copy(
            status = EngineStatus.STOPPED,
            currentStepDescription = reason,
            errorMessage = null
        )
        Log.d(TAG, "Makro sonlandırıldı: $reason")
    }

    /**
     * Ekran kapandığında veya yön değiştiğinde çağrılan güvenlik sonlandırıcısı
     */
    fun stopSafety(reason: String) {
        stopMacro("Güvenlik İptali: $reason")
    }

    /**
     * Ekran yönü değiştiğinde motoru bilgilendirir
     */
    fun onOrientationChanged(newOrientation: ScreenOrientation) {
        val macro = activeMacro
        if (_progress.value.status == EngineStatus.RUNNING && macro != null) {
            if (macro.stopOnOrientationChange) {
                stopSafety("Ekran yönü değiştiği için güvenlik gereği durduruldu")
            }
        }
    }

    private suspend fun runMacroLoop(macro: MacroEntity) {
        val totalLoops = macro.loopCount // 0 = Sonsuz tekrar
        var currentLoop = 0

        _progress.value = MacroExecutionProgress(
            status = EngineStatus.RUNNING,
            macroName = macro.name,
            currentLoop = 1,
            totalLoops = totalLoops,
            currentStepIndex = 0,
            totalSteps = macro.steps.size,
            currentStepDescription = "Makro başlatılıyor..."
        )

        try {
            while (currentCoroutineContext().isActive && (totalLoops == 0 || currentLoop < totalLoops)) {
                currentLoop++
                _progress.value = _progress.value.copy(currentLoop = currentLoop)

                for ((index, step) in macro.steps.withIndex()) {
                    currentCoroutineContext().ensureActive()

                    // Duraklatma kontrolü
                    while (isPaused && currentCoroutineContext().isActive) {
                        delay(100)
                    }

                    _progress.value = _progress.value.copy(
                        currentStepIndex = index + 1,
                        currentStepDescription = "${step.label} (${step.durationMs}ms)"
                    )

                    // Adımı yürüt (Hata durumunda exception fırlatılır)
                    executeStep(step, macro.targetOrientation)

                    // Adım sonrası bekleme gecikmesi (delayAfterMs)
                    if (step.delayAfterMs > 0) {
                        delay(step.delayAfterMs)
                    }
                }
            }

            if (currentCoroutineContext().isActive) {
                _progress.value = _progress.value.copy(
                    status = EngineStatus.COMPLETED,
                    currentStepDescription = "Başarıyla tamamlandı ($currentLoop tekrar yapıldı)"
                )
            }
        } catch (e: CancellationException) {
            Log.d(TAG, "Makro döngüsü iptal edildi.")
        } catch (e: Exception) {
            val errorMsg = e.localizedMessage ?: "Dokunma işlemi yürütülürken beklenmeyen bir hata oluştu"
            Log.e(TAG, "Makro hatası: $errorMsg", e)
            _progress.value = _progress.value.copy(
                status = EngineStatus.ERROR,
                errorMessage = errorMsg,
                currentStepDescription = "Hata: $errorMsg"
            )
        }
    }

    private suspend fun executeStep(step: MacroStep, macroOrientation: ScreenOrientation) {
        val service = serviceProvider()
        if (service == null) {
            throw IllegalStateException("Erişilebilirlik Hizmeti aktif değil veya durduruldu!")
        }

        when (step.type) {
            StepType.WAIT -> {delay(step.durationMs.coerceAtLeast(10L))}
            }
            StepType.TAP, StepType.LONG_PRESS, StepType.RELEASE -> {
                performTapOrHold(service, step, macroOrientation)
            }
            StepType.SWIPE -> {
                performSwipe(service, step, macroOrientation)
            }
        }
    }

    private suspend fun performTapOrHold(
        service: TouchMacroAccessibilityService,
        step: MacroStep,
        macroOrientation: ScreenOrientation
    ) = gestureMutex.withLock {
        val currentOrientation = GalaxyTabOptimizer.getCurrentOrientation(service)
        val (finalX, finalY) = GalaxyTabOptimizer.adaptCoordinates(
            step.x,
            step.y,
            macroOrientation,
            currentOrientation
        )

        // Asgari 50ms süre garantisi
        val duration = step.durationMs.coerceAtLeast(GalaxyTabOptimizer.MIN_RECOMMENDED_DURATION_MS)

        val path = Path().apply {
            moveTo(finalX, finalY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        val success = dispatchGestureSynchronously(service, gesture)
        if (!success) {
            throw RuntimeException("Dokunma işlemi başarısız oldu (X: ${finalX.toInt()}, Y: ${finalY.toInt()}). Ekran meşgul veya kilitli olabilir.")
        }
    }

    private suspend fun performSwipe(
        service: TouchMacroAccessibilityService,
        step: MacroStep,
        macroOrientation: ScreenOrientation
    ) = gestureMutex.withLock {
        val currentOrientation = GalaxyTabOptimizer.getCurrentOrientation(service)
        val (startX, startY) = GalaxyTabOptimizer.adaptCoordinates(
            step.x,
            step.y,
            macroOrientation,
            currentOrientation
        )
        val (endX, endY) = GalaxyTabOptimizer.adaptCoordinates(
            step.endX,
            step.endY,
            macroOrientation,
            currentOrientation
        )

        val duration = step.durationMs.coerceAtLeast(GalaxyTabOptimizer.MIN_RECOMMENDED_DURATION_MS)

        val path = Path().apply {
            moveTo(startX, startY)
            lineTo(endX, endY)
        }

        val stroke = GestureDescription.StrokeDescription(path, 0L, duration)
        val gesture = GestureDescription.Builder()
            .addStroke(stroke)
            .build()

        val success = dispatchGestureSynchronously(service, gesture)
        if (!success) {
            throw RuntimeException("Kaydırma işlemi başarısız oldu (X: ${startX.toInt()} -> ${endX.toInt()}).")
        }
    }

    /**
     * Gesture sonucunu CompletableDeferred ile bekleyerek senkronize eder
     */
    private suspend fun dispatchGestureSynchronously(
        service: TouchMacroAccessibilityService,
        gesture: GestureDescription
    ): Boolean {
        val deferred = CompletableDeferred<Boolean>()

        val callback = object : AccessibilityService.GestureResultCallback() {
            override fun onCompleted(gestureDescription: GestureDescription?) {
                super.onCompleted(gestureDescription)
                deferred.complete(true)
            }

            override fun onCancelled(gestureDescription: GestureDescription?) {
                super.onCancelled(gestureDescription)
                Log.w(TAG, "dispatchGesture sistem tarafından iptal edildi.")
                deferred.complete(false)
            }
        }

        val dispatched = withContext(Dispatchers.Main.immediate) {service.dispatchGesture(gesture, callback, null)}
        if (!dispatched) {
            Log.e(TAG, "dispatchGesture çağrısı sistem tarafından reddedildi.")
            return false
        }

        return withTimeoutOrNull(5000L) {
            deferred.await()
        } ?: false
    }

    fun release() {
        stopMacro("Motor kapatıldı")
        scope.cancel()
    }
}
