package com.example.models

import kotlinx.serialization.Serializable

@Serializable
data class ConfigModel(
    val AED: Boolean = false,
    val AUD: Boolean = false,
    val BGN: Boolean = false,
    val BRL: Boolean = false,
    val CAD: Boolean = false,
    val CHF: Boolean = false,
    val CNY: Boolean = false,
    val CZK: Boolean = false,
    val DKK: Boolean = false,
    val EGP: Boolean = false,
    val EUR: Boolean = false,
    val GBP: Boolean = false,
    val HUF: Boolean = false,
    val INR: Boolean = false,
    val JPY: Boolean = false,
    val KRW: Boolean = false,
    val MDL: Boolean = false,
    val MXN: Boolean = false,
    val NOK: Boolean = false,
    val NZD: Boolean = false,
    val PLN: Boolean = false,
    val RSD: Boolean = false,
    val RUB: Boolean = false,
    val SEK: Boolean = false,
    val THB: Boolean = false,
    val TRY: Boolean = false,
    val UAH: Boolean = false,
    val USD: Boolean = false,
    val XAU: Boolean = false,
    val XDR: Boolean = false,
    val ZAR: Boolean = false
)
// in-memory configuration
var currentConfig = ConfigModel()