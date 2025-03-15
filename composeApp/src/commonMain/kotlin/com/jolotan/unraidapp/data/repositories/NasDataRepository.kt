package com.jolotan.unraidapp.data.repositories

import com.jolotan.unraidapp.data.datasource.NasConnectionDataSource
import com.jolotan.unraidapp.data.models.NasConnectionData
import kotlinx.coroutines.flow.Flow

interface NasDataRepository {
    fun getNasConnectionDataFlow(): Flow<List<NasConnectionData>>
    suspend fun connectToNas(ipAddress: String)
}

class NasDataRepositoryImpl(private val nasConnectionDataSource: NasConnectionDataSource) :
    NasDataRepository {
    override fun getNasConnectionDataFlow(): Flow<List<NasConnectionData>> =
        nasConnectionDataSource.getNasConnectionDataFlow()

    override suspend fun connectToNas(ipAddress: String) {
        nasConnectionDataSource.createNasConnectionData(NasConnectionData(ipAddress = ipAddress))
    }
}