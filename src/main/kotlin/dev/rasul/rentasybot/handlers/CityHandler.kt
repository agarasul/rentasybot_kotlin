package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatIdFromCallback
import dev.rasul.rentasybot.extension.editMessage
import dev.rasul.rentasybot.extension.getCallBackInfo
import dev.rasul.rentasybot.models.UserCriteria

class CityHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.getCallBackInfo()?.let { info ->
            try {
                update.chatIdFromCallback?.let { id ->

                    dao.updateUser(
                        telegramChatId = id,
                        params = mapOf(
                            "criteria.${UserCriteria::city.name}" to info.data.toInt(),
                            "criteria.${UserCriteria::step.name}" to UserCriteria.Step.District.name,
                        )
                    )?.let { user ->
                        val districts = resources.locations.first { city -> city.id == info.data.toInt() }.locations
                        val message = if (districts.isEmpty()) {
                            resources.getTranslation(user.lang, "select_room_min")
                        } else {
                            resources.getTranslation(user.lang, "select_city")
                        }
                        user.criteria?.let { criteria ->
                            bot.editMessage(
                                messageId = update.callbackQuery?.message?.messageId,
                                chatId = ChatId.fromId(user.telegramUserId),
                                text = message,
                                replyMarkup = keyboardHelper.getDistrictsKeyboard(
                                    user.lang,
                                    criteria
                                )
                            )

                        }
                    }


                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}