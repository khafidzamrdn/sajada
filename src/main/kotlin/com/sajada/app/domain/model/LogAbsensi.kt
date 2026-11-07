package com.sajada.app.domain.model

data class LogAbsensi(
    val id: String,
    val jamaahId: String,
    val namaJamaah: String,
    val waktuShalat: WaktuShalat,
    val timestamp: Long,
    val status: StatusAbsensi,
    val mode: ModeAbsensi
)
