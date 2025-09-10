package com.jolotan.unraidapp.data.api

enum class BackendQuery(val queryString: String) {
    ConnectionCheck(
        "info {" +
                "  id  " +
                "}"
    ),
    Dashboard(
        "server {" +
                "  name" +
                "}" +
                "registration {" +
                "  type" +
                "  updateExpiration" +
                "}" +
                "array {" +
                "  capacity {" +
                "    kilobytes {" +
                "      free" +
                "      total" +
                "      used" +
                "    }" +
                "    disks {" +
                "      free" +
                "      total" +
                "      used" +
                "    }" +
                "  }" +
                "  parities {" +
                "    name" +
                "    status" +
                "    temp" +
                "    size" +
                "  }" +
                "  disks {" +
                "    name" +
                "    status" +
                "    temp" +
                "    size" +
                "  }" +
                "}" +
                "shares {" +
                "  name" +
                "  used" +
                "  free" +
                "}"
    )
}