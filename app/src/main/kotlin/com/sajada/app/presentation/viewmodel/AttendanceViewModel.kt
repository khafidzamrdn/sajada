package com.sajada.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajada.app.data.ai.GeminiAiService
import com.sajada.app.data.bluetooth.MockBluetoothFingerprintManagerImpl
import com.sajada.app.data.repository.InMemoryAttendanceRepository
import com.sajada.app.data.repository.InMemoryJamaahRepository
import com.sajada.app.domain.model.*
import com.sajada.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class Screen {
    MAIN, WEEKLY_RECAP, AI_ASSISTANT
}

class AttendanceViewModel : ViewModel() {

    private val jamaahRepo = InMemoryJamaahRepository(
        listOf(
            Jamaah("JM-001", "Ahmad Fauzi", "FP-1001"),
            Jamaah("JM-002", "Siti Aminah", "FP-1002"),
            Jamaah("JM-003", "Umar Faruq", "FP-1003")
        )
    )
    private val attendanceRepo = InMemoryAttendanceRepository()
    private val evaluator = AttendanceEvaluatorUseCase()
    private val addJamaahUseCase = AddJamaahUseCase(jamaahRepo)
    private val recordManualUseCase = RecordManualAttendanceUseCase(jamaahRepo, attendanceRepo, evaluator)
    private val processFingerprintUseCase = ProcessFingerprintScanUseCase(jamaahRepo, attendanceRepo, evaluator)
    private val weeklyRecapUseCase = GenerateWeeklyRecapUseCase()
    private val geminiService = GeminiAiService()
    private val bluetoothManager = MockBluetoothFingerprintManagerImpl()

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.MAIN)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // UI States
    private val _selectedMode = MutableStateFlow(ModeAbsensi.MANUAL)
    val selectedMode: StateFlow<ModeAbsensi> = _selectedMode.asStateFlow()

    private val _isBluetoothConnected = MutableStateFlow(false)
    val isBluetoothConnected: StateFlow<Boolean> = _isBluetoothConnected.asStateFlow()

    private val _bluetoothDeviceName = MutableStateFlow("ESP32-Fingerprint-01")
    val bluetoothDeviceName: StateFlow<String> = _bluetoothDeviceName.asStateFlow()

    private val _jamaahList = MutableStateFlow(jamaahRepo.getAll())
    val jamaahList: StateFlow<List<Jamaah>> = _jamaahList.asStateFlow()

    val attendanceLogs: StateFlow<List<LogAbsensi>> = attendanceRepo.attendanceLogs

    private val _latestAttendanceEvent = MutableStateFlow<LogAbsensi?>(null)
    val latestAttendanceEvent: StateFlow<LogAbsensi?> = _latestAttendanceEvent.asStateFlow()

    private val _isManageJamaahOpen = MutableStateFlow(false)
    val isManageJamaahOpen: StateFlow<Boolean> = _isManageJamaahOpen.asStateFlow()

    private val _editingLog = MutableStateFlow<LogAbsensi?>(null)
    val editingLog: StateFlow<LogAbsensi?> = _editingLog.asStateFlow()

    // Jadwal Shalat Hari Ini
    val jadwalHariIni = JadwalShalatHariIni(
        tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
        mapWaktu = mapOf(
            WaktuShalat.SUBUH to "04:30",
            WaktuShalat.DZUHUR to "12:00",
            WaktuShalat.ASHAR to "15:15",
            WaktuShalat.MAGHRIB to "18:00",
            WaktuShalat.ISYA to "19:15"
        )
    )

    init {
        try {
            val initialLog = recordManualUseCase.execute("JM-001", WaktuShalat.DZUHUR, jadwalHariIni, System.currentTimeMillis())
            _latestAttendanceEvent.value = initialLog
        } catch (_: Exception) {}
    }

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
    }

    fun setMode(mode: ModeAbsensi) {
        _selectedMode.value = mode
        if (mode == ModeAbsensi.AUTOMATIC_FINGERPRINT && !_isBluetoothConnected.value) {
            toggleBluetoothConnection()
        }
    }

    fun toggleBluetoothConnection() {
        viewModelScope.launch {
            if (_isBluetoothConnected.value) {
                bluetoothManager.disconnect()
                _isBluetoothConnected.value = false
            } else {
                val success = bluetoothManager.connect("00:11:22:33:44:55")
                _isBluetoothConnected.value = success
            }
        }
    }

    fun addJamaah(nama: String, fingerprintId: String?) {
        try {
            val newId = "JM-${String.format("%03d", jamaahRepo.getAll().size + 1)}"
            addJamaahUseCase.execute(id = newId, nama = nama, fingerprintId = fingerprintId.takeIf { !it.isNullOrBlank() })
            _jamaahList.value = jamaahRepo.getAll()
        } catch (e: Exception) {
            throw e
        }
    }

    fun removeJamaah(id: String) {
        val list = jamaahRepo.getAll().toMutableList()
        list.removeAll { it.id == id }
        val updated = InMemoryJamaahRepository(list)
        _jamaahList.value = updated.getAll()
    }

    fun recordManual(jamaahId: String, waktuShalat: WaktuShalat) {
        val log = recordManualUseCase.execute(jamaahId, waktuShalat, jadwalHariIni, System.currentTimeMillis())
        _latestAttendanceEvent.value = log
    }

    fun simulateFingerprint(fingerprintId: String) {
        viewModelScope.launch {
            try {
                if (!bluetoothManager.isConnected) {
                    bluetoothManager.connect("00:11:22:33:44:55")
                    _isBluetoothConnected.value = true
                }
                val event = FingerprintScanEvent(rawFingerprintId = fingerprintId, timestamp = System.currentTimeMillis())
                val log = processFingerprintUseCase.execute(event, jadwalHariIni)
                _latestAttendanceEvent.value = log
            } catch (_: Exception) {}
        }
    }

    fun setEditingLog(log: LogAbsensi?) {
        _editingLog.value = log
    }

    fun updateLog(logId: String, newStatus: StatusAbsensi, newPrayer: WaktuShalat) {
        val logs = attendanceRepo.getAllLogs().toMutableList()
        val index = logs.indexOfFirst { it.id == logId }
        if (index != -1) {
            val old = logs[index]
            logs[index] = old.copy(status = newStatus, waktuShalat = newPrayer)
            attendanceRepo.clear()
            logs.forEach { attendanceRepo.saveLog(it) }
        }
        _editingLog.value = null
    }

    fun deleteLog(logId: String) {
        val logs = attendanceRepo.getAllLogs().toMutableList()
        logs.removeAll { it.id == logId }
        attendanceRepo.clear()
        logs.forEach { attendanceRepo.saveLog(it) }
    }

    fun setManageJamaahOpen(isOpen: Boolean) {
        _isManageJamaahOpen.value = isOpen
    }

    fun getWeeklyRecap(): List<WeeklyRecapItem> {
        return weeklyRecapUseCase.execute(jamaahRepo.getAll(), attendanceRepo.getAllLogs())
    }

    fun setGeminiApiKey(key: String) {
        geminiService.setApiKey(key)
    }

    suspend fun askAi(prompt: String): String {
        val jamaahs = jamaahRepo.getAll()
        val logs = attendanceRepo.getAllLogs()
        val recap = getWeeklyRecap()

        val contextStr = buildString {
            append("Daftar Jamaah (${jamaahs.size}):\n")
            jamaahs.forEach { append("- ${it.nama} (ID: ${it.id}, FP: ${it.fingerprintId})\n") }
            append("\nLog Absensi Terbaru (${logs.size}):\n")
            logs.forEach { append("- ${it.namaJamaah} | Shalat: ${it.waktuShalat} | Status: ${it.status} | Mode: ${it.mode}\n") }
            append("\nRekap Mingguan:\n")
            recap.forEach { append("- ${it.nama}: Hadir ${it.hadirCount}x, Alfa ${it.alfaCount}x\n") }
        }

        return geminiService.askGemini(prompt, contextStr)
    }
}
