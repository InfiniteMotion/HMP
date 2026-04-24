package com.hmp.data.network

import io.ktor.client.engine.darwin.*
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

actual fun createHttpClient(json: Json): HttpClient {
    return HttpClient(Darwin) {
        install(ContentNegotiation) {
            json(json)
        }
    }
}

actual fun createJson(): Json {
    return Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }
}
