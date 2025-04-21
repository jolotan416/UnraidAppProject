package com.jolotan.unraidapp.data.repositories

import com.jolotan.unraidapp.data.api.UnraidNasQueryApi
import com.jolotan.unraidapp.data.datasource.NasConnectionLocalDataSource
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.models.NasConnectionData
import com.jolotan.unraidapp.ui.utils.InternalLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private const val TAG = "NasDataRepository"
private const val IP_ADDRESS_DELIMITER = "."
private const val DEFAULT_BROADCAST_IP_ADDRESS_HOST_NUMBER = "255"

interface NasDataRepository {
    // Only expose 1 connection data at a time
    fun getNasConnectionDataFlow(): Flow<NasConnectionData?>
    suspend fun connectToNas(ipAddress: String, apiKey: String)
    suspend fun wakeOnLan(nasConnectionData: NasConnectionData)
}

class NasDataRepositoryImpl(
    private val nasConnectionLocalDataSource: NasConnectionLocalDataSource,
    private val udpSocketDataSource: UdpSocketDataSource,
    private val queryApi: UnraidNasQueryApi,
) : NasDataRepository {
    private val ioScope = CoroutineScope(Dispatchers.IO)

    init {
        observeNasConnectionDataList()
        observeApiCurrentNasConnectionData()
    }

    override fun getNasConnectionDataFlow(): Flow<NasConnectionData?> =
        nasConnectionLocalDataSource.getNasConnectionDataListFlow()
            .map { nasConnectionDataList -> nasConnectionDataList.firstOrNull() }

    override suspend fun connectToNas(ipAddress: String, apiKey: String) {
        var currentNasConnectionDataWithIpAddress =
            nasConnectionLocalDataSource.getNasConnectionDataListFlow()
                .firstOrNull()
                ?.find { it.ipAddress == ipAddress }
        if (currentNasConnectionDataWithIpAddress == null) {
            currentNasConnectionDataWithIpAddress =
                nasConnectionLocalDataSource.createNasConnectionData(
                    NasConnectionData(
                        ipAddress = ipAddress,
                        apiKey = apiKey,
                        broadcastIpAddress = ipAddress.getDefaultBroadcastIpAddress()
                    )
                )
        } else {
            currentNasConnectionDataWithIpAddress =
                currentNasConnectionDataWithIpAddress.copy(apiKey = apiKey)
            nasConnectionLocalDataSource.updateNasConnectionData(
                currentNasConnectionDataWithIpAddress
            )
        }
        queryApi.configureNasConnectionData(currentNasConnectionDataWithIpAddress)

        InternalLog.d(tag = TAG, message = "initial query: ${queryApi.queryDashboardData()}")
    }

    override suspend fun wakeOnLan(nasConnectionData: NasConnectionData) {
        nasConnectionLocalDataSource.updateNasConnectionData(nasConnectionData)
        nasConnectionData.apply {
            udpSocketDataSource.sendWakeOnLanPacket(
                macAddress ?: error("Mac address is null."),
                broadcastIpAddress,
                wakeOnLanPort
            )
        }
    }

    private fun observeNasConnectionDataList() {
        ioScope.launch {
            nasConnectionLocalDataSource.getNasConnectionDataListFlow()
                .collect { nasConnectionDataList ->
                    InternalLog.d(
                        tag = TAG,
                        message = "NasConnectionDataList updated: $nasConnectionDataList"
                    )
                    nasConnectionDataList.firstOrNull()?.run {
                        queryApi.configureNasConnectionData(this)
                    }
                }
        }
    }

    private fun observeApiCurrentNasConnectionData() {
        ioScope.launch {
            queryApi.currentNasConnectionDataFlow
                .collect { nasConnectionData ->
                    InternalLog.d(
                        tag = TAG,
                        message = "NasConnectionData from API updated: $nasConnectionData"
                    )
                    nasConnectionLocalDataSource.updateNasConnectionData(nasConnectionData)
                }
        }
    }

    private fun String.getDefaultBroadcastIpAddress(): String {
        val ipAddressNetworkAddress = trim().split(IP_ADDRESS_DELIMITER)
            .run { subList(0, lastIndex) }

        return (ipAddressNetworkAddress + listOf(DEFAULT_BROADCAST_IP_ADDRESS_HOST_NUMBER))
            .joinToString(IP_ADDRESS_DELIMITER)
    }
}