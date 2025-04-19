package com.jolotan.unraidapp.data.api

import com.jolotan.unraidapp.data.models.backend.BackendData
import com.jolotan.unraidapp.data.models.backend.DashboardData
import com.jolotan.unraidapp.ui.utils.InternalLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.StringValues

private const val TAG = "QueryApi"

interface QueryApi {
    fun updateCommonData(ipAddress: String, apiKey: String)
    suspend fun queryDashboardData(): BackendData<DashboardData>
}

class QueryApiImpl(private val httpClient: HttpClient) : QueryApi {
    private var baseUrl: String = ""
    private var apiKey: String = ""
    private var queryReferer: String = ""

    override fun updateCommonData(ipAddress: String, apiKey: String) {
        this.baseUrl = "http://$ipAddress"
        this.apiKey = apiKey
    }

    override suspend fun queryDashboardData(): BackendData<DashboardData> = performPostQuery(
        "graphql",
        BackendQuery.Dashboard.queryString
    ).body<BackendData<DashboardData>>()

    private suspend fun performPostQuery(queryPath: String, queryBodyString: String): HttpResponse {
        val queryUrl = "$baseUrl/$queryPath"
        InternalLog.d(tag = TAG, message = "Performing post query: $queryUrl")
        val response =
            httpClient.post(queryUrl) {
                contentType(ContentType.Application.Json)
                headers.apply {
                    appendAll(StringValues.build {
                        append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                        append(
                            "x-api-key",
                            apiKey
                        )
                        append(
                            "Origin",
                            baseUrl
                        )
                        if (queryReferer.isNotBlank()) {
                            append("Referer", "$queryReferer/graphql")
                        }
                    })
                }

                setBody("{\"query\":\"$queryBodyString\"}")
            }

        return when (response.status) {
            HttpStatusCode.OK -> response
            HttpStatusCode.Found -> {
                queryReferer = baseUrl
                baseUrl = response.headers["Location"]?.removeSuffix("/$queryPath")
                    ?: error("New location is not found after encountering status code 302.")
                performPostQuery(queryPath, queryBodyString)
            }

            else -> error("Encountered status code with: ${response.status}")
        }
    }
}