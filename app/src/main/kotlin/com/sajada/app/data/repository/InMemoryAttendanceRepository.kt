package com.sajada.app.data.repository

import com.sajada.app.domain.model.LogAbsensi
import com.sajada.app.domain.repository.AttendanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class InMemoryAttendanceRepository : AttendanceRepository {

    private val _attendanceLogs = MutableStateFlow<List<LogAbsensi>>(emptyList())
    override val attendanceLogs: StateFlow<List<LogAbsensi>> = _attendanceLogs.asStateFlow()

    override fun saveLog(log: LogAbsensi) {
        _attendanceLogs.update { currentList ->
            currentList + log
        }
    }

    override fun getAllLogs(): List<LogAbsensi> {
        return _attendanceLogs.value
    }

    override fun getLogsByJamaahId(jamaahId: String): List<LogAbsensi> {
        return _attendanceLogs.value.filter { it.jamaahId == jamaahId }
    }

    fun clear() {
        _attendanceLogs.value = emptyList()
    }
}
