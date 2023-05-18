@file:Suppress("DEPRECATION")

package com.example

import com.example.models.ConfigModel
import com.example.models.currentConfig
import com.example.plugins.configureRouting
import com.example.plugins.configureSerialization
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.client.http.FileContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.server.application.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.json.XML
import java.io.FileInputStream
import java.time.LocalDate
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.days


//TODO: make function for jsonOfRates.forEach{} that writes its own file, according to currentConfig from persistent storage
fun main(args: Array<String>): Unit = io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val client = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }
        }
    }
    val APPLICATION_NAME = "VSP-BackendChallenge-BTC"
    val JSON_FACTORY = GsonFactory.getDefaultInstance()
    val SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE)
    val HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport()
    val credential: GoogleCredential =
        GoogleCredential.fromStream(FileInputStream("src/main/resources/serviceAccCred.json")).createScoped(SCOPES)
    val service = Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build()
    val folderId = "1wCLLGXa-C_2v8200G_Ltf-rb1bSacItm"
    configureSecurity()
    configureSerialization()
    configureRouting(client)

    loadConfigFromFile()
    val scheduler = Scheduler {
        launch(Dispatchers.IO) {
            writeDailyFiles(client, service, folderId)
        }
    }
    scheduler.scheduleExecution(1.days)
}

fun loadConfigFromFile() {
    val configString = java.io.File("src/main/resources/currentConfig.json").readText()
    currentConfig = Json.decodeFromString<ConfigModel>(configString)
}

suspend fun writeDailyFiles(
    client: HttpClient,
    service: Drive,
    folderId: String
) {

    //Get today's xml
    val xmlFileRawText = client.get("https://www.bnr.ro/nbrfxrates.xml").bodyAsText()
    //Extract currencies rates as json
    val jsonOfRates =
        XML.toJSONObject(xmlFileRawText).getJSONObject("DataSet").getJSONObject("Body").getJSONObject("Cube")
            .getJSONArray("Rate")
    //
    val configJSONObject = JSONObject(currentConfig)
    jsonOfRates.forEach {
        it as JSONObject
        if (!configJSONObject.getBoolean(it.getString("currency"))) {
            it.clear()
        } else {
            val fileMetadata = File()
            fileMetadata.name = LocalDate.now().toString() + it.getString("currency") + ".json"
            fileMetadata.parents = Collections.singletonList(folderId)
            it.put(it.getString("currency"), it.getDouble("content"))
            it.remove("currency")
            it.remove("content")
            java.io.File("src/main/resources/dailyContent.json").writeText(it.toString())
            val fileContent = FileContent("application/json", java.io.File("src/main/resources/dailyContent.json"))
            try {
                service.files().create(fileMetadata, fileContent).setFields("id, parents").execute()
            } catch (e: GoogleJsonResponseException) {
                // TODO handle error appropriately
                System.err.println("Unable to upload file: " + e.details)
            }
        }
    }
}


class Scheduler(private val task: Runnable) {
    private val executor = Executors.newScheduledThreadPool(1)
    fun scheduleExecution(every: kotlin.time.Duration) {
        val taskWrapper = Runnable {
            task.run()
        }
        executor.scheduleWithFixedDelay(
            taskWrapper,
            1000,
            every.inWholeMilliseconds,
            TimeUnit.MILLISECONDS
        )
    }

    fun stop() {
        executor.shutdown()
        try {
            executor.awaitTermination(1, TimeUnit.HOURS)
        } catch (e: InterruptedException) {

        }
    }
}
