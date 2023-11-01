package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.models.UserInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async

class StartHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper,
    val scope: CoroutineScope
) : MessageHandler {
    override suspend fun handle(bot: Bot, update: Update) {
        update.message?.chat?.let {
            try {
                val chatId = it.id

                val config = dao.getUser(chatId)

                val message: String = scope.async {
                    if (config == null) {
                        val newConfig = UserInfo.new(
                            telegramUserId = chatId,
                            firstname = update.message?.from?.firstName,
                            lastname = update.message?.from?.lastName
                        )
                        dao.insertUser(newConfig)

                        resources.getTranslation("en", "lets_start")
                    } else {
                        dao.updateUser(
                            chatId,
                            params = mapOf(
                                UserInfo::seenOlxIds.name to listOf("4", "3"),
                                UserCriteria::step.name to UserCriteria.Step.Language
                            )
                        )

                        resources.getTranslation("en", "start_search_message")
                    }
                }.await()

                bot.sendMessage(
                    chatId = ChatId.fromId(chatId),
                    text = message,
                    replyMarkup = keyboardHelper.getLangKeyboard(isEdit = false)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}