package com.sajada.app.domain.model

data class JadwalShalatHariIni(
    val tanggal: String, // format yyyy-MM-dd
    val mapWaktu: Map<WaktuShalat, String> // format "HH:mm"
)
