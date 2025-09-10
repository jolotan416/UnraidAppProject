package com.jolotan.unraidapp.data.models.backend

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DashboardData(
    @SerialName("server")
    val serverData: DashboardServerData,

    @SerialName("registration")
    val registrationData: DashboardRegistrationData,

    @SerialName("array")
    val arrayData: DashboardArrayData,

    @SerialName("shares")
    val sharesData: List<DashboardShareData>
)

@Serializable
data class DashboardServerData(
    @SerialName("name")
    val name: String
)

@Serializable
data class DashboardRegistrationData(
    @SerialName("type")
    val registrationType: String,

    @SerialName("updateExpiration")
    val updateExpiration: String
)

@Serializable
data class DashboardArrayData(
    @SerialName("capacity")
    val arrayCapacityData: DashboardArrayCapacityData,

    @SerialName("parities")
    val parityDisks: List<DashboardDiskData>,

    @SerialName("disks")
    val disks: List<DashboardDiskData>,
)

@Serializable
data class DashboardArrayCapacityData(
    @SerialName("kilobytes")
    val arrayDiskSpaceSize: DashboardArraySizeData,

    @SerialName("disks")
    val arrayDiskSlotsSize: DashboardArraySizeData
)

@Serializable
data class DashboardArraySizeData(
    @SerialName("free")
    val free: String,

    @SerialName("total")
    val total: String,

    @SerialName("used")
    val used: String
)

@Serializable
data class DashboardDiskData(
    @SerialName("name")
    val name: String,

    @SerialName("status")
    val status: String,

    @SerialName("temp")
    val temperature: Int,

    @SerialName("size")
    val size: String,
)

@Serializable
data class DashboardShareData(
    @SerialName("name")
    val name: String,

    @SerialName("used")
    val used: String,

    @SerialName("free")
    val free: String
)