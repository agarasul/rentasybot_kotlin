package dev.rasul.rentasybot.extension

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.models.CallbackInfo


val Update.chatId: Long?
    get() = message?.from?.id

val Update.chatIdFromCallback: Long?
    get() = callbackQuery?.from?.id


val Update.dataFromCallbackQuery
    get() = callbackQuery?.data?.split("_")?.lastOrNull()

fun Update.getCallBackInfo(): CallbackInfo? {
    return callbackQuery?.data?.split("_")?.let {
        CallbackInfo(key = it.first(), data = it.last())
    }
}


suspend fun Bot.editMessage(
    messageId: Long?,
    chatId: ChatId,
    text: String,
    replyMarkup: ReplyMarkup,
    parseMode: ParseMode = ParseMode.HTML
) {
    editMessageText(
        messageId = messageId,
        chatId = chatId,
        text = text,
        replyMarkup = replyMarkup,
        parseMode = parseMode
    )
}