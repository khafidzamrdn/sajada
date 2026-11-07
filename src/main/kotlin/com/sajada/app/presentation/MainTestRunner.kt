package com.sajada.app.presentation

import com.sajada.app.data.bluetooth.MockBluetoothFingerprintManagerImpl
import com.sajada.app.data.repository.InMemoryAttendanceRepository
import com.sajada.app.data.repository.InMemoryJamaahRepository
import com.sajada.app.domain.model.JadwalShalatHariIni
import com.sajada.app.domain.model.Jamaah
import com.sajada.app.domain.model.WaktuShalat
import com.sajada.app.domain.usecase.AttendanceEvaluatorUseCase
import com.sajada.app.domain.usecase.ProcessFingerprintScanUseCase
import com.sajada.app.domain.usecase.RecordManualAttendanceUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

fun main() = runBlocking {
    println("========================================================")
    println("  ANDROID APP ARCHITECTURE & CORE LOGIC: ABSENSI JAMAAH  ")
    println("========================================================\n")

    // 1. Inisialisasi Repositori In-Memory & Mock Data Jamaah
    val initialJamaahs = listOf(
        Jamaah(id = "JM-001", nama = "Ahmad Fauzi (Admin)", fingerprintId = "FP-1001"),
        Jamaah(id = "JM-002", nama = "Siti Aminah (Jamaah)", fingerprintId = "FP-1002"),
        Jamaah(id = "JM-003", nama = "Umar Faruq (Jamaah)", fingerprintId = "FP-1003")
    )
    val jamaahRepository = InMemoryJamaahRepository(initialJamaahs)
    val attendanceRepository = InMemoryAttendanceRepository()

    // 2. Inisialisasi Use Cases & Evaluator
    val evaluatorUseCase = AttendanceEvaluatorUseCase()
    val recordManualUseCase = RecordManualAttendanceUseCase(jamaahRepository, attendanceRepository, evaluatorUseCase)
    val processFingerprintUseCase = ProcessFingerprintScanUseCase(jamaahRepository, attendanceRepository, evaluatorUseCase)

    // 3. Inisialisasi Jadwal Shalat Hari Ini (Misal: Dzuhur pukul 12:00)
    val todayDateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
    val jadwalHariIni = JadwalShalatHariIni(
        tanggal = todayDateStr,
        mapWaktu = mapOf(
            WaktuShalat.SUBUH to "04:30",
            WaktuShalat.DZUHUR to "12:00",
            WaktuShalat.ASHAR to "15:15",
            WaktuShalat.MAGHRIB to "18:00",
            WaktuShalat.ISYA to "19:15"
        )
    )

    println("[Setup] Jadwal Shalat Hari Ini ($todayDateStr):")
    jadwalHariIni.mapWaktu.forEach { (waktu, time) ->
        println("  - $waktu: $time")
    }
    println()

    // Helper untuk membuat timestamp relatif dari waktu Dzuhur (12:00)
    fun getDzuhurEpochWithOffset(offsetMinutes: Int): Long {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 12)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            add(Calendar.MINUTE, offsetMinutes)
        }
        return cal.timeInMillis
    }

    // --- A. Eksekusi Absensi MANUAL untuk Jamaah A (Ahmad Fauzi - JM-001) ---
    println("--- SCENARIO A: ABSENSI MANUAL (Admin Input) ---")
    val manualTimestamp = getDzuhurEpochWithOffset(5) // Dzuhur + 5 menit (Tepat Waktu)
    val manualLog = recordManualUseCase.execute(
        jamaahId = "JM-001",
        waktuShalat = WaktuShalat.DZUHUR,
        jadwalHariIni = jadwalHariIni,
        timestamp = manualTimestamp
    )
    println("✓ Manual Attendance Success:")
    println("  -> Jamaah  : ${manualLog.namaJamaah} (ID: ${manualLog.jamaahId})")
    println("  -> Waktu   : ${manualLog.waktuShalat}")
    println("  -> Status  : ${manualLog.status}")
    println("  -> Mode    : ${manualLog.mode}")
    println()

    // --- B & C. Eksekusi Simulasi Scan FINGERPRINT AUTOMATIC via Bluetooth Driver ---
    println("--- SCENARIO B & C: ABSENSI OTOMATIS FINGERPRINT BLUETOOTH ---")
    val bluetoothManager = MockBluetoothFingerprintManagerImpl()

    // Hubungkan Bluetooth Device
    bluetoothManager.connect("00:11:22:33:44:55")

    // Observe Bluetooth Scan Events (Coroutines Flow Collector)
    val job = CoroutineScope(Dispatchers.Default).launch {
        bluetoothManager.listenScanEvents().collect { scanEvent ->
            try {
                val autoLog = processFingerprintScanUseCase.execute(scanEvent, jadwalHariIni)
                println("-> [Processor Event] Auto Attendance Recorded:")
                println("     * Nama   : ${autoLog.namaJamaah} (ID: ${autoLog.jamaahId})")
                println("     * Shalat : ${autoLog.waktuShalat}")
                println("     * Status : ${autoLog.status}")
                println("     * Mode   : ${autoLog.mode}")
            } catch (e: Exception) {
                println("-> [Processor Error] ${e.message}")
            }
        }
    }

    // Simulasi Scan 1: Siti Aminah (FP-1002) pada Dzuhur + 12 menit (Tepat Waktu)
    println("\n[Simulasi Hardware] Siti Aminah melakukan scan jari...")
    bluetoothManager.simulateFingerprintScan("FP-1002", getDzuhurEpochWithOffset(12))

    // Simulasi Scan 2: Umar Faruq (FP-1003) pada Dzuhur + 25 menit (Terlambat)
    println("\n[Simulasi Hardware] Umar Faruq melakukan scan jari...")
    bluetoothManager.simulateFingerprintScan("FP-1003", getDzuhurEpochWithOffset(25))

    // Beri sedikit jeda untuk coroutine event processing lalu putus koneksi
    kotlinx.coroutines.delay(100)
    job.cancel()
    bluetoothManager.disconnect()
    println()

    // --- D. Rekapitulasi Log Absensi Akhir ---
    println("--- D. REKAPITULASI LOG ABSENSI TERAKHIR (In-Memory Repository) ---")
    val allLogs = attendanceRepository.getAllLogs()
    println("Total Log Tercatat: ${allLogs.size}")
    allLogs.forEachIndexed { index, log ->
        println("  [${index + 1}] ${log.namaJamaah} | ${log.waktuShalat} | Status: ${log.status} | Mode: ${log.mode}")
    }

    println("\n========================================================")
    println("  SEMUA SKENARIO ARSITEKTUR KOTLIN BERHASIL DIEKSEKUSI!  ")
    println("========================================================")
}
