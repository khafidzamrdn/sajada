import { Jamaah } from '../../domain/entities/Jamaah.js';

export class InMemoryJamaahRepository {
  constructor(initialData = []) {
    this.jamaahs = new Map();
    this.fingerprintIndex = new Map(); // fingerprintId -> jamaahId

    // Seed initial mock data
    initialData.forEach(data => {
      const jamaah = new Jamaah(data);
      this.save(jamaah);
    });
  }

  save(jamaah) {
    this.jamaahs.set(jamaah.id, jamaah);
    if (jamaah.fingerprintId) {
      this.fingerprintIndex.set(jamaah.fingerprintId, jamaah.id);
    }
    return jamaah;
  }

  findById(id) {
    return this.jamaahs.get(id) || null;
  }

  findByFingerprintId(fingerprintId) {
    const jamaahId = this.fingerprintIndex.get(fingerprintId);
    if (!jamaahId) return null;
    return this.findById(jamaahId);
  }

  findAll() {
    return Array.from(this.jamaahs.values());
  }
}
