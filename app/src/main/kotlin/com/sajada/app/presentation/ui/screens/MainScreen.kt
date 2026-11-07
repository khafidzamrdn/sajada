package com.sajada.app.presentation.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sajada.app.R
import com.sajada.app.domain.model.*
import com.sajada.app.presentation.ui.components.GlassCard
import com.sajada.app.presentation.ui.components.OneUi7PillWidget
import com.sajada.app.presentation.viewmodel.AttendanceViewModel
import com.sajada.app.presentation.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(viewModel: AttendanceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()

    when (currentScreen) {
        Screen.MAIN -> MainDashboardContent(viewModel = viewModel)
        Screen.WEEKLY_RECAP -> WeeklyRecapScreen(
            recapList = viewModel.getWeeklyRecap(),
            onBack = { viewModel.navigateTo(Screen.MAIN) }
        )
        Screen.AI_ASSISTANT -> AiAssistantScreen(
            viewModel = viewModel,
            onBack = { viewModel.navigateTo(Screen.MAIN) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardContent(viewModel: AttendanceViewModel) {
    val mode by viewModel.selectedMode.collectAsState()
    val isBtConnected by viewModel.isBluetoothConnected.collectAsState()
    val btDeviceName by viewModel.bluetoothDeviceName.collectAsState()
    val jamaahList by viewModel.jamaahList.collectAsState()
    val logs by viewModel.attendanceLogs.collectAsState()
    val isManageOpen by viewModel.isManageJamaahOpen.collectAsState()
    val editingLog by viewModel.editingLog.collectAsState()
    val latestLog by viewModel.latestAttendanceEvent.collectAsState()

    val isDark = isSystemInDarkTheme()
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFF1F5F9)))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgGradient)
    ) {
        // Embedded app wallpaper
        Image(
            painter = painterResource(id = R.drawable.app_wallpaper),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        )

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                LargeTopAppBar(
                    title = {
                        Column {
                            Text(
                                "Absensi Jamaah",
                                fontWeight = FontWeight.Bold,
                                fontSize = 28.sp,
                                color = Color.White
                            )
                            Text(
                                "Masjid Al-Ikhlas • 5 Waktu",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    },
                    actions = {
                        // AI Assistant Button
                        IconButton(onClick = { viewModel.navigateTo(Screen.AI_ASSISTANT) }) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Asisten AI Gemini",
                                tint = Color(0xFFFFD700)
                            )
                        }
                        // Weekly Recap Button
                        IconButton(onClick = { viewModel.navigateTo(Screen.WEEKLY_RECAP) }) {
                            Icon(
                                Icons.Default.BarChart,
                                contentDescription = "Rekap Mingguan",
                                tint = Color(0xFF00E676)
                            )
                        }
                        // Manage Jamaah Button
                        IconButton(onClick = { viewModel.setManageJamaahOpen(true) }) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = "Kelola Jamaah",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.largeTopAppBarColors(
                        containerColor = Color.Transparent
                    )
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 0. One UI 7 Dynamic Pill Widget
                item {
                    OneUi7PillWidget(
                        selectedMode = mode,
                        isBtConnected = isBtConnected,
                        deviceName = btDeviceName,
                        latestLog = latestLog,
                        onSwitchMode = { newMode -> viewModel.setMode(newMode) },
                        onToggleBluetooth = { viewModel.toggleBluetoothConnection() }
                    )
                }

                // 1. Mode Selector & Bluetooth / Manual Section
                item {
                    GlassCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                    .padding(4.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                ModeButton(
                                    title = "Mode Manual",
                                    selected = mode == ModeAbsensi.MANUAL,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    viewModel.setMode(ModeAbsensi.MANUAL)
                                }
                                ModeButton(
                                    title = "Bluetooth Fingerprint",
                                    selected = mode == ModeAbsensi.AUTOMATIC_FINGERPRINT,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    viewModel.setMode(ModeAbsensi.AUTOMATIC_FINGERPRINT)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            if (mode == ModeAbsensi.AUTOMATIC_FINGERPRINT) {
                                BluetoothStatusCard(
                                    isConnected = isBtConnected,
                                    deviceName = btDeviceName,
                                    onToggleConnect = { viewModel.toggleBluetoothConnection() },
                                    onSimulateScan = { fpId -> viewModel.simulateFingerprint(fpId) },
                                    jamaahList = jamaahList
                                )
                            } else {
                                ManualAttendanceForm(
                                    jamaahList = jamaahList,
                                    onRecord = { jamaahId, waktu ->
                                        viewModel.recordManual(jamaahId, waktu)
                                    }
                                )
                            }
                        }
                    }
                }

                // 2. Section Header: Rekap Log Hari Ini
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Rekap Log Hari Ini",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Text(
                            "${logs.size} Kehadiran",
                            fontSize = 14.sp,
                            color = Color(0xFF00E676),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // 3. Attendance Logs List
                if (logs.isEmpty()) {
                    item {
                        GlassCard(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "Belum ada absensi tercatat hari ini.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                } else {
                    items(logs, key = { it.id }) { log ->
                        AttendanceLogCard(
                            log = log,
                            onEdit = { viewModel.setEditingLog(log) },
                            onDelete = { viewModel.deleteLog(log.id) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }

        // Manage Jamaah Dialog
        if (isManageOpen) {
            ManageJamaahDialog(
                jamaahList = jamaahList,
                onDismiss = { viewModel.setManageJamaahOpen(false) },
                onAdd = { name, fp -> viewModel.addJamaah(name, fp) },
                onDelete = { id -> viewModel.removeJamaah(id) }
            )
        }

        // Edit Log Dialog
        if (editingLog != null) {
            EditLogDialog(
                log = editingLog!!,
                onDismiss = { viewModel.setEditingLog(null) },
                onSave = { logId, status, prayer -> viewModel.updateLog(logId, status, prayer) }
            )
        }
    }
}

@Composable
fun ModeButton(title: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else Color.White

    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = fg)
        }
    }
}

@Composable
fun BluetoothStatusCard(
    isConnected: Boolean,
    deviceName: String,
    onToggleConnect: () -> Unit,
    onSimulateScan: (String) -> Unit,
    jamaahList: List<Jamaah>
) {
    var selectedFpId by remember { mutableStateOf(jamaahList.firstOrNull()?.fingerprintId ?: "FP-1001") }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(if (isConnected) Color(0xFF00C853) else Color(0xFFFF5252))
                )
                Column {
                    Text(
                        if (isConnected) "Terhubung ke $deviceName" else "Bluetooth Terputus",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Text(
                        if (isConnected) "Scanner siap menerima sinyal sidik jari" else "Ketuk Hubungkan untuk aktifkan scanner",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }

            Button(
                onClick = onToggleConnect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isConnected) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (isConnected) "Putuskan" else "Hubungkan", fontSize = 12.sp)
            }
        }

        if (isConnected) {
            Divider(color = Color.White.copy(alpha = 0.2f))
            Text("Simulasi Hardware Scanner:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color.White)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }
                val currentJamaah = jamaahList.find { it.fingerprintId == selectedFpId }

                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(currentJamaah?.let { "${it.nama} (${it.fingerprintId})" } ?: "Pilih Jamaah", color = Color.White)
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        jamaahList.forEach { jamaah ->
                            DropdownMenuItem(
                                text = { Text("${jamaah.nama} (${jamaah.fingerprintId ?: "N/A"})") },
                                onClick = {
                                    if (jamaah.fingerprintId != null) {
                                        selectedFpId = jamaah.fingerprintId!!
                                    }
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = { onSimulateScan(selectedFpId) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scan Jari")
                }
            }
        }
    }
}

@Composable
fun ManualAttendanceForm(
    jamaahList: List<Jamaah>,
    onRecord: (String, WaktuShalat) -> Unit
) {
    var selectedJamaahId by remember { mutableStateOf(jamaahList.firstOrNull()?.id ?: "") }
    var selectedPrayer by remember { mutableStateOf(WaktuShalat.DZUHUR) }
    var jamaahDropdownExpanded by remember { mutableStateOf(false) }
    var prayerDropdownExpanded by remember { mutableStateOf(false) }

    val selectedJamaahName = jamaahList.find { it.id == selectedJamaahId }?.nama ?: "Pilih Jamaah"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Form Absensi Manual (Admin)", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color.White)

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { jamaahDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(selectedJamaahName, color = Color.White)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }
            }
            DropdownMenu(expanded = jamaahDropdownExpanded, onDismissRequest = { jamaahDropdownExpanded = false }) {
                jamaahList.forEach { j ->
                    DropdownMenuItem(
                        text = { Text(j.nama) },
                        onClick = {
                            selectedJamaahId = j.id
                            jamaahDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { prayerDropdownExpanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Waktu Shalat: $selectedPrayer", color = Color.White)
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
                }
            }
            DropdownMenu(expanded = prayerDropdownExpanded, onDismissRequest = { prayerDropdownExpanded = false }) {
                WaktuShalat.values().forEach { prayer ->
                    DropdownMenuItem(
                        text = { Text(prayer.name) },
                        onClick = {
                            selectedPrayer = prayer
                            prayerDropdownExpanded = false
                        }
                    )
                }
            }
        }

        Button(
            onClick = {
                if (selectedJamaahId.isNotBlank()) {
                    onRecord(selectedJamaahId, selectedPrayer)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Simpan Kehadiran")
        }
    }
}

@Composable
fun AttendanceLogCard(
    log: LogAbsensi,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val timeFormatted = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date(log.timestamp))
    val statusColor = when (log.status) {
        StatusAbsensi.TEPAT_WAKTU -> Color(0xFF00C853)
        StatusAbsensi.TERLAMBAT -> Color(0xFFFFAB00)
        StatusAbsensi.DI_LUAR_JADWAL -> Color(0xFFFF5252)
    }

    var menuExpanded by remember { mutableStateOf(false) }

    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(log.namaJamaah, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                    Surface(
                        color = statusColor.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = log.status.name,
                            color = statusColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Waktu: ${log.waktuShalat} • Jam: $timeFormatted",
                    fontSize = 13.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    "Mode: ${log.mode}",
                    fontSize = 11.sp,
                    color = Color(0xFF98FBCB)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.White)
                }
                DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                    DropdownMenuItem(
                        text = { Text("Edit Log") },
                        onClick = {
                            menuExpanded = false
                            onEdit()
                        },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Log", color = Color(0xFFFF5252)) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFFF5252)) }
                    )
                }
            }
        }
    }
}

@Composable
fun ManageJamaahDialog(
    jamaahList: List<Jamaah>,
    onDismiss: () -> Unit,
    onAdd: (String, String?) -> Unit,
    onDelete: (String) -> Unit
) {
    var nameInput by remember { mutableStateOf("") }
    var fpInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Kelola Anggota Jamaah") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Nama Jamaah") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = fpInput,
                    onValueChange = { fpInput = it },
                    label = { Text("Fingerprint ID (Opsional, cth: FP-1004)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFFF5252), fontSize = 12.sp)
                }

                Button(
                    onClick = {
                        try {
                            if (nameInput.isNotBlank()) {
                                onAdd(nameInput, fpInput)
                                nameInput = ""
                                fpInput = ""
                                errorMessage = null
                            } else {
                                errorMessage = "Nama jamaah harus diisi"
                            }
                        } catch (e: Exception) {
                            errorMessage = e.message
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Tambah Jamaah")
                }

                Divider()

                Text("Daftar Jamaah Terdaftar (${jamaahList.size}):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(jamaahList, key = { it.id }) { jamaah ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(jamaah.nama, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("ID: ${jamaah.id} • FP: ${jamaah.fingerprintId ?: "-"}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            IconButton(onClick = { onDelete(jamaah.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFFF5252))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun EditLogDialog(
    log: LogAbsensi,
    onDismiss: () -> Unit,
    onSave: (String, StatusAbsensi, WaktuShalat) -> Unit
) {
    var selectedStatus by remember { mutableStateOf(log.status) }
    var selectedPrayer by remember { mutableStateOf(log.waktuShalat) }

    var statusExpanded by remember { mutableStateOf(false) }
    var prayerExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Log Absensi: ${log.namaJamaah}") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { statusExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Status: $selectedStatus")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                        StatusAbsensi.values().forEach { st ->
                            DropdownMenuItem(
                                text = { Text(st.name) },
                                onClick = {
                                    selectedStatus = st
                                    statusExpanded = false
                                }
                            )
                        }
                    }
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { prayerExpanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Waktu Shalat: $selectedPrayer")
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                    DropdownMenu(expanded = prayerExpanded, onDismissRequest = { prayerExpanded = false }) {
                        WaktuShalat.values().forEach { pr ->
                            DropdownMenuItem(
                                text = { Text(pr.name) },
                                onClick = {
                                    selectedPrayer = pr
                                    prayerExpanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(log.id, selectedStatus, selectedPrayer) },
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Simpan Perubahan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
