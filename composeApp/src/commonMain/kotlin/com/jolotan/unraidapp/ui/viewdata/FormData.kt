package com.jolotan.unraidapp.ui.viewdata

data class FormData<T>(
    val value: T,
    val isValid: Boolean = true,
    val isPreviouslyUpdated: Boolean = false,
)
