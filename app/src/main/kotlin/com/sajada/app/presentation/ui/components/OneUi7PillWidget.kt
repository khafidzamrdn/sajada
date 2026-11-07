package com.sajada.app.presentation.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajada.app.domain.model.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OneUi7PillWidget(
    selectedMode: ModeAbsensi,
    isBtConnected: Boolean,
    deviceName: String,
    latestLog: LogAbsensi?,
    onSwitchMode: (ModeAbsensi) -> Unit,
    onToggleBluetooth: () -> Unit
) {
    var isExpandedSheetOpen by remember { mutableStateOf(false) }
    var showSuccessState by remember { mutableStateOf(false) }
    var activeLogEvent by remember { mutableStateOf<LogAbsensi?>(null) }

    // Trigger success state animation when new log arrives
    LaunchedEffect(latestLog) {
        if (latestLog != null) {
            activeLogEvent = latestLog
            showSuccessState = true
            delay(3500) // Show for 3.5 seconds
            showSuccessState = false
        }
    }

    val isDark = isSystemInDarkTheme()
    val pillBackgroundColor = if (isDark) {
        Color(0xFF1E1E1E).copy(alpha = 0.85f)
    } else {
        Color(0xFF222222).copy(alpha = 0.90f)
    }
    val pillBorderColor = Color.White.copy(alpha = 0.2f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .clip(CircleShape)
                .clickable { isExpandedSheetOpen = true },
            shape = CircleShape,
            color = pillBackgroundColor,
            shadowElevation = 8.dp,
            border = androidx.compose.foundation.BorderStroke(0.5.dp, pillBorderColor)
        ) {
            AnimatedContent(
                targetState = showSuccessState,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) + scaleIn(initialScale = 0.9f, animationSpec = tween(300)) togetherWith
                            fadeOut(animationSpec = tween(300)) + scaleOut(targetScale = 0.9f, animationSpec = tween(300))
                },
                label = "OneUi7PillAnimation"
            ) { isSuccess ->
                if (isSuccess && activeLogEvent != null) {
                    // State 2: Success Attendance Live Event Pill
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF00C853)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        Column {
                            Text(
                                text = activeLogEvent!!.namaJamaah,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "${activeLogEvent!!.waktuShalat} • ${activeLogEvent!!.status.name}",
                                color = Color(0xFF98FBCB),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else {
                    // State 1: Idle Bluetooth / Mode Indicator Pill
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Dot indicator
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    when {
                                        selectedMode == ModeAbsensi.MANUAL -> Color(0xFFFFAB00)
                                        isBtConnected -> Color(0xFF00C853)
                                        else -> Color(0xFFFF5252)
                                    }
                                )
                        )

                        Icon(
                            imageVector = if (selectedMode == ModeAbsensi.AUTOMATIC_FINGERPRINT) Icons.Default.Bluetooth else Icons.Default.Edit,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )

                        Text(
                            text = when (selectedMode) {
                                ModeAbsensi.MANUAL -> "Mode Manual (Admin)"
                                ModeAbsensi.AUTOMATIC_FINGERPRINT -> if (isBtConnected) "Terhubung: $deviceName" else "Bluetooth Terputus"
                            },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Icon(
                            Icons.Default.KeyboardArrowUp,
                            contentDescription = "Options",
                            tint = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    // Quick Glassmorphic Bottom Sheet Popup when Pill is clicked
    if (isExpandedSheetOpen) {
        ModalBottomSheet(
            onDismissRequest = { isExpandedSheetOpen = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = if (isDark) Color(0xFF1E1E1E).copy(alpha = 0.95f) else Color(0xFFFAFAFA).copy(alpha = 0.95f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    "One UI 7 • Quick Control Pill",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    "Kelola mode absensi dan koneksi perangkat keras secara instan.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Mode switch options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            onSwitchMode(ModeAbsensi.MANUAL)
                            isExpandedSheetOpen = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == ModeAbsensi.MANUAL) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Manual")
                    }

                    Button(
                        onClick = {
                            onSwitchMode(ModeAbsensi.AUTOMATIC_FINGERPRINT)
                            isExpandedSheetOpen = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedMode == ModeAbsensi.AUTOMATIC_FINGERPRINT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Bluetooth")
                    }
                }

                if (selectedMode == ModeAbsensi.AUTOMATIC_FINGERPRINT) {
                    OutlinedButton(
                        onClick = {
                            onToggleBluetooth()
                            isExpandedSheetOpen = false
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(
                            imageVector = if (isBtConnected) Icons.Default.Close else Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (isBtConnected) "Putuskan Koneksi Bluetooth" else "Scan & Hubungkan Ulang Device")
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
