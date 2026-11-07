package com.sajada.app.presentation

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.sajada.app.data.repository.InMemoryAttendanceRepository
import com.sajada.app.data.repository.InMemoryJamaahRepository
import com.sajada.app.domain.model.JadwalShalatHariIni
import com.sajada.app.domain.model.Jamaah
import com.sajada.app.domain.model.WaktuShalat
import com.sajada.app.domain.usecase.AttendanceEvaluatorUseCase
import com.sajada.app.domain.usecase.RecordManualAttendanceUseCase
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val jamaahRepo = InMemoryJamaahRepository(
        listOf(
            Jamaah("JM-001", "Ahmad Fauzi", "FP-1001"),
            Jamaah("JM-002", "Siti Aminah", "FP-1002"),
            Jamaah("JM-003", "Umar Faruq", "FP-1003")
        )
    )
    private val attendanceRepo = InMemoryAttendanceRepository()
    private val evaluator = AttendanceEvaluatorUseCase()
    private val manualUseCase = RecordManualAttendanceUseCase(jamaahRepo, attendanceRepo, evaluator)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Simple programmatic UI for initial prototype
        val textView = TextView(this).apply {
            textSize = 18f
            setPadding(32, 64, 32, 32)
        }

        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Calendar.getInstance().time)
        val jadwal = JadwalShalatHariIni(
            tanggal = todayStr,
            mapWaktu = mapOf(
                WaktuShalat.SUBUH to "04:30",
                WaktuShalat.DZUHUR to "12:00",
                WaktuShalat.ASHAR to "15:15",
                WaktuShalat.MAGHRIB to "18:00",
                WaktuShalat.ISYA to "19:15"
            )
        )

        // Simulate a manual attendance record on startup
        try {
            manualUseCase.execute("JM-001", WaktuShalat.DZUHUR, jadwal, System.currentTimeMillis())
        } catch (e: Exception) {
            // ignore
        }

        val logs = attendanceRepo.getAllLogs()
        val jamaahs = jamaahRepo.getAll()

        val sb = StringBuilder()
        sb.append("🕌 APLIKASI ABSENSI JAMAAH 5 WAKTU\n\n")
        sb.append("📅 Tanggal: $todayStr\n\n")
        sb.append("👥 Total Jamaah Terdaftar: ${jamaahs.size}\n")
        jamaahs.forEach { sb.append(" - ${it.nama} (FP: ${it.fingerprintId})\n") }
        sb.append("\n📋 Log Absensi Hari Ini (${logs.size}):\n")
        if (logs.isEmpty()) {
            sb.append(" (Belum ada absensi tercatat)\n")
        } else {
            logs.forEach { log ->
                sb.append(" • ${log.namaJamaah} | ${log.waktuShalat} | ${log.status} (${log.mode})\n")
            }
        }
        sb.append("\n✅ Status: Engine Berjalan Normal (Siap Bluetooth Fingerprint SDK)")

        textView.text = sb.toString()
        setContentView(textView)
    }
}
