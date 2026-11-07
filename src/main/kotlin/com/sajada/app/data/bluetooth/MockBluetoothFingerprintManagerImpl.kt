package com.sajada.app.data.bluetooth

import com.sajada.app.domain.model.FingerprintScanEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class MockBluetoothFingerprintManagerImpl : BluetoothFingerprintManager {

    private val _scanEvents = MutableSharedFlow<FingerprintScanEvent>(replay = 0)
    var isConnected: Boolean = false
        private set

    override suspend fun connect(deviceAddress: String): Boolean {
        println("[Bluetooth Mock Driver] Connecting to Bluetooth Fingerprint Device at $deviceAddress...")
        kotlinx.coroutines.delay(50)
        isConnected = true
        println("[Bluetooth Mock Driver] Connected successfully to device: $deviceAddress")
        return true
    }

    override suspend fun disconnect(): Boolean {
        println("[Bluetooth Mock Driver] Disconnecting Bluetooth device...")
        kotlinx.coroutines.delay(30)
        isConnected = false
        println("[Bluetooth Mock Driver] Disconnected successfully.")
        return true
    }

    override fun listenScanEvents(): Flow<FingerprintScanEvent> {
        return _scanEvents.asSharedFlow()
    }

    /**
     * Simulator function to trigger hardware fingerprint scan without physical hardware.
     */
    suspend fun simulateFingerprintScan(fingerprintId: String, timestamp: Long = System.currentTimeMillis()) {
        if (!isConnected) {
            throw IllegalStateException("Cannot simulate scan: Bluetooth device is not connected.")
        }
        val event = FingerprintScanEvent(rawFingerprintId = fingerprintId, timestamp = timestamp)
        println("[Bluetooth Hardware Simulator] Scan detected! Raw ID: '$fingerprintId', Timestamp: $timestamp")
        _scanEvents.emit(event)
    }
}
