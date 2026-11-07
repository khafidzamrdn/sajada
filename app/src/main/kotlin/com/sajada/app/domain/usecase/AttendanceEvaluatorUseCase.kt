package com.sajada.app.domain.usecase

import com.sajada.app.domain.model.StatusAbsensi
import com.sajada.app.domain.model.WaktuShalat
import java.util.Calendar
import java.util.TimeZone

class AttendanceEvaluatorUseCase {

    /**
     * Evaluates whether the incoming timestamp falls into TEPAT_WAKTU, TERLAMBAT, or DI_LUAR_JADWAL
     * based on prayer time string ("HH:mm") and target timestamp.
     */
    fun evaluate(prayerTimeStr: String, timestamp: Long): StatusAbsensi {
        val prayerMillis = parseTimeToMillisToday(prayerTimeStr, timestamp)
        val diffMinutes = (timestamp - prayerMillis) / (1000 * 60)

        return when {
            // Window: -15 minutes before prayer up to +15 minutes after prayer
            diffMinutes >= -15 && diffMinutes <= 15 -> StatusAbsensi.TEPAT_WAKTU
            // Window: +16 minutes up to +30 minutes after prayer
            diffMinutes > 15 && diffMinutes <= 30 -> StatusAbsensi.TERLAMBAT
            // Outside valid attendance window
            else -> StatusAbsensi.DI_LUAR_JADWAL
        }
    }

    /**
     * Determines which WaktuShalat window matches the given timestamp from a schedule map.
     */
    fun findClosestPrayer(scheduleMap: Map<WaktuShalat, String>, timestamp: Long): Pair<WaktuShalat, StatusAbsensi>? {
        var bestMatch: Pair<WaktuShalat, StatusAbsensi>? = null
        var minAbsDiffMinutes = Long.MAX_VALUE

        for ((waktu, timeStr) in scheduleMap) {
            val prayerMillis = parseTimeToMillisToday(timeStr, timestamp)
            val diffMinutes = (timestamp - prayerMillis) / (1000 * 60)
            val absDiff = Math.abs(diffMinutes)

            // Active window check: -15 mins to +30 mins
            if (diffMinutes >= -15 && diffMinutes <= 30) {
                val status = if (diffMinutes <= 15) StatusAbsensi.TEPAT_WAKTU else StatusAbsensi.TERLAMBAT
                return Pair(waktu, status) // Immediate valid active prayer found
            }

            // Track closest prayer if outside immediate active window but within reasonable range
            if (absDiff < minAbsDiffMinutes) {
                minAbsDiffMinutes = absDiff
                val status = when {
                    diffMinutes < -15 -> StatusAbsensi.DI_LUAR_JADWAL
                    diffMinutes > 30 -> StatusAbsensi.DI_LUAR_JADWAL
                    else -> StatusAbsensi.DI_LUAR_JADWAL
                }
                bestMatch = Pair(waktu, status)
            }
        }

        // If within 60 mins of closest prayer, return it with DI_LUAR_JADWAL or let caller decide
        return if (minAbsDiffMinutes <= 60 && bestMatch != null) {
            Pair(bestMatch.first, StatusAbsensi.DI_LUAR_JADWAL)
        } else {
            null
        }
    }

    private fun parseTimeToMillisToday(timeStr: String, referenceTimestamp: Long): Long {
        val parts = timeStr.split(":")
        val hours = parts[0].toInt()
        val minutes = parts[1].toInt()

        val calendar = Calendar.getInstance(TimeZone.getDefault()).apply {
            timeInMillis = referenceTimestamp
            set(Calendar.HOUR_OF_DAY, hours)
            set(Calendar.MINUTE, minutes)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}
