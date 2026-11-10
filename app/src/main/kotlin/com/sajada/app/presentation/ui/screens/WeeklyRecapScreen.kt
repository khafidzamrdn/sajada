package com.sajada.app.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajada.app.domain.usecase.WeeklyRecapItem
import com.sajada.app.presentation.ui.components.GlassCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyRecapScreen(
    recapList: List<WeeklyRecapItem>
) {
    var notificationSentDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Rekap Absensi 1 Minggu", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("📊 Laporan Mingguan Jamaah", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Rekapitulasi otomatis kehadiran jamaah selama 7 hari terakhir. Sistem otomatis mengirimkan notifikasi ringkasan ini setiap Malam Senin pukul 20:00.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { notificationSentDialog = true },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simulasi Kirim Notifikasi Malam Senin", fontSize = 12.sp)
                    }
                }
            }

            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Nama Jamaah", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(2f))
                Text("Hadir", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("Alfa", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1f))
                Text("Kehadiran", fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.weight(1.2f))
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(recapList) { item ->
                    val percentage = if (item.totalExpected > 0) (item.hadirCount.toFloat() / item.totalExpected * 100).toInt() else 0
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(item.nama, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(2f))
                            Text("${item.hadirCount}x", color = Color(0xFF00C853), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("${item.alfaCount}x", color = Color(0xFFFF5252), fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                            Text("$percentage%", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1.2f))
                        }
                    }
                }
            }
        }

        if (notificationSentDialog) {
            AlertDialog(
                onDismissRequest = { notificationSentDialog = false },
                title = { Text("Notifikasi Malam Senin Terkirim 🔔") },
                text = { Text("Notifikasi rekap mingguan berhasil disiarkan ke pengurus dan jamaah via Push Notification (Simulasi)") },
                confirmButton = {
                    Button(onClick = { notificationSentDialog = false }, shape = RoundedCornerShape(12.dp)) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
