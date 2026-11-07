export class InMemoryAttendanceRepository {
  constructor() {
    this.logs = [];
  }

  save(log) {
    this.logs.push(log);
    return log;
  }

  findAll() {
    return [...this.logs];
  }

  findByJamaahId(jamaahId) {
    return this.logs.filter(log => log.jamaahId === jamaahId);
  }

  findByDate(date) {
    const targetStr = date.toDateString();
    return this.logs.filter(log => log.timestamp.toDateString() === targetStr);
  }

  clear() {
    this.logs = [];
  }
}
