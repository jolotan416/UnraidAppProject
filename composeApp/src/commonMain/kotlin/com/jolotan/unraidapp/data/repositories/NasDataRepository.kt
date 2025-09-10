package com.jolotan.unraidapp.data.repositories

import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.api.BackendConnectionError
import com.jolotan.unraidapp.data.api.UnraidNasQueryApi
import com.jolotan.unraidapp.data.datasource.NasConnectionLocalDataSource
import com.jolotan.unraidapp.data.datasource.UdpSocketDataSource
import com.jolotan.unraidapp.data.models.NasConnectionData
import com.jolotan.unraidapp.data.models.backend.ConnectionCheckData
import com.jolotan.unraidapp.ui.utils.InternalLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

private const val TAG = "NasDataRepository"
private const val IP_ADDRESS_DELIMITER = "."
private const val DEFAULT_BROADCAST_IP_ADDRESS_HOST_NUMBER = "255"
private const val WAKE_ON_LAN_RETRY_DELAY = 200L
private const val WAKE_ON_LAN_TIMEOUT = 30000L

interface NasDataRepository {
    // Only expose 1 connection data at a time
    fun getNasConnectionDataFlow(): Flow<NasConnectionData?>
    suspend fun connectToNas(
        ipAddress: String,
        apiKey: String
    ): GenericState<ConnectionCheckData, BackendConnectionError>

    suspend fun wakeOnLan(nasConnectionData: NasConnectionData): GenericState<ConnectionCheckData, BackendConnectionError>
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
            .map { nasConnectionDataList ->
                nasConnectionDataList.firstOrNull { it.isActive }
                    ?: nasConnectionDataList.firstOrNull()
            }
            .distinctUntilChanged()

    override suspend fun connectToNas(
        ipAddress: String,
        apiKey: String
    ): GenericState<ConnectionCheckData, BackendConnectionError> {
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

        nasConnectionLocalDataSource.getNasConnectionDataListFlow()
            .firstOrNull()
            ?.filter { it.ipAddress != ipAddress }
            ?.forEach { nasConnectionData ->
                InternalLog.d(
                    TAG,
                    "Deactivating connection data with ip address: ${nasConnectionData.ipAddress}"
                )
                nasConnectionLocalDataSource.updateNasConnectionData(nasConnectionData.copy(isActive = false))
            }

        val connectionCheckResult = queryApi.run {
            configureNasConnectionData(currentNasConnectionDataWithIpAddress)
            checkConnection()
        }
        InternalLog.d(tag = TAG, message = "Connection check result: $connectionCheckResult")

        return connectionCheckResult
    }

    override suspend fun wakeOnLan(nasConnectionData: NasConnectionData): GenericState<ConnectionCheckData, BackendConnectionError> {
        nasConnectionLocalDataSource.updateNasConnectionData(nasConnectionData)

        return nasConnectionData.run {
            val wakeOnLanResult = udpSocketDataSource.sendWakeOnLanPacket(
                macAddress ?: error("Mac address is null."),
                broadcastIpAddress,
                wakeOnLanPort
            )
            if (wakeOnLanResult is GenericState.Error) {
                return@run wakeOnLanResult
            }

            withTimeoutOrNull(WAKE_ON_LAN_TIMEOUT) {
                var result: GenericState<ConnectionCheckData, BackendConnectionError>
                do {
                    result = connectToNas(ipAddress, apiKey)
                    InternalLog.d(
                        tag = TAG,
                        message = "Connection result after wake on LAN retry: $result"
                    )
                    delay(WAKE_ON_LAN_RETRY_DELAY)
                } while (result !is GenericState.Loaded)

                result
            } ?: GenericState.Error(BackendConnectionError.ConnectionError)
        }
    }

    private fun observeNasConnectionDataList() {
        ioScope.launch {
            getNasConnectionDataFlow().collect { nasConnectionData ->
                InternalLog.d(
                    tag = TAG,
                    message = "Nas connection data updated: $nasConnectionData"
                )
                nasConnectionData?.run {
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