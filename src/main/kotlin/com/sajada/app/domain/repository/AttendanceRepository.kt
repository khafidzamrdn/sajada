package com.sajada.app.domain.repository

import com.sajada.app.domain.model.LogAbsensi
import kotlinx.coroutines.flow.StateFlow

interface AttendanceRepository {
    val attendanceLogs: StateFlow<List<LogAbsensi>>
    fun saveLog(log: LogAbsensi)
    fun getAllLogs(): List<LogAbsensi>
    fun getLogsByJamaahId(jamaahId: String): List<LogAbsensi>
}
