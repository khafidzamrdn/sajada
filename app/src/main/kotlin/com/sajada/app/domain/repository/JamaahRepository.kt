package com.sajada.app.domain.repository

import com.sajada.app.domain.model.Jamaah

interface JamaahRepository {
    fun getById(id: String): Jamaah?
    fun getByFingerprintId(fingerprintId: String): Jamaah?
    fun getAll(): List<Jamaah>
    fun save(jamaah: Jamaah)
}
