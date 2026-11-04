package com.touchmacro.taba9.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(
    isAccessibilityEnabled: Boolean,
    isOverlayEnabled: Boolean,
    onOpenAccessibilitySettings: () -> Unit,
    onOpenOverlaySettings: () -> Unit,
    onRefreshPermissions: () -> Unit,
    onProceedToHome: () -> Unit
) {
    val allGranted = isAccessibilityEnabled && isOverlayEnabled

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Touch Macro Kurulumu (Galaxy Tab A9+)") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    IconButton(onClick = onRefreshPermissions) {
                        Icon(Icons.Default.Refresh, contentDescription = "Yenile")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 32.dp, vertical = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Dokunma Makro Motoru Kurulumu",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Samsung Galaxy Tab A9+ tabletinizde root, ADB veya Shizuku olmadan oyunlarda otomatik dokunma ve kaydırma yapabilmek için aşağıdaki iki Android iznini etkinleştirin.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // İzin 1: Erişilebilirlik Hizmeti
            PermissionCard(
                title = "1. Erişilebilirlik Hizmeti (Zorunlu)",
                description = "Uygulamanın ekran üzerinde dokunma (tap), basılı tutma ve kaydırma (dispatchGesture) hareketlerini gönderebilmesi için gereklidir.",
                isGranted = isAccessibilityEnabled,
                actionButtonText = if (isAccessibilityEnabled) "Etkinleştirildi" else "Erişilebilirlik Ayarlarını Aç",
                onActionClick = onOpenAccessibilitySettings
            )
            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onProceedToHome,
                enabled = isAccessibilityEnabled,
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text(
                    text = if (isAccessibilityEnabled) "Makrolara Başla →" else "Önce Erişilebilirlik Hizmetini Açın",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    title: String,
    description: String,
    isGranted: Boolean,
    actionButtonText: String,
    onActionClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isGranted) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = "Etkin",
                        tint = Color(0xFF10B981)
                    )
                } else {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = "Gerekiyor",
                        tint = Color(0xFFF59E0B)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onActionClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isGranted) Color(0xFF1E293B) else MaterialTheme.colorScheme.primary,
                    contentColor = if (isGranted) Color(0xFF10B981) else Color.White
                )
            ) {
                Text(actionButtonText)
            }
        }
    }
}
