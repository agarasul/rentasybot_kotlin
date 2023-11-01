package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatIdFromCallback
import dev.rasul.rentasybot.extension.getCallBackInfo
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.models.UserInfo

class ConfirmationHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.getCallBackInfo()?.let { info ->
            try {
                update.chatIdFromCallback?.let { id ->

                    val user = dao.getUser(id)!!

                    val message: String
                    val replyMarkup: ReplyMarkup
                    if (info.data == "correct") {
                        message = resources.getTranslation(user.lang, "start_search_message")
                        replyMarkup = ReplyKeyboardRemove()
                        dao.updateUser(
                            telegramChatId = id,
                            params = mapOf<String, Any>(
                                "criteria.${UserCriteria::step.name}" to UserCriteria.Step.Finished.name,
                                "criteria.${UserCriteria::finished.name}" to true,
                                "criteria.${UserCriteria::enabled.name}" to true,
                            )
                        )
                    } else {
                        message = resources.getTranslation(user.lang, "lets_start")
                        replyMarkup = keyboardHelper.getLangKeyboard(isEdit = false)
                        dao.updateUser(
                            telegramChatId = id,
                            params = mapOf<String, Any>(
                                UserInfo::criteria.name to UserCriteria()
                            )
                        )
                    }

                    bot.deleteMessage(
                        messageId = update.callbackQuery?.message?.messageId!!,
                        chatId = ChatId.fromId(user.telegramUserId)
                    )

                    bot.sendMessage(
                        chatId = ChatId.fromId(user.telegramUserId),
                        text = message,
                        replyMarkup = replyMarkup
                    )


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}