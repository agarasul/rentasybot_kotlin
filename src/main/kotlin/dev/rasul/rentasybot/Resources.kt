package dev.rasul.rentasybot

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import dev.rasul.rentasybot.models.Location

class Resources(private val gson: Gson) {

    companion object {
        private val TRANSLATES_JSON_NAME = "localization.json"
        private val CITIES_JSON_NAME = "cities.json"
    }


    private val localization =
        gson.fromJson(
            this.javaClass.classLoader.getResource(TRANSLATES_JSON_NAME)?.readText() ?: "",
            JsonObject::class.java
        )

    val translates = localization["translates"].asJsonObject

    val locations = gson.fromJson<List<Location>>(
        this.javaClass.classLoader.getResource(CITIES_JSON_NAME)?.readText() ?: "",
        object : TypeToken<List<Location>>() {}.type
    )
}