package com.sajada.app.domain.model

data class FingerprintScanEvent(
    val rawFingerprintId: String,
    val timestamp: Long
)
