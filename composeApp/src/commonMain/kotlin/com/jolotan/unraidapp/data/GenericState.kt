package com.jolotan.unraidapp.data

sealed class GenericState<out Value, out Error> {
    data object Loading : GenericState<Nothing, Nothing>()
    data class Loaded<out Value>(val value: Value) : GenericState<Value, Nothing>()
    data class Error<out Error>(val error: Error) : GenericState<Nothing, Error>()
}