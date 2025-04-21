package com.jolotan.unraidapp.data.models.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    @SerialName("info")
    val dashboardSystemData: DashboardSystemData
)

@Serializable
data class DashboardSystemData(
    @SerialName("cpu")
    val cpuData: CpuData
)

@Serializable
data class CpuData(
    val cores: Int,
    val brand: String,
)