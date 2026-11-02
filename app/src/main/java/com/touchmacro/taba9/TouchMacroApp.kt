package com.touchmacro.taba9

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import com.touchmacro.taba9.data.db.MacroDatabase
import com.touchmacro.taba9.data.model.MacroEntity
import com.touchmacro.taba9.data.model.MacroStep
import com.touchmacro.taba9.data.model.ScreenOrientation
import com.touchmacro.taba9.data.model.StepType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Touch Macro Uygulaması
 * Samsung Galaxy Tab A9+ (WUXGA 1920x1200) için optimize edilmiş dokunma otomasyon motoru.
 */
class TouchMacroApp : Application() {

    lateinit var database: MacroDatabase
        private set

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        database = MacroDatabase.getInstance(this)
        createNotificationChannels()
        seedInitialMacrosIfEmpty()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Touch Macro Servisi",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Makro çalışırken ve yüzen panel etkinken arka plan durumu bildirimi"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    /**
     * İlk kurulumda kullanıcının hemen test edebilmesi için örnek Samsung Tab A9+ makrolarını ekler
     */
    private fun seedInitialMacrosIfEmpty() {
        applicationScope.launch {
            try {
                val existing = database.macroDao().getAllMacros().first()
                if (existing.isEmpty()) {
                    val defaultMacros = listOf(
                        MacroEntity(
                            name = "Klan Savaşı Otomatik Farm",
                            gameProfile = "RPG Auto-Battle",
                            description = "Saldırı butonuna basar, özel gücü tetikler ve ganimet toplar.",
                            loopCount = 0, // Sonsuz tekrar
                            targetOrientation = ScreenOrientation.LANDSCAPE,
                            stopOnOrientationChange = true,
                            steps = listOf(
                                MacroStep(
                                    order = 1,
                                    type = StepType.TAP,
                                    label = "Normal Saldırı (1650, 920)",
                                    x = 1650f,
                                    y = 920f,
                                    durationMs = 80L,
                                    delayAfterMs = 350L
                                ),
                                MacroStep(
                                    order = 2,
                                    type = StepType.TAP,
                                    label = "1. Yetenek (1480, 780)",
                                    x = 1480f,
                                    y = 780f,
                                    durationMs = 120L,
                                    delayAfterMs = 500L
                                ),
                                MacroStep(
                                    order = 3,
                                    type = StepType.LONG_PRESS,
                                    label = "Kalkan Savunması (1350, 950)",
                                    x = 1350f,
                                    y = 950f,
                                    durationMs = 600L,
                                    delayAfterMs = 300L
                                ),
                                MacroStep(
                                    order = 4,
                                    type = StepType.WAIT,
                                    label = "Yetenek Beklemesi",
                                    x = 0f,
                                    y = 0f,
                                    durationMs = 50L,
                                    delayAfterMs = 1000L
                                )
                            )
                        ),
                        MacroEntity(
                            name = "Maden & Balıkçılık Sonsuz Tıklayıcı",
                            gameProfile = "Hayatta Kalma & MMORPG",
                            description = "Sabit noktada 250 ms aralıklarla otomatik maden kazma tıklaması yapar.",
                            loopCount = 0, // Sonsuz tekrar
                            targetOrientation = ScreenOrientation.LANDSCAPE,
                            stopOnOrientationChange = false,
                            steps = listOf(
                                MacroStep(
                                    order = 1,
                                    type = StepType.TAP,
                                    label = "Kazma Tıklaması",
                                    x = 1100f,
                                    y = 650f,
                                    durationMs = 70L,
                                    delayAfterMs = 250L
                                )
                            )
                        )
                    )
                    for (macro in defaultMacros) {
                        database.macroDao().insertMacro(macro)
                    }
                }
            } catch (e: Exception) {
                // Sessizce devam et
            }
        }
    }

    companion object {
        const val CHANNEL_ID = "touch_macro_overlay_channel"
        lateinit var instance: TouchMacroApp
            private set
    }
}
