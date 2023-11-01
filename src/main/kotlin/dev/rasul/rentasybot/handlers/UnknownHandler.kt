package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatId
import dev.rasul.rentasybot.models.ErrorMessage
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.queue.ErrorMessageQueue

class UnknownHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper,
    private val errorMessageQueue: ErrorMessageQueue
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.chatId?.let { chatId ->
            val user = dao.getUser(telegramUserId = chatId)!!


            when (UserCriteria.Step.valueOf(user.criteria!!.step)) {
                UserCriteria.Step.Price, UserCriteria.Step.Area -> {
                    bot.deleteMessage(
                        chatId = ChatId.fromId(chatId),
                        messageId = update.message?.messageId!!
                    )

                    errorMessageQueue.addToStack(
                        msg = ErrorMessage(
                            chatId = chatId,
                            text = resources.getTranslation(
                                lang = user.lang,
                                "range_format_error"
                            ),
                            markup = ReplyKeyboardRemove()
                        )
                    )
                }

                else -> {

                }
            }

        }
    }
}