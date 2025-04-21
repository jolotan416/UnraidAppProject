package com.jolotan.unraidapp.data.api

enum class BackendQuery(val queryString: String) {
    ConnectionCheck(
        "info {" +
                "  id  " +
                "}"
    ),
    Dashboard(
                "info {" +
                "  cpu {" +
                "    cores  " +
                "    brand  " +
                "  }" +
                "}"
    )
}