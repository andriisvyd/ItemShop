package com.svyd.itemshop.data.common

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Single source of truth for outbound HTTP. Created once and shared across
 * all data sources so connection pooling and JSON config stay consistent.
 *
 * The OkHttp engine is the standard Android choice: HTTP/2, connection
 * pooling, sensible defaults. When this code moves to KMP, swap the engine
 * (e.g. Darwin on iOS) without touching call sites.
 */
object HttpClientFactory {

    val json: Json = Json {
        ignoreUnknownKeys = true
        coerceInputValues = true
        explicitNulls = false
        encodeDefaults = true
    }

    fun create(enableLogging: Boolean): HttpClient = HttpClient(OkHttp) {
        expectSuccess = true

        install(ContentNegotiation) {
            json(json)
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 15_000
            requestTimeoutMillis = 30_000
            socketTimeoutMillis = 30_000
        }

        if (enableLogging) {
            install(Logging) {
                level = LogLevel.INFO
                logger = Logger.DEFAULT
                sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
            }
        }

        defaultRequest {
            header(HttpHeaders.Accept, ContentType.Application.Json.toString())
        }
    }
}
