package com.jolotan.unraidapp.data.datasource

import com.jolotan.unraidapp.data.models.NasConnectionData
import com.jolotan.unraidapp.ui.utils.InternalLog
import kotlinx.coroutines.flow.Flow

private const val TAG = "NasConnectionLocalDataSource"

interface NasConnectionLocalDataSource {
    fun getNasConnectionDataListFlow(): Flow<List<NasConnectionData>>
    suspend fun createNasConnectionData(nasConnectionData: NasConnectionData): NasConnectionData
    suspend fun updateNasConnectionData(nasConnectionData: NasConnectionData)
}

class NasConnectionLocalDataSourceImpl(private val nasConnectionDao: NasConnectionDao) :
    NasConnectionLocalDataSource {
    override fun getNasConnectionDataListFlow(): Flow<List<NasConnectionData>> =
        nasConnectionDao.getNasConnectionDataListFlow()

    override suspend fun createNasConnectionData(nasConnectionData: NasConnectionData): NasConnectionData {
        InternalLog.d(tag = TAG, message = "Creating NasConnectionData: $nasConnectionData")
        val nasConnectionDataIndex = nasConnectionDao.insertNasConnection(nasConnectionData)

        return nasConnectionData.copy(id = nasConnectionDataIndex)
    }

    override suspend fun updateNasConnectionData(nasConnectionData: NasConnectionData) {
        InternalLog.d(tag = TAG, message = "Updating NasConnectionData: $nasConnectionData")
        nasConnectionDao.updateNasConnectionData(nasConnectionData)
    }
}