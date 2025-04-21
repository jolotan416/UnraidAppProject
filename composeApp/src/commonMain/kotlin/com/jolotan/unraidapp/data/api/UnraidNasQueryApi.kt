package com.jolotan.unraidapp.data.api

import com.jolotan.unraidapp.data.GenericState
import com.jolotan.unraidapp.data.models.NasConnectionData
import com.jolotan.unraidapp.data.models.backend.BackendData
import com.jolotan.unraidapp.data.models.backend.DashboardData
import com.jolotan.unraidapp.ui.utils.InternalLog
import io.ktor.client.HttpClient
import io.ktor.client.call.NoTransformationFoundException
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first

private const val TAG = "QueryApi"
private const val GRAPH_QL_PATH = "graphql"

interface UnraidNasQueryApi {
    val currentNasConnectionDataFlow: Flow<NasConnectionData>
    suspend fun configureNasConnectionData(nasConnectionData: NasConnectionData)
    suspend fun queryDashboardData(): GenericState<BackendData<DashboardData>, Exception>
}

class UnraidNasQueryApiImpl(private val httpClient: HttpClient) : UnraidNasQueryApi {
    private val _currentNasConnectionDataFlow: MutableSharedFlow<NasConnectionData> =
        MutableSharedFlow(replay = 1)

    override val currentNasConnectionDataFlow: Flow<NasConnectionData>
        get() = _currentNasConnectionDataFlow.distinctUntilChanged()

    override suspend fun configureNasConnectionData(nasConnectionData: NasConnectionData) {
        _currentNasConnectionDataFlow.emit(nasConnectionData)
    }

    override suspend fun queryDashboardData(): GenericState<BackendData<DashboardData>, Exception> =
        handlePostQueryState(GRAPH_QL_PATH, BackendQuery.Dashboard.queryString)

    private suspend inline fun <reified T> handlePostQueryState(
        queryPath: String,
        queryBodyString: String
    ): GenericState<T, Exception> =
        try {
            val result = performPostQuery(queryPath, queryBodyString).body<T>()

            GenericState.Loaded(result)
        } catch (exception: IllegalStateException) {
            GenericState.Error(exception)
        } catch (exception: NoTransformationFoundException) {
            GenericState.Error(exception)
        }

    private suspend fun performPostQuery(queryPath: String, queryBodyString: String): HttpResponse =
        _currentNasConnectionDataFlow.first().run {
            val queryUrl = "$baseUrl/$queryPath"
            InternalLog.d(tag = TAG, message = "Performing post query: $queryUrl")
            val response =
                httpClient.post(queryUrl) {
                    configureCommonHeaders(this@run)
                    setBody("{\"query\":\"$queryBodyString\"}")
                }

            when (response.status) {
                HttpStatusCode.OK -> response
                HttpStatusCode.Found -> {
                    val updatedBaseUrl = response.headers["Location"]?.removeSuffix("/$queryPath")
                        ?: error("New location is not found after encountering status code 302.")
                    _currentNasConnectionDataFlow.emit(copy(baseUrl = updatedBaseUrl))
                    performPostQuery(queryPath, queryBodyString)
                }

                else -> error("Encountered status code with: ${response.status}")
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
}