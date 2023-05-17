package com.example.plugins

import com.example.routes.configRoute
import com.example.routes.jsonDisplayRouting
import io.ktor.client.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.application.*

fun Application.configureRouting(client:HttpClient) {
    routing {
        jsonDisplayRouting(client)
        configRoute()
    }
}
