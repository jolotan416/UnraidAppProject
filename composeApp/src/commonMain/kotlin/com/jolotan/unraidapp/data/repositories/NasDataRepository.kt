package com.jolotan.unraidapp.data.repositories

import com.jolotan.unraidapp.data.api.QueryApi
import com.jolotan.unraidapp.data.datasource.NasConnectionDataSource
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.models.NasConnectionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val IP_ADDRESS_DELIMITER = "."
private const val DEFAULT_BROADCAST_IP_ADDRESS_HOST_NUMBER = "255"

interface NasDataRepository {
    // Only expose 1 connection data at a time
    fun getNasConnectionDataFlow(): Flow<NasConnectionData?>
    suspend fun connectToNas(ipAddress: String, apiKey: String)
    suspend fun wakeOnLan(nasConnectionData: NasConnectionData)
}

class NasDataRepositoryImpl(
    private val nasConnectionDataSource: NasConnectionDataSource,
    private val udpSocketDataSource: UdpSocketDataSource,
    private val queryApi: QueryApi,
) : NasDataRepository {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        ioScope.launch {
            nasConnectionDataSource.getNasConnectionDataListFlow()
                .collect { nasConnectionDataList ->
                    nasConnectionDataList.firstOrNull()?.run {
                        queryApi.updateCommonData(ipAddress, apiKey)
                    }
                }
        }
    }

    override fun getNasConnectionDataFlow(): Flow<NasConnectionData?> =
        nasConnectionDataSource.getNasConnectionDataListFlow()
            .map { nasConnectionDataList -> nasConnectionDataList.firstOrNull() }

    override suspend fun connectToNas(ipAddress: String, apiKey: String) {
        val currentNasConnectionDataWithIpAddress =
            nasConnectionDataSource.getNasConnectionDataListFlow()
                .firstOrNull()
                ?.find { it.ipAddress == ipAddress }
        if (currentNasConnectionDataWithIpAddress == null) {
            nasConnectionDataSource.createNasConnectionData(
                NasConnectionData(
                    ipAddress = ipAddress,
                    apiKey = apiKey,
                    broadcastIpAddress = ipAddress.getDefaultBroadcastIpAddress()
                )
            )
        }

        println("initial query: ${queryApi.queryDashboardData()}")
    }

    override suspend fun wakeOnLan(nasConnectionData: NasConnectionData) {
        nasConnectionDataSource.updateNasConnectionData(nasConnectionData)
        nasConnectionData.apply {
            udpSocketDataSource.sendWakeOnLanPacket(
                macAddress ?: error("Mac address is null."),
                broadcastIpAddress,
                wakeOnLanPort
            )
        }
    }

    private fun String.getDefaultBroadcastIpAddress(): String {
        val ipAddressNetworkAddress = trim().split(IP_ADDRESS_DELIMITER)
            .run { subList(0, lastIndex) }

        return (ipAddressNetworkAddress + listOf(DEFAULT_BROADCAST_IP_ADDRESS_HOST_NUMBER))
            .joinToString(IP_ADDRESS_DELIMITER)
    }
}