package com.sajada.app.domain.usecase

import com.sajada.app.domain.model.FingerprintScanEvent
import com.sajada.app.domain.model.JadwalShalatHariIni
import com.sajada.app.domain.model.LogAbsensi
import com.sajada.app.domain.model.ModeAbsensi
import com.sajada.app.domain.model.StatusAbsensi
import com.sajada.app.domain.model.WaktuShalat
import com.sajada.app.domain.repository.AttendanceRepository
import com.sajada.app.domain.repository.JamaahRepository
import java.util.UUID

class ProcessFingerprintScanUseCase(
    private val jamaahRepository: JamaahRepository,
    private val attendanceRepository: AttendanceRepository,
    private val evaluatorUseCase: AttendanceEvaluatorUseCase
) {

    fun execute(
        event: FingerprintScanEvent,
        jadwalHariIni: JadwalShalatHariIni
    ): LogAbsensi {
        val jamaah = jamaahRepository.getByFingerprintId(event.rawFingerprintId)
            ?: throw IllegalArgumentException("Jamaah dengan Fingerprint ID '${event.rawFingerprintId}' tidak terdaftar.")

        // Automatically find matching prayer time window
        val match = evaluatorUseCase.findClosestPrayer(jadwalHariIni.mapWaktu, event.timestamp)

        val waktuShalat = match?.first ?: WaktuShalat.DZUHUR // Fallback default
        val status = match?.second ?: StatusAbsensi.DI_LUAR_JADWAL

        val log = LogAbsensi(
            id = "LOG-AUTO-${UUID.randomUUID().toString().take(6)}",
            jamaahId = jamaah.id,
            namaJamaah = jamaah.nama,
            waktuShalat = waktuShalat,
            timestamp = event.timestamp,
            status = status,
            mode = ModeAbsensi.AUTOMATIC_FINGERPRINT
        )

        attendanceRepository.saveLog(log)
        return log
    }
}
