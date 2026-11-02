package com.touchmacro.taba9.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import com.touchmacro.taba9.data.model.ScreenOrientation
import com.touchmacro.taba9.data.model.StepType
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    macroId: Long,
    database: MacroDatabase,
    onNavigateBack: () -> Unit,
    onLaunchPointPicker: ((Float, Float) -> Unit) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var macro by remember { mutableStateOf<MacroEntity?>(null) }

    LaunchedEffect(macroId) {
        val loaded = database.macroDao().getMacroById(macroId)
        macro = loaded ?: MacroEntity(name = "Yeni Makro")
    }

    if (macro == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var currentMacro by remember(macro) { mutableStateOf(macro!!) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Makro Düzenleyici") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                database.macroDao().insertMacro(currentMacro)
                                onNavigateBack()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Kaydet")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddDialog = true },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Adım Ekle") },
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
            // Makro Bilgileri (İsim, Oyun Profili, Tekrar Sayısı)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = currentMacro.name,
                            onValueChange = { currentMacro = currentMacro.copy(name = it) },
                            label = { Text("Makro Adı") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true
                        )

                        OutlinedTextField(
                            value = currentMacro.gameProfile,
                            onValueChange = { currentMacro = currentMacro.copy(gameProfile = it) },
                            label = { Text("Oyun Profili") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertizontally,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertizontally) {
                            Text("Tekrar Sayısı:")
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = currentMacro.loopCount == 0,
                                onClick = { currentMacro = currentMacro.copy(loopCount = 0) },
                                label = { Text("Sonsuz (Durdurana Kadar)") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = currentMacro.loopCount == 5,
                                onClick = { currentMacro = currentMacro.copy(loopCount = 5) },
                                label = { Text("5x") }
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            FilterChip(
                                selected = currentMacro.loopCount == 20,
                                onClick = { currentMacro = currentMacro.copy(loopCount = 20) },
                                label = { Text("20x") }
                            )
                        }

                        // Ekran Yönü Uyarısı / Güvenlik
                        Row(verticalAlignment = Alignment.CenterVertizontally) {
                            Checkbox(
                                checked = currentMacro.stopOnOrientationChange,
                                onCheckedChange = { currentMacro = currentMacro.copy(stopOnOrientationChange = it) }
                            )
                            Text("Yön değişirse güvenle durdur", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Makro Adımları Başlığı
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertizontally
            ) {
                Text(
                    text = "Makro Adımları (${currentMacro.steps.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Ekrana Dokunarak Koordinat Yakalama Butonu
                FilledTonalButton(
                    onClick = {
                        onLaunchPointPicker { x, y ->
                            val newStep = MacroStep.createTap(
                                x = x,
                                y = y,
                                order = currentMacro.steps.size + 1,
                                label = "Ekrandan Seçilen Nokta"
                            )
                            currentMacro = currentMacro.copy(steps = currentMacro.steps + newStep)
                        }
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ekrandan Dokunup Seç")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Adım Listesi (Yeniden sıralanabilir ve düzenlenebilir)
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(currentMacro.steps, key = { _, item -> item.id }) { index, step ->
                    StepItemRow(
                        step = step,
                        index = index,
                        totalSteps = currentMacro.steps.size,
                        onMoveUp = {
                            if (index > 0) {
                                val list = currentMacro.steps.toMutableList()
                                val item = list.removeAt(index)
                                list.add(index - 1, item)
                                currentMacro = currentMacro.copy(steps = list)
                            }
                        },
                        onMoveDown = {
                            if (index < currentMacro.steps.size - 1) {
                                val list = currentMacro.steps.toMutableList()
                                val item = list.removeAt(index)
                                list.add(index + 1, item)
                                currentMacro = currentMacro.copy(steps = list)
                            }
                        },
                        onDelete = {
                            val list = currentMacro.steps.toMutableList()
                            list.removeAt(index)
                            currentMacro = currentMacro.copy(steps = list)
                        },
                        onUpdate = { updated ->
                            val list = currentMacro.steps.toMutableList()
                            list[index] = updated
                            currentMacro = currentMacro.copy(steps = list)
                        }
                    )
                }
            }
        }
    }

    // Yeni Adım Ekleme Diyaloğu
    if (showAddDialog) {
        AddStepDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { newStep ->
                currentMacro = currentMacro.copy(steps = currentMacro.steps + newStep)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun StepItemRow(
    step: MacroStep,
    index: Int,
    totalSteps: Int,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    onUpdate: (MacroStep) -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertizontally
        ) {
            // Adım Sıra Numarası Rozeti
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "#${index + 1}",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${step.type.name} - ${step.label}",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Text(
                    text = if (step.type == StepType.SWIPE) {
                        "X: ${step.x.toInt()}, Y: ${step.y.toInt()} -> X: ${step.endX.toInt()}, Y: ${step.endY.toInt()} | Süre: ${step.durationMs}ms | Gecikme: ${step.delayAfterMs}ms"
                    } else if (step.type == StepType.WAIT) {
                        "Bekleme Süresi: ${step.delayAfterMs}ms"
                    } else {
                        "Koordinat: X=${step.x.toInt()}, Y=${step.y.toInt()} | Süre: ${step.durationMs}ms (Min: 50ms) | Gecikme: ${step.delayAfterMs}ms"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray
                )
            }

            // Yukarı / Aşağı Taşıma Butonları (Reordering)
            IconButton(onClick = onMoveUp, enabled = index > 0) {
                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Yukarı")
            }
            IconButton(onClick = onMoveDown, enabled = index < totalSteps - 1) {
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Aşağı")
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Sil", tint = Color(0xFFEF4444))
            }
        }
    }
}

@Composable
fun AddStepDialog(
    onDismiss: () -> Unit,
    onAdd: (MacroStep) -> Unit
) {
    var stepType by remember { mutableStateOf(StepType.TAP) }
    var label by remember { mutableStateOf("Dokunma Adımı") }
    var xCoord by remember { mutableStateOf("960") }
    var yCoord by remember { mutableStateOf("600") }
    var durationMs by remember { mutableStateOf("100") }
    var delayAfterMs by remember { mutableStateOf("300") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Yeni Adım Ekle") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = stepType == StepType.TAP,
                        onClick = { stepType = StepType.TAP },
                        label = { Text("Dokun") }
                    )
                    FilterChip(
                        selected = stepType == StepType.LONG_PRESS,
                        onClick = { stepType = StepType.LONG_PRESS },
                        label = { Text("Basılı Tut") }
                    )
                    FilterChip(
                        selected = stepType == StepType.SWIPE,
                        onClick = { stepType = StepType.SWIPE },
                        label = { Text("Kaydır") }
                    )
                    FilterChip(
                        selected = stepType == StepType.WAIT,
                        onClick = { stepType = StepType.WAIT },
                        label = { Text("Bekle") }
                    )
                }

                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Adım Açıklaması") },
                    singleLine = true
                )

                if (stepType != StepType.WAIT) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = xCoord,
                            onValueChange = { xCoord = it },
                            label = { Text("X Koordinat (0-1920)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = yCoord,
                            onValueChange = { yCoord = it },
                            label = { Text("Y Koordinat (0-1200)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = durationMs,
                        onValueChange = { durationMs = it },
                        label = { Text("Süre ms (Min 50ms)") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = delayAfterMs,
                        onValueChange = { delayAfterMs = it },
                        label = { Text("Bekleme ms") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dur = durationMs.toLongOrNull()?.coerceAtLeast(50L) ?: 100L
                    val del = delayAfterMs.toLongOrNull() ?: 300L
                    val x = xCoord.toFloatOrNull() ?: 960f
                    val y = yCoord.toFloatOrNull() ?: 600f

                    val step = MacroStep(
                        type = stepType,
                        label = label,
                        x = x,
                        y = y,
                        durationMs = dur,
                        delayAfterMs = del
                    )
                    onAdd(step)
                }
            ) {
                Text("Ekle")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("İptal")
            }
        }
    )
}
