package dev.rasul.rentasybot.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.TelegramFile
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import dev.rasul.rentasybot.config.BuildConfig
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.handlers.StartHandler
import dev.rasul.rentasybot.queue.AdsMessageQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class RentasyBot(
    private val scope: CoroutineScope,
    private val userConfigDao: UserConfigDao,
    private val startHandler: StartHandler,
    private val queue: AdsMessageQueue
) {

    private var bot: Bot? = null

    fun start() {
        bot = bot {
            token = BuildConfig.getBotApiKey()
            dispatch {
                command("start") {

                    startHandler.handle(update)
                }
            }
        }
        bot?.startPolling()
    }

    fun sendAds() {
        scope.launch {
            queue.messages.collect { message ->
                try {
                    if (message.chatId != BuildConfig.DEVELOPER_CHAT_ID) {
                        return@collect
                    }

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
}