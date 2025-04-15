package com.jolotan.unraidapp.data.api

enum class BackendQuery(val queryString: String) {
    Dashboard(
        "query {info {cpu {cores brand}}}"
    )
}