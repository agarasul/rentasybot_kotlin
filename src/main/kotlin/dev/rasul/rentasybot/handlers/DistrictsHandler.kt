package dev.rasul.rentasybot.handlers

import KeyboardHelper
import RoomKeyboardType
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatIdFromCallback
import dev.rasul.rentasybot.extension.editMessage
import dev.rasul.rentasybot.extension.getCallBackInfo
import dev.rasul.rentasybot.models.UserCriteria

class DistrictsHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.getCallBackInfo()?.let { info ->
            try {
                update.chatIdFromCallback?.let { id ->
                    var user = dao.getUser(id)!!

                    val message: String
                    val keyboardMarkup: ReplyMarkup


                    when (info.data) {
                        "next" -> {
                            val params = if (user.criteria!!.allDistricts) {
                                mapOf(
                                    "criteria.${UserCriteria::district.name}" to emptyList<Int>(),
                                    "criteria.${UserCriteria::step.name}" to UserCriteria.Step.RoomMin.name
                                )
                            } else {
                                mapOf("criteria.${UserCriteria::step.name}" to UserCriteria.Step.RoomMin.name)
                            }
                            dao.updateUser(
                                telegramChatId = user.telegramUserId,
                                params = params
                            )

                            message = resources.getTranslation(user.lang, "select_room_min")
                            keyboardMarkup = keyboardHelper.getRoomKeyboard(type = RoomKeyboardType.Min)
                        }

                        "clear" -> {
                            message = resources.getTranslation(user.lang, "select_location")
                            keyboardMarkup = dao.updateUser(
                                user.telegramUserId,
                                params = mapOf(
                                    "criteria.${UserCriteria::district.name}" to emptyList<Int>(),
                                    "criteria.${UserCriteria::step.name}" to UserCriteria.Step.District.name,
                                )
                            )?.let {
                                keyboardHelper.getDistrictsKeyboard(
                                    it.lang,
                                    it.criteria!!
                                )
                            } ?: update.callbackQuery?.message?.replyMarkup!!

                        }

                        else -> {
                            message = resources.getTranslation(user.lang, "select_location")

                            val isAllDistricts = info.data == "all"

                            val newDistricts: List<Int> = if (isAllDistricts) {
                                emptyList()
                            } else {
                                (user.criteria?.district ?: emptyList()).toMutableList().apply {
                                    if (contains(info.data.toInt())) {
                                        remove(info.data.toInt())
                                    } else {
                                        add(info.data.toInt())
                                    }
                                }
                            }


                            user = dao.updateUser(
                                telegramChatId = id,
                                params = mapOf(
                                    "criteria.${UserCriteria::district.name}" to newDistricts,
                                    "criteria.${UserCriteria::allDistricts.name}" to isAllDistricts,
                                    "criteria.${UserCriteria::step.name}" to UserCriteria.Step.District.name,

                                )
                            )!!

                            keyboardMarkup = user.let {
                                keyboardHelper.getDistrictsKeyboard(
                                    it.lang,
                                    it.criteria!!
                                )
                            }
                        }
                    }

                    bot.editMessage(
                        messageId = update.callbackQuery?.message?.messageId,
                        chatId = ChatId.fromId(user.telegramUserId),
                        text = message,
                        replyMarkup = keyboardMarkup
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}