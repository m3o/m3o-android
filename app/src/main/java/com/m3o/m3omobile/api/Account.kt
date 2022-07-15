package com.m3o.m3omobile.api

import com.m3o.m3omobile.api.Networking.getUrl
import com.m3o.m3omobile.api.Networking.ktorHttpAuthClient
import io.ktor.client.request.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class ReadRequest(val email: String, val password: String)

@Serializable
internal data class ReadResponse(val customer: Customer)

@Serializable
internal data class Customer(
    val created: String,
    val email: String,
    val id: String,
    val meta: Map<String, String>,
    val name: String,
    val status: String,
    val updated: String
)

@Serializable
internal data class BalanceRequest(@SerialName("customer_id") val customerId: String)

@Serializable
internal data class BalanceResponse(@SerialName("current_balance") val currentBalance: String)

object AccountService {

    internal suspend fun balance(customerId: String): BalanceResponse {
        return ktorHttpAuthClient.post(getUrl("balance/Current")) {
            body = BalanceRequest(customerId)
        }
    }

    internal suspend fun read(email: String, password: String): ReadResponse {
        return ktorHttpAuthClient.post(getUrl("customers/read")) {
            body = ReadRequest(email, password)
        }
    }
}
