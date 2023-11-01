package dev.rasul.rentasybot.handlers

import KeyboardHelper
import RoomKeyboardType
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatIdFromCallback
import dev.rasul.rentasybot.extension.editMessage
import dev.rasul.rentasybot.extension.getCallBackInfo
import dev.rasul.rentasybot.models.UserCriteria

class RoomMinHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.getCallBackInfo()?.let { info ->

            update.chatIdFromCallback?.let { id ->
                val params = mutableMapOf<String, Any>(
                    "criteria.${UserCriteria::step.name}" to UserCriteria.Step.RoomMax.name,
                )
                if (info.data == "max") {
                    params["criteria.${UserCriteria::roomMin.name}"] = 4

                } else {
                    params["criteria.${UserCriteria::roomMin.name}"] = info.data.toInt()
                }

                val user = dao.updateUser(
                    telegramChatId = id,
                    params = params
                )!!

                val message = if (info.data == "max") {
                    resources.getTranslation(user.lang, "select_price")
                } else {
                    resources.getTranslation(user.lang, "select_room_max")
                }

                bot.editMessage(
                    messageId = update.callbackQuery?.message?.messageId,
                    chatId = ChatId.fromId(user.telegramUserId),
                    text = message,
                    replyMarkup = keyboardHelper.getRoomKeyboard(type = RoomKeyboardType.Max)
                )
            }

        }
    }
}