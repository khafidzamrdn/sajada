package com.sajada.app.domain.usecase

import com.sajada.app.domain.model.Jamaah
import com.sajada.app.domain.model.LogAbsensi
import com.sajada.app.domain.model.StatusAbsensi

data class WeeklyRecapItem(
    val jamaahId: String,
    val nama: String,
    val hadirCount: Int,
    val alfaCount: Int,
    val totalExpected: Int
)

class GenerateWeeklyRecapUseCase {

    fun execute(jamaahList: List<Jamaah>, allLogs: List<LogAbsensi>, totalDays: Int = 7): List<WeeklyRecapItem> {
        val totalExpectedSlots = totalDays * 5 // 5 shalat per hari

        return jamaahList.map { jamaah ->
            val jamaahLogs = allLogs.filter { it.jamaahId == jamaah.id }
            val hadirCount = jamaahLogs.count { it.status == StatusAbsensi.TEPAT_WAKTU || it.status == StatusAbsensi.TERLAMBAT }
            val alfaCount = maxOf(0, totalExpectedSlots - hadirCount)

            WeeklyRecapItem(
                jamaahId = jamaah.id,
                nama = jamaah.nama,
                hadirCount = hadirCount,
                alfaCount = alfaCount,
                totalExpected = totalExpectedSlots
            )
        }
    }
}
