package com.jolotan.unraidapp.data.datasource

import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.utils.io.core.buildPacket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.io.IOException

interface UdpSocketDataSource {
    suspend fun sendWakeOnLanPacket(macAddress: String, ipAddress: String, port: Int)
}

class UdpSocketDataSourceImpl : UdpSocketDataSource {
    companion object {
        private const val MAC_ADDRESS_SEPARATOR = ":"
        private const val HEXADECIMAL_RADIX = 16

        private const val MAGIC_PACKET_INITIAL_BYTE = 0xFF.toByte()
        private val MAGIC_PACKET_INITIAL_BYTE_ARRAY = byteArrayOf(
            MAGIC_PACKET_INITIAL_BYTE,
            MAGIC_PACKET_INITIAL_BYTE,
            MAGIC_PACKET_INITIAL_BYTE,
            MAGIC_PACKET_INITIAL_BYTE,
            MAGIC_PACKET_INITIAL_BYTE,
            MAGIC_PACKET_INITIAL_BYTE
        )
        private const val MAGIC_PACKET_MAC_ADDRESS_REPETITION = 16
    }

    override suspend fun sendWakeOnLanPacket(
        macAddress: String,
        ipAddress: String,
        port: Int
    ) {
        try {
            val macAddressBytes = macAddress.convertToMacAddressBytes()
            val socketAddress = InetSocketAddress(ipAddress, port)
            val selectorManager = SelectorManager(Dispatchers.IO)
            val socket = aSocket(selectorManager).udp().bind { broadcast = true }
            val packet = buildPacket {
                write(MAGIC_PACKET_INITIAL_BYTE_ARRAY)
                repeat(MAGIC_PACKET_MAC_ADDRESS_REPETITION) {
                    write(macAddressBytes)
                }
            }

            println("Sending packet: $packet to socket address: $socketAddress...")
            socket.send(Datagram(packet, socketAddress))
        } catch (exception: IOException) {
            println("Encountered exception while sending WOL packet: ${exception.stackTraceToString()}")
        }
    }

    private fun String.convertToMacAddressBytes(): ByteArray =
        trim()
            .split(MAC_ADDRESS_SEPARATOR)
            .map {
                it.toInt(HEXADECIMAL_RADIX)
                    .toByte()
            }.toByteArray()
}