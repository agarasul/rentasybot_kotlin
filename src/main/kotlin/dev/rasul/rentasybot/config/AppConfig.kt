package dev.rasul.rentasybot.config

import com.google.gson.Gson
import dev.rasul.rentasybot.models.AppConfigModel

class AppConfig(gson: Gson) {

    companion object {
        private val CONFIG_JSON_FILE = "config.json"
    }

    private val config = gson.fromJson(
        this.javaClass.classLoader.getResource(CONFIG_JSON_FILE)?.readText() ?: "",
        AppConfigModel::class.java
    )

    fun getDeveloperUserName() = config.developerUserId

    fun getTelegramBotToken(): String {
        return if (config.isDebug) {
            config.telegramBotToken.dev
        } else {
            config.telegramBotToken.prod
        }
    }

    fun getDbName(): String {
        return config.db.name
    }

    fun getCollectionName(): String {
        return if (config.isDebug) {
            config.db.collection.dev
        } else {
            config.db.collection.prod
        }
    }
}