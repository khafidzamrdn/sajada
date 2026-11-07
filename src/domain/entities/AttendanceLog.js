export class AttendanceLog {
  constructor({ id, jamaahId, prayerName, timestamp, status, mode }) {
    this.id = id || `LOG-${Date.now()}-${Math.random().toString(36).substr(2, 4)}`;
    this.jamaahId = jamaahId;
    this.prayerName = prayerName; // Subuh, Dzuhur, Ashar, Maghrib, Isya
    this.timestamp = timestamp instanceof Date ? timestamp : new Date(timestamp);
    this.status = status; // "Tepat Waktu", "Terlambat", "Di Luar Jam Shalat"
    this.mode = mode; // "MANUAL", "AUTOMATIC"
    this.createdAt = new Date();
  }
}
