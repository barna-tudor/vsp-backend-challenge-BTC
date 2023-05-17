package com.example.routes

import com.example.models.configModel
import com.example.models.currentConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.json.JSONObject

fun Route.configRoute() {
    route("/configureCurrencies") {
        post {
            try {
                val config = call.receive<configModel>()
                currentConfig = config
                java.io.File("src/main/resources/currentConfig.json").writeText(JSONObject(currentConfig).toString())
            } catch (e: ContentTransformationException) {
                call.respondText("Invalid Configuration Format", status = HttpStatusCode.BadRequest)
            }
            call.respondText(
                "Configuration updated\n${JSONObject(currentConfig)}",
                status = HttpStatusCode.OK
            )
        }
    }
}