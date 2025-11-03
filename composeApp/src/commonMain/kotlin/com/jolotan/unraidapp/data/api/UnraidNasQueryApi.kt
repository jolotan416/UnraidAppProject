package com.jolotan.unraidapp.data.api

import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.NasConnectionData
import com.jolotan.unraidapp.data.models.backend.BackendData
import com.jolotan.unraidapp.data.models.backend.ConnectionCheckData
import com.jolotan.unraidapp.data.models.backend.DashboardData
import com.jolotan.unraidapp.ui.utils.InternalLog
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.JsonConvertException
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.io.IOException

private const val TAG = "QueryApi"
private const val GRAPH_QL_PATH = "graphql"

interface UnraidNasQueryApi {
    val currentNasConnectionDataFlow: Flow<NasConnectionData>
    suspend fun configureNasConnectionData(nasConnectionData: NasConnectionData)
    suspend fun checkConnection(): GenericState<ConnectionCheckData, BackendConnectionError>
    suspend fun queryDashboardData(): GenericState<DashboardData, BackendConnectionError>
}

class UnraidNasQueryApiImpl(private val httpClient: HttpClient) : UnraidNasQueryApi {
    private val _currentNasConnectionDataFlow: MutableSharedFlow<NasConnectionData> =
        MutableSharedFlow(replay = 1)

    override val currentNasConnectionDataFlow: Flow<NasConnectionData>
        get() = _currentNasConnectionDataFlow.distinctUntilChanged()

    override suspend fun configureNasConnectionData(nasConnectionData: NasConnectionData) {
        _currentNasConnectionDataFlow.emit(nasConnectionData)
    }

    override suspend fun checkConnection(): GenericState<ConnectionCheckData, BackendConnectionError> =
        handlePostQueryState(GRAPH_QL_PATH, BackendQuery.ConnectionCheck.queryBodyString)

    override suspend fun queryDashboardData(): GenericState<DashboardData, BackendConnectionError> =
        handlePostQueryState(GRAPH_QL_PATH, BackendQuery.Dashboard.queryBodyString)

    private suspend inline fun <reified T> handlePostQueryState(
        queryPath: String,
        queryBodyString: String
    ): GenericState<T, BackendConnectionError> =
        try {
            val result = performPostQuery(queryPath, queryBodyString).body<BackendData<T>>()

            GenericState.Loaded(result.data)
        } catch (exception: IOException) {
            GenericState.Error(BackendConnectionError.ConnectionError)
        } catch (exception: JsonConvertException) {
            GenericState.Error(BackendConnectionError.ParsingError)
        } catch (exception: IllegalStateException) {
            GenericState.Error(BackendConnectionError.InternalError)
        }

    private suspend fun performPostQuery(queryPath: String, queryBodyString: String): HttpResponse =
        _currentNasConnectionDataFlow.first().run {
            val queryUrl = "$baseUrl/$queryPath"
            InternalLog.d(tag = TAG, message = "Performing post query: $queryUrl")

            try {
                val response =
                    httpClient.post(queryUrl) {
                        configureCommonHeaders(this@run)
                        setBody(queryBodyString)
                    }

                when (response.status) {
                    HttpStatusCode.OK -> response
                    HttpStatusCode.Found -> {
                        val updatedBaseUrl =
                            response.headers["Location"]?.removeSuffix("/$queryPath")
                                ?: error("New location is not found after encountering status code 302.")
                        _currentNasConnectionDataFlow.emit(copy(baseUrl = updatedBaseUrl))
                        performPostQuery(queryPath, queryBodyString)
                    }

                    else -> error("Encountered status code with: ${response.status}")
                }
            } catch (exception: IllegalArgumentException) {
                error("Encountered exception: $exception")
            }

        }

    private fun HttpRequestBuilder.configureCommonHeaders(nasConnectionData: NasConnectionData) {
        contentType(ContentType.Application.Json)
        headers.apply {
            appendAll(StringValues.build {
                append(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                append(
                    "x-api-key",
                    nasConnectionData.apiKey
                )
                append(
                    "Origin",
                    nasConnectionData.baseUrl
                )
                if (nasConnectionData.baseUrl != nasConnectionData.ipAddress) {
                    append("Referer", "${nasConnectionData.ipAddress}/graphql")
                }
            })
        }
    }

    private val BackendQuery.queryBodyString: String
        get() = "{\"query\":\"query { $queryString } \"}"
}