package com.example.routes

import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.time.LocalDate
import java.time.format.DateTimeParseException
import org.json.*

fun Route.jsonDisplayRouting() {
    val client = HttpClient(OkHttp) {
        engine {
            config {
                followRedirects(true)
            }
        }
    }

    fun modifyJson(it: Any) {
        it as JSONObject
        it.put(it.getString("currency"), it.getDouble("content"))
        it.remove("currency")
        it.remove("content")
    }
    route("/viewData") {
        get {
            val xmlFileRawText = client.get("https://www.bnr.ro/nbrfxrates.xml").bodyAsText()
            val jsonOfRatesByDate = XML.toJSONObject(xmlFileRawText)
                .getJSONObject("DataSet").getJSONObject("Body").getJSONObject("Cube").getJSONArray("Rate")
            /*jsonOfRatesByDate.forEach {
                it as JSONObject
                it.put(it.getString("currency"), it.getDouble("content"))
                it.remove("currency")
                it.remove("content")
            }
            */
            jsonOfRatesByDate.forEach(::modifyJson)
            call.respondText(jsonOfRatesByDate.toString())
            /* TODO: parse as json */
        }
        get(Regex("/(?<year>[0-9]{4})-(?<month>[0-9]{2})-(?<day>[0-9]{2})")) {
            //input validation
            val sYear = call.parameters["year"].toString()
            val sMonth = call.parameters["month"].toString()
            val sDay = call.parameters["day"].toString()
            val date: LocalDate
            try {
                date = LocalDate.parse("$sYear-$sMonth-$sDay")
            } catch (e: DateTimeParseException) {
                return@get call.respondText("Invalid date", status = HttpStatusCode.BadRequest)
            }
            if (LocalDate.now() < date) {
                return@get call.respondText("Invalid date", status = HttpStatusCode.BadRequest)
            }
            if (date.year < 2005) {
                return@get call.respondText("No data for years before 2005", status = HttpStatusCode.BadRequest)
            }
            if (date == LocalDate.now()) {
                return@get call.respondRedirect("/viewData")
            }
            val reqURL = "https://www.bnr.ro/files/xml/years/nbrfxrates${sYear}.xml"
            val xmlFileRawText = client.get(reqURL).bodyAsText()
            val jsonArray = XML.toJSONObject(xmlFileRawText)
                .getJSONObject("DataSet").getJSONObject("Body").getJSONArray("Cube")
            var targetJSONObject = jsonArray.getJSONObject(0).getJSONArray("Rate")
            var jsonDate: LocalDate
            for (i in jsonArray) {
                i as JSONObject
                jsonDate = LocalDate.parse(i.getString("date"))
                if (jsonDate == date) {
                    targetJSONObject = i.getJSONArray("Rate")
                    break
                }
                if (jsonDate.plusDays(1) == date || jsonDate.plusDays(2) == date) {
                    targetJSONObject = i.getJSONArray("Rate")
                }
                if (jsonDate > date) {
                    break
                }
            }
            targetJSONObject.forEach(::modifyJson)
            call.respondText(targetJSONObject.toString())

        }
    }
}