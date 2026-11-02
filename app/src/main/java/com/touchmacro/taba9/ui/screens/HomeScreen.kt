package com.touchmacro.taba9.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.touchmacro.taba9.data.db.MacroDatabase
import com.touchmacro.taba9.data.model.MacroEntity
import com.touchmacro.taba9.data.model.MacroStep
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    database: MacroDatabase,
    onNavigateToEditor: (Long) -> Unit,
    onNavigateToSetup: () -> Unit,
    onStartFloatingHud: (MacroEntity) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val macros by database.macroDao().getAllMacros().collectAsState(initial = emptyList())
    var selectedProfile by remember { mutableStateOf("Tümü") }

    // Profil listesi
    val profiles = remember(macros) {
        listOf("Tümü") + macros.map { it.gameProfile }.distinct().filter { it.isNotBlank() }
    }

    val filteredMacros = remember(macros, selectedProfile) {
        if (selectedProfile == "Tümü") macros else macros.filter { it.gameProfile == selectedProfile }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertizontally) {
                        Text("Touch Macro", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primaryContainer) {
                            Text("Samsung Galaxy Tab A9+", fontSize = 11.sp)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToSetup) {
                        Icon(Icons.Default.Settings, contentDescription = "İzin Ayarları")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    // Yeni makro oluştur
                    coroutineScope.launch {
                        val newMacro = MacroEntity(
                            name = "Yeni Makro #${macros.size + 1}",
                            gameProfile = if (selectedProfile != "Tümü") selectedProfile else "Genel Oyun",
                            steps = listOf(
                                MacroStep.createTap(960f, 600f, 1, "Merkez Dokun"),
                                MacroStep.createWait(500L, 2)
                            )
                        )
                        val id = database.macroDao().insertMacro(newMacro)
                        onNavigateToEditor(id)
                    }
                },
                icon = { Icon(Icons.Default.Add, contentDescription = "Yeni Makro") },
                text = { Text("Yeni Makro Ekle") },
                containerColor = MaterialTheme.colorScheme.primary
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Oyun Profilleri Filtresi
            ScrollableTabRow(
                selectedTabIndex = profiles.indexOf(selectedProfile).coerceAtLeast(0),
                edgePadding = 0.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                profiles.forEach { profile ->
                    Tab(
                        selected = selectedProfile == profile,
                        onClick = { selectedProfile = profile },
                        text = { Text(profile, fontWeight = if (selectedProfile == profile) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (filteredMacros.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Default.TouchApp,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Henüz kaydedilmiş bir makro yok.", color = Color.Gray)
                        Text("Aşağıdaki buton ile ilk dokunma makronuzu oluşturun.", color = Color.DarkGray)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredMacros, key = { it.id }) { macro ->
                        MacroItemCard(
                            macro = macro,
                            onEdit = { onNavigateToEditor(macro.id) },
                            onStartOverlay = { onStartFloatingHud(macro) },
                            onDelete = {
                                coroutineScope.launch {
                                    database.macroDao().deleteMacro(macro)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MacroItemCard(
    macro: MacroEntity,
    onEdit: () -> Unit,
    onStartOverlay: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertizontally,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertizontally) {
                    Text(
                        text = macro.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            text = macro.gameProfile,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "${macro.steps.size} Adım • ${if (macro.isInfiniteLoop) "Sonsuz Tekrar" else "${macro.loopCount} Tekrar"} • Toplam Süre: ~${macro.totalDurationPerLoopMs} ms",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            Row(verticalAlignment = Alignment.CenterVertizontally) {
                // Yüzen Panel Başlat Butonu
                FilledTonalButton(
                    onClick = onStartOverlay,
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color(0xFF10B981),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Başlat", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Oyna")
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color.LightGray)
                }
            }
        }
    }
}
