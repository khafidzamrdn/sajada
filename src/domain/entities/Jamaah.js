export class Jamaah {
  constructor({ id, name, fingerprintId, phoneNumber = '' }) {
    this.id = id;
    this.name = name;
    this.fingerprintId = fingerprintId;
    this.phoneNumber = phoneNumber;
    this.createdAt = new Date();
  }
}
