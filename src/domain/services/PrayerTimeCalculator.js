export class PrayerTimeCalculator {
  constructor(defaultSchedule = null) {
    // Default standard prayer times (HH:MM format)
    this.defaultTimes = defaultSchedule || {
      Subuh: '04:30',
      Dzuhur: '12:00',
      Ashar: '15:15',
      Maghrib: '18:00',
      Isya: '19:15'
    };
  }

  /**
   * Get prayer schedule for a specific date
   * @param {Date} date 
   * @returns {Object} { Subuh: Date, Dzuhur: Date, ... }
   */
  getScheduleForDate(date) {
    const year = date.getFullYear();
    const month = date.getMonth();
    const day = date.getDate();

    const schedule = {};
    for (const [prayer, timeStr] of Object.entries(this.defaultTimes)) {
      const [hours, minutes] = timeStr.split(':').map(Number);
      const prayerDate = new Date(year, month, day, hours, minutes, 0, 0);
      schedule[prayer] = prayerDate;
    }
    return schedule;
  }

  /**
   * Determine attendance status for a specific prayer and timestamp
   * Rules:
   * - Tepat Waktu: 0 to 15 minutes after prayer time
   * - Terlambat: 16 to 45 minutes after prayer time
   * - Di Luar Jam Shalat: before prayer time or > 45 minutes after prayer time
   * 
   * @param {string} prayerName 
   * @param {Date} timestamp 
   * @param {Object} schedule 
   * @returns {string} Status ("Tepat Waktu" | "Terlambat" | "Di Luar Jam Shalat")
   */
  determineStatus(prayerName, timestamp, schedule) {
    const prayerTime = schedule[prayerName];
    if (!prayerTime) {
      return 'Di Luar Jam Shalat';
    }

    const diffMinutes = (timestamp.getTime() - prayerTime.getTime()) / (1000 * 60);

    // Can allow early check-in up to 10 mins before prayer as "Tepat Waktu"
    if (diffMinutes >= -10 && diffMinutes <= 15) {
      return 'Tepat Waktu';
    } else if (diffMinutes > 15 && diffMinutes <= 45) {
      return 'Terlambat';
    } else {
      return 'Di Luar Jam Shalat';
    }
  }

  /**
   * Find which prayer window a given timestamp falls into (for automatic mode)
   * Window duration: -10 mins to +45 mins from prayer time
   * @param {Date} timestamp 
   * @param {Object} schedule 
   * @returns {Object|null} { prayerName, status } or null
   */
  findActivePrayerForTimestamp(timestamp, schedule) {
    for (const [prayerName, prayerTime] of Object.entries(schedule)) {
      const diffMinutes = (timestamp.getTime() - prayerTime.getTime()) / (1000 * 60);

      // Active window: -10 mins to +45 mins
      if (diffMinutes >= -10 && diffMinutes <= 45) {
        let status = 'Tepat Waktu';
        if (diffMinutes > 15) {
          status = 'Terlambat';
        }
        return { prayerName, status };
      }
    }
    return null;
  }
}
