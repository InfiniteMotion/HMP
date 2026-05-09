package com.hmp.data.network

import io.ktor.client.*
import kotlinx.serialization.json.Json

expect fun createHttpClient(json: Json): HttpClient

expect fun createJson(): Json
