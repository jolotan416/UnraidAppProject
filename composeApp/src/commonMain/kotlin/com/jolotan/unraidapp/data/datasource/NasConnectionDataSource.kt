package com.jolotan.unraidapp.data.datasource

import com.jolotan.unraidapp.data.models.NasConnectionData
import kotlinx.coroutines.flow.Flow

interface NasConnectionDataSource {
    fun getNasConnectionDataListFlow(): Flow<List<NasConnectionData>>
    suspend fun createNasConnectionData(nasConnectionData: NasConnectionData)
    suspend fun updateNasConnectionData(nasConnectionData: NasConnectionData)
}

class NasConnectionDataSourceImpl(private val nasConnectionDao: NasConnectionDao) :
    NasConnectionDataSource {
    override fun getNasConnectionDataListFlow(): Flow<List<NasConnectionData>> =
        nasConnectionDao.getNasConnectionDataListFlow()

    override suspend fun createNasConnectionData(nasConnectionData: NasConnectionData) {
        nasConnectionDao.insertNasConnection(nasConnectionData)
    }

    override suspend fun updateNasConnectionData(nasConnectionData: NasConnectionData) {
        nasConnectionDao.updateNasConnectionData(nasConnectionData)
    }
}