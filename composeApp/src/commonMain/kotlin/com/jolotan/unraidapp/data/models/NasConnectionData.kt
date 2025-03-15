package com.jolotan.unraidapp.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey

internal const val DEFAULT_WAKE_ON_LAN_PORT = 9

@Entity(tableName = "nasConnection")
data class NasConnectionData(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ipAddress: String,
    val macAddress: String? = null,
    val wakeOnLanPort: Int = DEFAULT_WAKE_ON_LAN_PORT,
)
