package com.m3o.mobile.api

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import io.ktor.http.*

object Networking {
    private const val BASE_URL = "https://api.m3o.com/"
    private lateinit var authorization: Pair<String, String>
    internal lateinit var ktorHttpClient: HttpClient
    internal lateinit var ktorHttpAuthClient: HttpClient

    internal fun initialize() {
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
        }
    }

    internal fun initializeAuth(accessToken: String) {
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
        }
    }

    fun getUrl(tail: String) = "$BASE_URL$tail"
}