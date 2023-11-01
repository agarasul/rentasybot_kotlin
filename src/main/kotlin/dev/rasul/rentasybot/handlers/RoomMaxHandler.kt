package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatIdFromCallback
import dev.rasul.rentasybot.extension.getCallBackInfo
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.queue.MessageQueue

class RoomMaxHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper,
    private val messageQueue: MessageQueue
) : MessageHandler {


    override suspend fun handle(bot: Bot, update: Update) {
        update.getCallBackInfo()?.let { info ->

            update.chatIdFromCallback?.let { id ->
                val params = mutableMapOf<String, Any>(
                    "criteria.${UserCriteria::step.name}" to UserCriteria.Step.Area.name,
                )

                val user = dao.getUser(telegramUserId = id)!!


                if (info.data == "max") {
                    params["criteria.${UserCriteria::roomMax.name}"] = 4

                } else {
                    if (user.criteria!!.roomMin!! <= info.data.toInt()) {
                        params["criteria.${UserCriteria::roomMax.name}"] = info.data.toInt()
                    } else {
                        bot.answerCallbackQuery(
                            callbackQueryId = update.callbackQuery!!.id,
                            text = resources.getTranslation(user.lang, "room_max_error"),
                            showAlert = true
                        )
                        return
                    }
                }

                dao.updateUser(
                    telegramChatId = id,
                    params = params
                )?.let { updatedUser ->
                    val message = resources.getTranslation(updatedUser.lang, "select_area")

                    bot.deleteMessage(
                        chatId = ChatId.fromId(user.telegramUserId),
                        messageId = update.callbackQuery?.message?.messageId!!
                    )

                    bot.sendMessage(
                        chatId = ChatId.fromId(updatedUser.telegramUserId),
                        text = message,
                        replyMarkup = ReplyKeyboardRemove()
                    ).getOrNull()?.let {
                        messageQueue.addToStack(it)
                    }
                }
            }

        }
    }
}