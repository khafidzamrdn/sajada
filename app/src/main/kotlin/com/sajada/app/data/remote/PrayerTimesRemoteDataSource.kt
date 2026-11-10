package com.sajada.app.data.remote

import com.sajada.app.domain.model.JadwalShalatHariIni
import com.sajada.app.domain.model.WaktuShalat
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerTimesRemoteDataSource {

    data class Result(
        val jadwal: JadwalShalatHariIni,
        val city: String,
        val source: String
    )

    /**
     * Mengambil jadwal shalat dari Aladhan API (https://aladhan.com/prayer-times-api).
     * method=20 -> Kementerian Agama Republik Indonesia.
     */
    fun fetch(city: String = "Jakarta", country: String = "Indonesia"): Result {
        val encodedCity = URLEncoder.encode(city, "UTF-8")
        val encodedCountry = URLEncoder.encode(country, "UTF-8")
        val endpoint = "https://api.aladhan.com/v1/timingsByCity" +
            "?city=$encodedCity&country=$encodedCountry&method=20"

        val connection = (URL(endpoint).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 10_000
            readTimeout = 10_000
            setRequestProperty("Accept", "application/json")
        }

        try {
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                throw IllegalStateException("HTTP $responseCode")
            }

            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(body)
            val data = json.getJSONObject("data")
            val timings = data.getJSONObject("timings")

            fun readTime(key: String): String =
                timings.getString(key).substringBefore(" ").trim()

            val mapWaktu = mapOf(
                WaktuShalat.SUBUH to readTime("Fajr"),
                WaktuShalat.DZUHUR to readTime("Dhuhr"),
                WaktuShalat.ASHAR to readTime("Asr"),
                WaktuShalat.MAGHRIB to readTime("Maghrib"),
                WaktuShalat.ISYA to readTime("Isha")
            )

            val tanggal = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            return Result(
                jadwal = JadwalShalatHariIni(tanggal = tanggal, mapWaktu = mapWaktu),
                city = city,
                source = "Aladhan API"
            )
        } finally {
            connection.disconnect()
        }
    }
}
