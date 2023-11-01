package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatIdFromCallback
import dev.rasul.rentasybot.extension.dataFromCallbackQuery
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.models.UserInfo

class LanguageHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.dataFromCallbackQuery?.let { lang ->
            try {
                update.chatIdFromCallback?.let { id ->
                    dao.updateUser(
                        telegramChatId = id,
                        params = mapOf(
                            UserInfo::lang.name to lang,
                            "criteria.${UserCriteria::step.name}" to UserCriteria.Step.City.name,
                        )
                    )?.let {

                        val message = resources.getTranslation(it.lang, "select_city")

                        bot.editMessageText(
                            messageId = update.callbackQuery?.message?.messageId,
                            chatId = ChatId.fromId(it.telegramUserId),
                            text = message,
                            replyMarkup = keyboardHelper.getCitiesKeyboard()
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}