package dev.rasul.rentasybot.models

import com.github.kotlintelegrambot.entities.ReplyMarkup

data class ErrorMessage(
    val chatId: Long,
    val text: String,
    val markup: ReplyMarkup
)