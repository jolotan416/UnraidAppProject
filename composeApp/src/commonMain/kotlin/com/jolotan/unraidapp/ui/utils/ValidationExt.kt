package com.jolotan.unraidapp.ui.utils

private val MAC_ADDRESS_REGEX = "^((\\d|[A-Fa-f]){2}(:(?!\$)|\$)){6}\$".toRegex()
private val IP_ADDRESS_REGEX = "^((25[0-5]|(2[0-4]|1\\d|[1-9]|)\\d)(\\.(?!\$)|\$)){4}\$".toRegex()

fun String.isValidIpAddress(): Boolean = IP_ADDRESS_REGEX.matches(this)
fun String.isValidMacAddress(): Boolean = MAC_ADDRESS_REGEX.matches(this)