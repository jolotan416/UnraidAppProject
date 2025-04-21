package com.jolotan.unraidapp.data.models.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ConnectionCheckData(
    @SerialName("info")
    val connectionInfo: ConnectionInfo
)

@Serializable
data class ConnectionInfo(
    @SerialName("id")
    val id: String
)