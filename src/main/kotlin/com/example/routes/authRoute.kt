package com.example.routes

import com.example.checkIfUserExists
import com.example.checkPasswordForEmail
import com.example.getHashWithSalt
import com.example.models.AccountRequest
import com.example.models.User
import com.example.registerUser
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.OK
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoute() {
    route("/register") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                return@post call.respond(BadRequest)
            }
            val userExists = checkIfUserExists(request.email)
            if (!userExists) {
                if (registerUser(User(request.email, getHashWithSalt(request.password)))) {
                    call.respond(OK, "Successfully created account!")
                } else {
                    call.respond(OK, "An unknown error occurred")
                }
            } else {
                call.respond(OK, "A user with that E-Mail already exists")
            }
        }
    }
    // Only used for testing, does nothing by itself
    route("/login") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (e: ContentTransformationException) {
                return@post call.respond(BadRequest)
            }
            val isPasswordCorrect = checkPasswordForEmail(request.email, request.password)
            if (isPasswordCorrect) {
                call.respond(OK, "Your are now logged in!")
            } else {
                call.respond(OK, "The E-Mail or password is incorrect")
            }
        }
    }
}
