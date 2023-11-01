package dev.rasul.rentasybot.models

import com.google.gson.annotations.SerializedName

data class AppConfigModel(
    @SerializedName("is_debug")
    val isDebug: Boolean,
    @SerializedName("developer_user_id")
    val developerUserId: String,
    @SerializedName("telegram_bot_token")
    val telegramBotToken: AppType,
    val db: Database
) {
    data class AppType(
        val prod: String,
        val dev: String
    )

    data class Database(
        val name: String,
        val collection: AppType
    )
}