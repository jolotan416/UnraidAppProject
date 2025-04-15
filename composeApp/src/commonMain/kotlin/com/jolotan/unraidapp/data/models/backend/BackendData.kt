package com.jolotan.unraidapp.data.models.backend

import kotlinx.serialization.Serializable

@Serializable
data class BackendData<T>(val data: T)
