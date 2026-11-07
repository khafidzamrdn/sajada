export class BluetoothFingerprintService {
  constructor(deviceName = 'Mock-Fingerprint-Device') {
    this.deviceName = deviceName;
    this.isConnected = false;
    this.dataCallback = null;
  }

  async connect() {
    // Simulate Bluetooth connection handshake
    console.log(`[Bluetooth] Connecting to ${this.deviceName}...`);
    await new Promise(resolve => setTimeout(resolve, 50));
    this.isConnected = true;
    console.log(`[Bluetooth] Connected successfully to ${this.deviceName}`);
    return true;
  }

  async disconnect() {
    console.log(`[Bluetooth] Disconnecting from ${this.deviceName}...`);
    this.isConnected = false;
    console.log(`[Bluetooth] Disconnected.`);
    return true;
  }

  /**
   * Register callback when fingerprint data is received from device
   * @param {Function} callback - receives { fingerprintId, timestamp }
   */
  onDataReceived(callback) {
    if (typeof callback !== 'function') {
      throw new Error('Callback must be a function');
    }
    this.dataCallback = callback;
  }

  /**
   * Simulator method to simulate incoming hardware payload
   * @param {string} fingerprintId 
   * @param {Date} timestamp 
   */
  simulateIncomingScan(fingerprintId, timestamp = new Date()) {
    if (!this.isConnected) {
      throw new Error('Cannot receive data: Bluetooth device is not connected.');
    }
    if (this.dataCallback) {
      console.log(`[Bluetooth Hardware Event] Raw payload received: { fingerprintId: "${fingerprintId}", timestamp: "${timestamp.toISOString()}" }`);
      this.dataCallback({ fingerprintId, timestamp });
    } else {
      console.warn('[Bluetooth] Data received but no callback registered.');
    }
  }
}
