package com.sajada.app.data.bluetooth

import com.sajada.app.domain.model.FingerprintScanEvent
import kotlinx.coroutines.flow.Flow

interface BluetoothFingerprintManager {
    suspend fun connect(deviceAddress: String): Boolean
    suspend fun disconnect(): Boolean
    fun listenScanEvents(): Flow<FingerprintScanEvent>
}
