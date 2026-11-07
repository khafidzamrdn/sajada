package com.sajada.app.domain.usecase

import com.sajada.app.domain.model.Jamaah
import com.sajada.app.domain.repository.JamaahRepository

class AddJamaahUseCase(
    private val jamaahRepository: JamaahRepository
) {

    fun execute(id: String, nama: String, fingerprintId: String? = null, phoneNumber: String? = null): Jamaah {
        // Validasi dasar
        require(id.isNotBlank()) { "ID Jamaah tidak boleh kosong." }
        require(nama.isNotBlank()) { "Nama Jamaah tidak boleh kosong." }

        // Cek apakah ID sudah terdaftar
        if (jamaahRepository.getById(id) != null) {
            throw IllegalArgumentException("Jamaah dengan ID '$id' sudah terdaftar.")
        }

        // Cek apakah Fingerprint ID sudah digunakan (jika diisi)
        if (!fingerprintId.isNullOrBlank()) {
            val existingByFp = jamaahRepository.getByFingerprintId(fingerprintId)
            if (existingByFp != null) {
                throw IllegalArgumentException("Fingerprint ID '$fingerprintId' sudah terdaftar atas nama ${existingByFp.nama}.")
            }
        }

        val newJamaah = Jamaah(
            id = id,
            nama = nama,
            fingerprintId = fingerprintId
        )

        jamaahRepository.save(newJamaah)
        return newJamaah
    }
}
