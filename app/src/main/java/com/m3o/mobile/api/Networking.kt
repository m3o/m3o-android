package com.m3o.mobile.api

import android.content.Context
import com.cyb3rko.m3okotlin.CustomError
import com.m3o.mobile.utils.logE
import com.m3o.mobile.utils.showErrorDialog
import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

object Networking {
    private const val BASE_URL = "https://api.m3o.com/"
    private lateinit var authorization: Pair<String, String>
    internal lateinit var ktorHttpClient: HttpClient
    internal lateinit var ktorHttpAuthClient: HttpClient

    internal fun initialize(context: Context) {
        ktorHttpClient = HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }

            installResponseValidator(context)
        }
    }

    internal fun initializeAuth(context: Context, accessToken: String) {
        authorization = "Authorization" to "Bearer $accessToken"
        ktorHttpAuthClient = HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                    prettyPrint = true
                    ignoreUnknownKeys = true
                })
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                header(HttpHeaders.Authorization, authorization.second)
            }

            installResponseValidator(context)
        }
    }

    private fun HttpClientConfig<*>.installResponseValidator(context: Context) {
        HttpResponseValidator {
            handleResponseException {
                it.printStackTrace()
                val clientException = it as? ClientRequestException
                val serverException: ServerResponseException?

                val response: String
                if (clientException != null) {
                    response = clientException.response.readText()
                } else {
                    serverException = it as? ServerResponseException
                    response = serverException?.response?.readText() ?: "No error information"
                }
                logE("Ktor/Serialization error - $response")

                try {
                    val errorInformation = Json.decodeFromString<CustomError>(response)
                    val title = errorInformation.status
                    val message = when (errorInformation.code) {
                        HttpStatusCode.Unauthorized.value -> "Your API Key may be invalid"
                        else -> errorInformation.detail
                    }
                    context.showErrorDialog(title, message)
                } catch (_: Exception) {
                    context.showErrorDialog(message = response)
                }
            }
        }
    }

    fun getUrl(tail: String) = "$BASE_URL$tail"
}