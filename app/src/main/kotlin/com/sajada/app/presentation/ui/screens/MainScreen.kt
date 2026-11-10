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
import com.sajada.app.presentation.ui.components.SajadaBottomNavBar
import com.sajada.app.presentation.viewmodel.AttendanceViewModel
import com.sajada.app.presentation.viewmodel.Screen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MainScreen(viewModel: AttendanceViewModel) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isManageOpen by viewModel.isManageJamaahOpen.collectAsState()
    val editingLog by viewModel.editingLog.collectAsState()
    val jamaahList by viewModel.jamaahList.collectAsState()

    val isDark = isSystemInDarkTheme()
    val bgGradient = if (isDark) {
        Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFE0F2FE), Color(0xFFF1F5F9)))
    }

    Scaffold(
        containerColor = Color.Transparent,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            SajadaBottomNavBar(
                currentScreen = currentScreen,
                onSelect = { viewModel.navigateTo(it) }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // Embedded app wallpaper with crystal clear frosted overlay
            Image(
                painter = painterResource(id = R.drawable.app_wallpaper),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            when (currentScreen) {
                Screen.DASHBOARD -> MainDashboardContent(viewModel = viewModel)
                Screen.JADWAL -> JadwalScreen(viewModel = viewModel)
                Screen.ABSENSI -> AbsensiScreen(viewModel = viewModel)
                Screen.REKAP -> WeeklyRecapScreen(recapList = viewModel.getWeeklyRecap())
                Screen.JAMAAH -> JamaahScreen(viewModel = viewModel)
            }

            // Global dialogs available from any page
            if (isManageOpen) {
                ManageJamaahDialog(
                    jamaahList = jamaahList,
                    onDismiss = { viewModel.setManageJamaahOpen(false) },
                    onAdd = { name, fp -> viewModel.addJamaah(name, fp) },
                    onDelete = { id -> viewModel.removeJamaah(id) }
                )
            }

            if (editingLog != null) {
                EditLogDialog(
                    log = editingLog!!,
                    onDismiss = { viewModel.setEditingLog(null) },
                    onSave = { logId, status, prayer -> viewModel.updateLog(logId, status, prayer) }
                )
            }
        }
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
    val latestLog by viewModel.latestAttendanceEvent.collectAsState()

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
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                },
                actions = {
                    // Add Jamaah Button
                    IconButton(onClick = { viewModel.setManageJamaahOpen(true) }) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Tambah Jamaah",
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
                                    .background(Color(0xFFF1F3F5))
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
                                    onRecordStatus = { jamaahId, waktu, status ->
                                        viewModel.recordManualStatus(jamaahId, waktu, status)
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
                                    color = Color(0xFF5F6368),
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
}

@Composable
fun ModeButton(title: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val bg = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent
    val fg = if (selected) MaterialTheme.colorScheme.onPrimary else Color(0xFF1A1A2E)

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
                        color = Color(0xFF1A1A2E)
                    )
                    Text(
                        if (isConnected) "Scanner siap menerima sinyal sidik jari" else "Ketuk Hubungkan untuk aktifkan scanner",
                        fontSize = 12.sp,
                        color = Color(0xFF5F6368)
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
            Divider(color = Color(0xFFE0E0E0))
            Text("Simulasi Hardware Scanner:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1A1A2E))

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
                        Text(currentJamaah?.let { "${it.nama} (${it.fingerprintId})" } ?: "Pilih Jamaah", color = Color(0xFF1A1A2E))
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
    onRecordStatus: (String, WaktuShalat, StatusAbsensi) -> Unit
) {
    var selectedPrayer by remember { mutableStateOf(WaktuShalat.DZUHUR) }
    var prayerDropdownExpanded by remember { mutableStateOf(false) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Pilih Waktu Shalat:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF1A1A2E))
            
            Box {
                OutlinedButton(
                    onClick = { prayerDropdownExpanded = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF1A1A2E))
                ) {
                    Text(selectedPrayer.name)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
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
        }

        Divider(color = Color(0xFFE0E0E0))

        Text("Daftar Jamaah & Quick Action Absensi:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF1A1A2E))

        if (jamaahList.isEmpty()) {
            Text("Belum ada anggota jamaah terdaftar.", color = Color(0xFF5F6368), fontSize = 13.sp)
        } else {
            jamaahList.forEach { jamaah ->
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    cornerRadius = 20.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(jamaah.nama, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF1A1A2E))
                                Text("ID: ${jamaah.id}", fontSize = 11.sp, color = Color(0xFF5F6368))
                            }
                        }

                        // 4 Status Buttons: Hadir, Sakit, Izin, Alfa
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusActionButton(
                                title = "Hadir",
                                color = Color(0xFF00C853),
                                modifier = Modifier.weight(1f)
                            ) {
                                onRecordStatus(jamaah.id, selectedPrayer, StatusAbsensi.HADIR)
                            }

                            StatusActionButton(
                                title = "Sakit",
                                color = Color(0xFF29B6F6),
                                modifier = Modifier.weight(1f)
                            ) {
                                onRecordStatus(jamaah.id, selectedPrayer, StatusAbsensi.SAKIT)
                            }

                            StatusActionButton(
                                title = "Izin",
                                color = Color(0xFFFFB300),
                                modifier = Modifier.weight(1f)
                            ) {
                                onRecordStatus(jamaah.id, selectedPrayer, StatusAbsensi.IZIN)
                            }

                            StatusActionButton(
                                title = "Alfa",
                                color = Color(0xFFFF5252),
                                modifier = Modifier.weight(1f)
                            ) {
                                onRecordStatus(jamaah.id, selectedPrayer, StatusAbsensi.TIDAK_HADIR)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatusActionButton(title: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(34.dp),
        shape = RoundedCornerShape(10.dp),
        color = color.copy(alpha = 0.85f),
        shadowElevation = 2.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.White)
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
        StatusAbsensi.HADIR -> Color(0xFF00C853)
        StatusAbsensi.SAKIT -> Color(0xFF29B6F6)
        StatusAbsensi.IZIN -> Color(0xFFFFB300)
        StatusAbsensi.TIDAK_HADIR -> Color(0xFFFF5252)
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
                    Text(log.namaJamaah, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF1A1A2E))
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
                    color = Color(0xFF5F6368)
                )
                Text(
                    "Mode: ${log.mode}",
                    fontSize = 11.sp,
                    color = Color(0xFF00875A)
                )
            }

            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color(0xFF1A1A2E))
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
