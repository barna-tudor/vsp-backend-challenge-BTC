package com.example.plugins

import com.example.routes.AuthRoute
import com.example.routes.configRoute
import com.example.routes.jsonDisplayRouting
import io.ktor.client.*
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureRouting(client:HttpClient) {
    routing {
        jsonDisplayRouting(client)
        configRoute()
        AuthRoute()
    }
}
