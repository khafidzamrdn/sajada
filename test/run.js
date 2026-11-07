import { InMemoryJamaahRepository } from '../src/infrastructure/memory/InMemoryJamaahRepository.js';
import { InMemoryAttendanceRepository } from '../src/infrastructure/memory/InMemoryAttendanceRepository.js';
import { PrayerTimeCalculator } from '../src/domain/services/PrayerTimeCalculator.js';
import { AttendanceProcessor } from '../src/domain/services/AttendanceProcessor.js';
import { BluetoothFingerprintService } from '../src/infrastructure/bluetooth/BluetoothFingerprintService.js';

async function runTests() {
  console.log('=== MEMULAI PENGUJIAN KODE & ARSITEKTUR ABSENSI JAMAAH 5 WAKTU ===\n');

  // 1. Setup Repositories dengan Mock Data
  const initialJamaah = [
    { id: 'J001', name: 'Ahmad Fauzi', fingerprintId: 'FP_001', phoneNumber: '081234567890' },
    { id: 'J002', name: 'Siti Aminah', fingerprintId: 'FP_002', phoneNumber: '081234567891' },
    { id: 'J003', name: 'Umar Faruq', fingerprintId: 'FP_003', phoneNumber: '081234567892' }
  ];

  const jamaahRepo = new InMemoryJamaahRepository(initialJamaah);
  const attendanceRepo = new InMemoryAttendanceRepository();
  const prayerCalculator = new PrayerTimeCalculator({
    Subuh: '04:30',
    Dzuhur: '12:00',
    Ashar: '15:15',
    Maghrib: '18:00',
    Isya: '19:15'
  });

  const attendanceProcessor = new AttendanceProcessor({
    jamaahRepo,
    attendanceRepo,
    prayerTimeCalculator: prayerCalculator
  });

  // Base date for testing: Today
  const testDate = new Date();
  const schedule = prayerCalculator.getScheduleForDate(testDate);
  const dzuhurTime = schedule['Dzuhur'];

  console.log('--- TEST 1: Prayer Time Status Calculation Logic ---');
  // Helper to create timestamp offset in minutes from Dzuhur
  const getDzuhurOffset = (offsetMinutes) => {
    return new Date(dzuhurTime.getTime() + offsetMinutes * 60 * 1000);
  };

  const status1 = prayerCalculator.determineStatus('Dzuhur', getDzuhurOffset(2), schedule);
  console.log(`+ Dzuhur + 2 mins -> Status: "${status1}" (Expected: Tepat Waktu)`);
  assert(status1 === 'Tepat Waktu', 'Status should be Tepat Waktu');

  const status2 = prayerCalculator.determineStatus('Dzuhur', getDzuhurOffset(25), schedule);
  console.log(`+ Dzuhur + 25 mins -> Status: "${status2}" (Expected: Terlambat)`);
  assert(status2 === 'Terlambat', 'Status should be Terlambat');

  const status3 = prayerCalculator.determineStatus('Dzuhur', getDzuhurOffset(60), schedule);
  console.log(`+ Dzuhur + 60 mins -> Status: "${status3}" (Expected: Di Luar Jam Shalat)`);
  assert(status3 === 'Di Luar Jam Shalat', 'Status should be Di Luar Jam Shalat');
  console.log('✓ Test 1 Passed Successfully!\n');


  console.log('--- TEST 2: Manual Attendance Processing ---');
  const manualTimestamp = getDzuhurOffset(5); // 5 mins after Dzuhur
  const manualLog = attendanceProcessor.processManualAttendance('J001', 'Dzuhur', manualTimestamp);
  console.log(`+ Manual Attendance Recorded:`, manualLog);
  assert(manualLog.jamaahId === 'J001', 'Jamaah ID should match');
  assert(manualLog.mode === 'MANUAL', 'Mode should be MANUAL');
  assert(manualLog.status === 'Tepat Waktu', 'Status should be Tepat Waktu');
  console.log('✓ Test 2 Passed Successfully!\n');


  console.log('--- TEST 3: Automatic Attendance via Bluetooth Fingerprint Service & Simulator ---');
  const btService = new BluetoothFingerprintService('ESP32-Fingerprint-Scanner');
  
  // Wire up Bluetooth event stream to AttendanceProcessor
  btService.onDataReceived((payload) => {
    try {
      const autoLog = attendanceProcessor.processAutomaticAttendance(payload);
      console.log(`-> [Automatic Processor Success] Log Created:`, autoLog);
    } catch (err) {
      console.error(`-> [Automatic Processor Error]`, err.message);
    }
  });

  await btService.connect();

  // Simulate fingerprint scan for Siti Aminah (FP_002) at Dzuhur + 10 mins (Tepat Waktu)
  console.log('Simulating scan for Siti Aminah (FP_002) at Dzuhur + 10 mins...');
  btService.simulateIncomingScan('FP_002', getDzuhurOffset(10));

  // Simulate fingerprint scan for Umar Faruq (FP_003) at Dzuhur + 30 mins (Terlambat)
  console.log('Simulating scan for Umar Faruq (FP_003) at Dzuhur + 30 mins...');
  btService.simulateIncomingScan('FP_003', getDzuhurOffset(30));

  await btService.disconnect();
  console.log('✓ Test 3 Passed Successfully!\n');


  console.log('--- TEST 4: Verify Attendance Repository Logs ---');
  const allLogs = attendanceRepo.findAll();
  console.log(`Total attendance logs recorded: ${allLogs.length}`);
  allLogs.forEach((log, idx) => {
    const jamaah = jamaahRepo.findById(log.jamaahId);
    console.log(`  [${idx + 1}] Jamaah: ${jamaah.name} (${log.jamaahId}) | Prayer: ${log.prayerName} | Status: ${log.status} | Mode: ${log.mode}`);
  });

  assert(allLogs.length === 3, 'There should be exactly 3 attendance logs');
  console.log('✓ Test 4 Passed Successfully!\n');

  console.log('=== SEMUA PENGUJIAN BERHASIL DIJALANKAN DENGAN 100% SUKSES! ===');
}

function assert(condition, message) {
  if (!condition) {
    throw new Error(`Assertion Failed: ${message}`);
  }
}

runTests().catch(err => {
  console.error('Test execution failed:', err);
  process.exit(1);
});
