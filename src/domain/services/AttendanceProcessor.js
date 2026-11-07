import { AttendanceLog } from '../entities/AttendanceLog.js';

export class AttendanceProcessor {
  constructor({ jamaahRepo, attendanceRepo, prayerTimeCalculator }) {
    this.jamaahRepo = jamaahRepo;
    this.attendanceRepo = attendanceRepo;
    this.prayerTimeCalculator = prayerTimeCalculator;
  }

  /**
   * Process manual attendance by admin/pengurus
   * @param {string} jamaahId 
   * @param {string} prayerName 
   * @param {Date} timestamp 
   * @returns {AttendanceLog}
   */
  processManualAttendance(jamaahId, prayerName, timestamp = new Date()) {
    const jamaah = this.jamaahRepo.findById(jamaahId);
    if (!jamaah) {
      throw new Error(`Jamaah with ID ${jamaahId} not found.`);
    }

    const schedule = this.prayerTimeCalculator.getScheduleForDate(timestamp);
    const status = this.prayerTimeCalculator.determineStatus(prayerName, timestamp, schedule);

    const log = new AttendanceLog({
      jamaahId: jamaah.id,
      prayerName,
      timestamp,
      status,
      mode: 'MANUAL'
    });

    this.attendanceRepo.save(log);
    return log;
  }

  /**
   * Process automatic attendance payload from Bluetooth fingerprint scanner
   * @param {Object} payload - { fingerprintId, timestamp }
   * @returns {AttendanceLog}
   */
  processAutomaticAttendance(payload) {
    const { fingerprintId, timestamp = new Date() } = payload;

    const jamaah = this.jamaahRepo.findByFingerprintId(fingerprintId);
    if (!jamaah) {
      throw new Error(`Jamaah with Fingerprint ID "${fingerprintId}" not registered/found.`);
    }

    const schedule = this.prayerTimeCalculator.getScheduleForDate(timestamp);
    const activePrayerInfo = this.prayerTimeCalculator.findActivePrayerForTimestamp(timestamp, schedule);

    let prayerName = 'Unknown';
    let status = 'Di Luar Jam Shalat';

    if (activePrayerInfo) {
      prayerName = activePrayerInfo.prayerName;
      status = activePrayerInfo.status;
    } else {
      // If outside strict window, determine closest or default to nearest upcoming/recent prayer, or mark "Di Luar Jam Shalat"
      // For demonstration, let's pick the closest prayer or default to "Dzuhur" / "Di Luar Jam Shalat"
      prayerName = 'Di Luar Jadwal';
    }

    const log = new AttendanceLog({
      jamaahId: jamaah.id,
      prayerName,
      timestamp,
      status,
      mode: 'AUTOMATIC'
    });

    this.attendanceRepo.save(log);
    return log;
  }
}
