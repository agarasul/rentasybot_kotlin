package dev.rasul.rentasybot.models

import java.io.File




data class AdMessage(
    val chatId : Long,
    val text : String,
    val attachments : List<File>
)