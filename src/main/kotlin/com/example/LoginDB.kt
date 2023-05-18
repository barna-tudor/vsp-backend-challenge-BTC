package com.example

import com.example.models.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import org.apache.commons.codec.binary.Hex
import org.apache.commons.codec.digest.DigestUtils
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import java.security.SecureRandom


private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("UserDB")
private val users = database.getCollection<User>()

suspend fun registerUser(user: User): Boolean {
    return users.insertOne(user).wasAcknowledged()
}

suspend fun checkIfUserExists(email: String): Boolean {
    return users.findOne(User::email eq email) != null
}

suspend fun checkPasswordForEmail(email: String, passwordToCheck: String): Boolean {
    // if email is registered, find corresponding password, else return false
    val actualPassword = users.findOne(User::email eq email)?.password ?: return false
    // check if input pass matches stored password
    return checkHashForPassword(passwordToCheck, actualPassword)
}


fun checkHashForPassword(password: String, hashWithSalt: String): Boolean {
    val hashAndSalt = hashWithSalt.split(":")
    val salt = hashAndSalt[0]
    val hash = hashAndSalt[1]
    val passwordHash = DigestUtils.sha256Hex("$salt$password")
    return hash == passwordHash
}

fun getHashWithSalt(stringToHash: String, saltLength: Int = 32): String {
    val salt = SecureRandom.getInstance("SHA1PRNG").generateSeed(saltLength)
    val saltAsHex = Hex.encodeHexString(salt)
    val hash = DigestUtils.sha256Hex("$saltAsHex$stringToHash")
    return "$saltAsHex:$hash"
}

fun Application.configureSecurity() {
    authentication {
        basic {
            realm = "Login Server"
            validate { credentials ->
                val email = credentials.name
                val password = credentials.password
                if (checkPasswordForEmail(email, password)) {
                    UserIdPrincipal(email)
                } else null
            }
        }
    }
}