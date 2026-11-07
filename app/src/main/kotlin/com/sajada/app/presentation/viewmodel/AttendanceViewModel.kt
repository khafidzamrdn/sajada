package com.sajada.app.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sajada.app.data.bluetooth.MockBluetoothFingerprintManagerImpl
import com.sajada.app.data.repository.InMemoryAttendanceRepository
import com.sajada.app.data.repository.InMemoryJamaahRepository
import com.sajada.app.domain.model.*
import com.sajada.app.domain.usecase.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

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
    private val bluetoothManager = MockBluetoothFingerprintManagerImpl()

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
            recordManualUseCase.execute("JM-001", WaktuShalat.DZUHUR, jadwalHariIni, System.currentTimeMillis())
        } catch (_: Exception) {}
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
        recordManualUseCase.execute(jamaahId, waktuShalat, jadwalHariIni, System.currentTimeMillis())
    }

    fun simulateFingerprint(fingerprintId: String) {
        viewModelScope.launch {
            try {
                if (!bluetoothManager.isConnected) {
                    bluetoothManager.connect("00:11:22:33:44:55")
                    _isBluetoothConnected.value = true
                }
                val event = FingerprintScanEvent(rawFingerprintId = fingerprintId, timestamp = System.currentTimeMillis())
                processFingerprintUseCase.execute(event, jadwalHariIni)
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
}
