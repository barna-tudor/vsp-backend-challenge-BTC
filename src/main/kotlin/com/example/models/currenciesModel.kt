package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class currenciesModel(
    /*val year: Int,
    val month: Int,
    val day: Int,*/
    val AED: Double,
    val AUD: Double,
    val BGN: Double,
    val BRL: Double,
    val CAD: Double,
    val CHF: Double,
    val CNY: Double,
    val CZK: Double,
    val DKK: Double,
    val EGP: Double,
    val EUR: Double,
    val GBP: Double,
    val HUF: Double, val HUF_multiplier: Int,
    val INR: Double,
    val JPY: Double, val JPY_multiplier: Int,
    val KRW: Double, val KRW_multiplier: Int,
    val MDL: Double,
    val MXN: Double,
    val NOK: Double,
    val NZD: Double,
    val PLN: Double,
    val RSD: Double,
    val RUB: Double,
    val SEK: Double,
    val THB: Double,
    val TRY: Double,
    val UAH: Double,
    val USD: Double,
    val XAU: Double,
    val XDR: Double,
    val ZAR: Double
)