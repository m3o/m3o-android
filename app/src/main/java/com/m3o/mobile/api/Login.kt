package com.m3o.mobile.api

import com.m3o.mobile.api.Networking.getUrl
import com.m3o.mobile.api.Networking.ktorHttpAuthClient
import com.m3o.mobile.api.Networking.ktorHttpClient
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LoginRequest(val email: String, val password: String)

@Serializable
internal data class LoginResponse(val token: LoginToken)

@Serializable
internal data class LoginToken(
    @SerialName("access_token")
    val accessToken: String,
    val expiry: String,
    @SerialName("refresh_token")
    val refreshToken: String
)

@Serializable
internal data class CreateKeyRequest(val description: String, val scopes: List<String>)

@Serializable
internal data class CreateKeyResponse(
    @SerialName("api_key")
    val apiKey: String,
    @SerialName("api_key_id")
    val apiKeyId: String
)

@Serializable
internal data class RefreshRequest(@SerialName("refresh_token") val refreshToken: String)

object LoginService {

    internal suspend fun createKey(
        description: String = "M3O App Android",
        scopes: List<String> = listOf("*")
    ): CreateKeyResponse {
        return ktorHttpAuthClient.post(getUrl("v1/api/keys/generate")) {
            body = CreateKeyRequest(description, scopes)
        }
    }

    internal suspend fun login(email: String, password: String): LoginResponse {
        return ktorHttpClient.post(getUrl("customers/login")) {
            body = LoginRequest(email, password)
        }
    }

    internal suspend fun refresh(refreshToken: String) {
        return ktorHttpAuthClient.post(getUrl("customers/login")) {
            body = RefreshRequest(refreshToken)
        }
    }
}
