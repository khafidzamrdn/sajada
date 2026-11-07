package com.sajada.app.domain.usecase

import com.sajada.app.domain.model.JadwalShalatHariIni
import com.sajada.app.domain.model.LogAbsensi
import com.sajada.app.domain.model.ModeAbsensi
import com.sajada.app.domain.model.StatusAbsensi
import com.sajada.app.domain.model.WaktuShalat
import com.sajada.app.domain.repository.AttendanceRepository
import com.sajada.app.domain.repository.JamaahRepository
import java.util.UUID

class RecordManualAttendanceUseCase(
    private val jamaahRepository: JamaahRepository,
    private val attendanceRepository: AttendanceRepository,
    private val evaluatorUseCase: AttendanceEvaluatorUseCase
) {

    fun execute(
        jamaahId: String,
        waktuShalat: WaktuShalat,
        jadwalHariIni: JadwalShalatHariIni,
        timestamp: Long = System.currentTimeMillis()
    ): LogAbsensi {
        val jamaah = jamaahRepository.getById(jamaahId)
            ?: throw IllegalArgumentException("Jamaah dengan ID $jamaahId tidak ditemukan.")

        val timeStr = jadwalHariIni.mapWaktu[waktuShalat]
            ?: throw IllegalArgumentException("Jadwal untuk $waktuShalat tidak tersedia.")

        val status = evaluatorUseCase.evaluate(timeStr, timestamp)

        val log = LogAbsensi(
            id = "LOG-MANUAL-${UUID.randomUUID().toString().take(6)}",
            jamaahId = jamaah.id,
            namaJamaah = jamaah.nama,
            waktuShalat = waktuShalat,
            timestamp = timestamp,
            status = status,
            mode = ModeAbsensi.MANUAL
        )

        attendanceRepository.saveLog(log)
        return log
    }
}
