package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao

interface MessageHandler {

    val dao : UserConfigDao
    val resources : Resources
    val keyboardHelper : KeyboardHelper

    @Throws(Exception::class)
    suspend fun handle(bot: Bot, update: Update)
}