package dev.rasul.rentasybot.models

data class Location(
    val name: String,
    val region: String,
    val olx: OlxInfo,
    val otodom: OtodomInfo,
    val locations: List<Location> = emptyList(),
    val id : Int
)

data class OlxInfo(
    val id: Int
)

data class OtodomInfo(
    val location: String,
    val region_id: String,
    val subregion_id: String,
    val city_id: String,
    val district_id: String? = null
)