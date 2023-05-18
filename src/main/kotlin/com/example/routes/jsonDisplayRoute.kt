package com.example.routes

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.json.JSONObject
import org.json.XML
import java.time.LocalDate
import java.time.format.DateTimeParseException


fun Route.jsonDisplayRouting(client: HttpClient) {
    // manipulate the raw json fields into favored format
    fun modifyJson(it: Any) {
        it as JSONObject
        it.put(it.getString("currency"), it.getDouble("content"))
        it.remove("currency")
        it.remove("content")
    }
    authenticate {
        route("/viewData") {
            get("/") {
                return@get call.respondRedirect("/viewData")
            }
            get {
                // get raw xml
                val xmlFileRawText = client.get("https://www.bnr.ro/nbrfxrates.xml").bodyAsText()
                // turn into json
                val jsonOfRatesByDate = XML.toJSONObject(xmlFileRawText)
                    .getJSONObject("DataSet").getJSONObject("Body").getJSONObject("Cube").getJSONArray("Rate")
                // format
                jsonOfRatesByDate.forEach(::modifyJson)
                // return json string
                call.respondText(jsonOfRatesByDate.toString())
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
                // get corresponding xml file
                val reqURL = "https://www.bnr.ro/files/xml/years/nbrfxrates${sYear}.xml"
                val xmlFileRawText = client.get(reqURL).bodyAsText()
                val jsonArray = XML.toJSONObject(xmlFileRawText)
                    .getJSONObject("DataSet").getJSONObject("Body").getJSONArray("Cube")
                var targetJSONObject = jsonArray.getJSONObject(0).getJSONArray("Rate")
                var jsonDate: LocalDate
                // find by date or most recent within 2 days
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
                // format
                targetJSONObject.forEach(::modifyJson)
                call.respondText(targetJSONObject.toString())
            }
        }
    }
}