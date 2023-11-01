package dev.rasul.rentasybot.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.dispatcher.message
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.extensions.filters.Filter
import dev.rasul.rentasybot.config.AppConfig
import dev.rasul.rentasybot.extension.getCallBackInfo
import dev.rasul.rentasybot.handlers.Callbacks
import dev.rasul.rentasybot.handlers.Handlers
import dev.rasul.rentasybot.queue.AdsMessageQueue
import dev.rasul.rentasybot.queue.ErrorMessageQueue
import dev.rasul.rentasybot.queue.MessageQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class RentasyBot(
    private val scope: CoroutineScope,
    private val handlers: Handlers,
    private val adsQueue: AdsMessageQueue,
    private val errorMessageQueue: ErrorMessageQueue,
    private val messageQueue: MessageQueue,
    private val config: AppConfig
) {

    private var bot: Bot? = null

    fun start() {
        bot = bot {
            token = config.getTelegramBotToken()
            dispatch {
                command("start") {
                    handlers.startHandler.handle(bot, update)
                }
                callbackQuery {

                    update.getCallBackInfo()?.let { info ->

                        val handler = when (info.key) {
                            Callbacks.CALLBACK_LANG -> handlers.languageHandler
                            Callbacks.CALLBACK_CITY -> handlers.cityHandler
                            Callbacks.CALLBACK_DISTRICT -> handlers.districtsHandler
                            Callbacks.CALLBACK_ROOM_MIN -> handlers.roomMinHandler
                            Callbacks.CALLBACK_ROOM_MAX -> handlers.roomMaxHandler
                            Callbacks.CALLBACK_AD_TYPE -> handlers.adTypeHandler
                            Callbacks.CALLBACK_CONFIRMATION -> handlers.confirmationHandler
                            else -> handlers.unknownHandler
                        }

                        handler.handle(bot, update)
                    }
                }
                message(filter = Filter.Custom {
                    val range = text?.split("-")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                    range.size == 2
                }) {
                    handlers.priceAndAreaHandler.handle(bot, update)
                }

                message(Filter.Text.and(
                    Filter.Custom {
                        val range = text?.split("-")?.mapNotNull { it.toIntOrNull() } ?: emptyList()
                        range.size != 2
                    }
                )) {
                    handlers.unknownHandler.handle(bot, update)
                }
            }
        }
        bot?.startPolling()
//        sendAds()
        sendErrorMessages()
        clearMessages()
    }

    fun sendAds() {
        scope.launch {
            adsQueue.messages.collect { message ->
                try {

                    bot?.sendMediaGroup(
                        chatId = ChatId.fromId(message.chatId),
                        mediaGroup = MediaGroup.from(
                            *message.attachments.mapIndexed { index, file ->
                                val caption = if (index == 0) {
                                    message.text
                                } else {
                                    null
                                }

                                InputMediaPhoto(
                                    media = TelegramFile.ByFile(file),
                                    caption = caption,
                                    parseMode = ParseMode.HTML.modeName
                                )
                            }.toTypedArray()
                        )
                    )

                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun sendErrorMessages() {
        scope.launch {
            errorMessageQueue.messages.collect { message ->
                try {
                    bot?.sendMessage(
                        chatId = ChatId.fromId(message.chatId),
                        text = message.text
                    )?.get()?.let {
                        delay(TimeUnit.SECONDS.toMillis(2))
                        bot?.deleteMessage(
                            chatId = ChatId.fromId(it.chat.id),
                            messageId = it.messageId
                        )
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                }
            }
        }
    }

    private fun clearMessages() {
        messageQueue.callback = {
            bot?.deleteMessage(
                chatId = ChatId.fromId(it.first),
                messageId = it.second
            )
        }
    }
}