package com.touchmacro.taba9.ui

import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.touchmacro.taba9.TouchMacroApp
import com.touchmacro.taba9.service.TouchMacroAccessibilityService
import com.touchmacro.taba9.ui.screens.EditorScreen
import com.touchmacro.taba9.ui.screens.HomeScreen
import com.touchmacro.taba9.ui.screens.SetupScreen
import com.touchmacro.taba9.ui.theme.TouchMacroTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            TouchMacroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    var isAccessibilityEnabled by remember { mutableStateOf(checkAccessibilityPermission()) }
                    var isOverlayEnabled by remember { mutableStateOf(checkOverlayPermission()) }

                    // Uygulama Ayarlardan geri döndüğünde izinleri otomatik güncelle
                    val lifecycleOwner = LocalLifecycleOwner.current
                    DisposableEffect(lifecycleOwner) {
                        val observer = LifecycleEventObserver { _, event ->
                            if (event == Lifecycle.Event.ON_RESUME) {
                                isAccessibilityEnabled = checkAccessibilityPermission()
                                isOverlayEnabled = checkOverlayPermission()
                            }
                        }
                        lifecycleOwner.lifecycle.addObserver(observer)
                        onDispose {
                            lifecycleOwner.lifecycle.removeObserver(observer)
                        }
                    }

                    val startDestination = if (isAccessibilityEnabled) "home" else "setup"

                    NavHost(navController = navController, startDestination = startDestination) {
                        composable("setup") {
                            SetupScreen(
                                isAccessibilityEnabled = isAccessibilityEnabled,
                                isOverlayEnabled = isOverlayEnabled,
                                onOpenAccessibilitySettings = { openAccessibilitySettings() },
                                onOpenOverlaySettings = { openOverlaySettings() },
                                onRefreshPermissions = {
                                    isAccessibilityEnabled = checkAccessibilityPermission()
                                    isOverlayEnabled = checkOverlayPermission()
                                },
                                onProceedToHome = {
                                    navController.navigate("home") {
                                        popUpTo("setup") { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable("home") {
                            HomeScreen(
                                database = TouchMacroApp.instance.database,
                                onNavigateToEditor = { macroId ->
                                    navController.navigate("editor?macroId=$macroId")
                                },
                                onNavigateToSetup = {
                                    navController.navigate("setup")
                                },
                                onStartFloatingHud = { macro ->
                                    val service = TouchMacroAccessibilityService.instance
                                    if (service != null && TouchMacroAccessibilityService.isServiceRunning) {
                                        service.showFloatingControls(macro)
                                        Toast.makeText(this@MainActivity, "Yüzen kontrol paneli açıldı. Oyununuza geçebilirsiniz.", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(this@MainActivity, "Lütfen önce Erişilebilirlik Hizmetini etkinleştirin.", Toast.LENGTH_LONG).show()
                                        openAccessibilitySettings()
                                    }
                                }
                            )
                        }

                        composable("editor?macroId={macroId}") { backStackEntry ->
                            val macroId = backStackEntry.arguments?.getString("macroId")?.toLongOrNull() ?: 0L
                            EditorScreen(
                                macroId = macroId,
                                database = TouchMacroApp.instance.database,
                                onNavigateBack = { navController.popBackStack() },
                                onLaunchPointPicker = { callback ->
                                    val service = TouchMacroAccessibilityService.instance
                                    if (service != null) {
                                        service.startPointPicker { x, y ->
                                            callback(x, y)
                                        }
                                    } else {
                                        Toast.makeText(this@MainActivity, "Nokta seçici için Erişilebilirlik Hizmeti açık olmalıdır.", Toast.LENGTH_LONG).show()
                                        openAccessibilitySettings()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAccessibilityPermission(): Boolean {
        val am = getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
        val enabledServices = am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)
        val expectedServiceName = "${packageName}/${TouchMacroAccessibilityService::class.java.canonicalName}"
        val expectedShortName = "${packageName}/.service.TouchMacroAccessibilityService"

        for (service in enabledServices) {
            val id = service.id
            if (id.equals(expectedServiceName, ignoreCase = true) || id.equals(expectedShortName, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    private fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(this)
        } else {
            true
        }
    }

    private fun openAccessibilitySettings() {
        try {
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Ayarlar açılamadı", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openOverlaySettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
            } catch (e: Exception) {
                val fallbackIntent = Intent(Settings.ACTION_SETTINGS)
                startActivity(fallbackIntent)
            }
        }
    }
}
