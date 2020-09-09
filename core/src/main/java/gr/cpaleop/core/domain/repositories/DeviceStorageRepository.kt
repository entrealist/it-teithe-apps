package gr.cpaleop.core.domain.repositories

import gr.cpaleop.core.domain.entities.Document

interface DeviceStorageRepository {

    suspend fun saveFile(fileName: String, fileData: ByteArray)

    suspend fun getLocalDocuments(): List<Document>
}