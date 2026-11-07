package com.sajada.app.data.repository

import com.sajada.app.domain.model.Jamaah
import com.sajada.app.domain.repository.JamaahRepository

class InMemoryJamaahRepository(
    initialJamaahList: List<Jamaah> = emptyList()
) : JamaahRepository {

    private val jamaahMap = mutableMapOf<String, Jamaah>()
    private val fingerprintIndex = mutableMapOf<String, String>() // fingerprintId -> jamaahId

    init {
        initialJamaahList.forEach { save(it) }
    }

    override fun getById(id: String): Jamaah? {
        return jamaahMap[id]
    }

    override fun getByFingerprintId(fingerprintId: String): Jamaah? {
        val jamaahId = fingerprintIndex[fingerprintId] ?: return null
        return jamaahMap[jamaahId]
    }

    override fun getAll(): List<Jamaah> {
        return jamaahMap.values.toList()
    }

    override fun save(jamaah: Jamaah) {
        jamaahMap[jamaah.id] = jamaah
        jamaah.fingerprintId?.let { fpId ->
            fingerprintIndex[fpId] = jamaah.id
        }
    }
}
