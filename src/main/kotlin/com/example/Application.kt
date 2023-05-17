package com.example

import com.example.models.configModel
import com.example.models.currentConfig
import com.example.plugins.*
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import kotlinx.serialization.decodeFromString
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.json.XML
import java.io.FileInputStream
import java.util.*

//TODO: make function for jsonOfRates.forEach{} that writes its own file, according to currentConfig from persistent storage
fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val client = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }
        }
    }
    configureSerialization()
    configureRouting(client)
    updateDaily(client)
    val APPLICATION_NAME = "VSP-BackendChallenge-BTC"
    val JSON_FACTORY = GsonFactory.getDefaultInstance()
    val SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE)
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    val credential: GoogleCredential =
        GoogleCredential.fromStream(FileInputStream("src/main/resources/serviceAccCred.json")).createScoped(SCOPES)
    val service = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential)
        .setApplicationName(APPLICATION_NAME)
        .build()
    val mimeType = "application/json"
    val folderId = "1wCLLGXa-C_2v8200G_Ltf-rb1bSacItm"

//    val fileMetadata = File()
//    fileMetadata.name = "output2.json" // Name of the new file
//    fileMetadata.setParents(Collections.singletonList(folderId)) // ID of the parent directory
//    var tempFile : java.io.File = java.io.File()
//    val fileContent = FileContent(mimeType, java.io.File("src/main/resources/dailyContent.json"))
//
//    try {
//        service.files().create(fileMetadata, fileContent)
//            .setFields("id, parents")
//            .execute()
//        println("File ID: " + fileMetadata.id)
//
//    } catch (e: GoogleJsonResponseException) {
//        // TODO(developer) - handle error appropriately
//        System.err.println("Unable to upload file: " + e.details)
//        throw e
//    }

}

fun updateDaily(client: HttpClient) {
    CoroutineScope(Job()).launch {
        if (currentConfig == configModel()) {
            val configString = java.io.File("src/main/resources/currentConfig.json").readText()
            currentConfig = Json.decodeFromString<configModel>(configString)
        }
        val xmlFileRawText = client.get("https://www.bnr.ro/nbrfxrates.xml").bodyAsText()
        val jsonOfRates = XML.toJSONObject(xmlFileRawText)
            .getJSONObject("DataSet").getJSONObject("Body").getJSONObject("Cube").getJSONArray("Rate")
        val configJSONObject = JSONObject(currentConfig)
        /*
            TODO: separate into different function
            TODO: keep this one just for currentConfig writing to persistent daily on timer
         */
        jsonOfRates.forEach {
            it as JSONObject
            if (!configJSONObject.getBoolean(it.getString("currency"))) {
                it.clear()
            } else {
                it.put(it.getString("currency"), it.getDouble("content"))
                it.remove("currency")
                it.remove("content")
            }
        }
        java.io.File("src/main/resources/dailyContent.json")
            .writeText(jsonOfRates.filter { it as JSONObject; (!it.isEmpty) }.toString())
    }
}

