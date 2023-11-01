package dev.rasul.rentasybot.handlers

import KeyboardHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.ReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import dev.rasul.rentasybot.Resources
import dev.rasul.rentasybot.db.UserConfigDao
import dev.rasul.rentasybot.extension.chatId
import dev.rasul.rentasybot.models.ErrorMessage
import dev.rasul.rentasybot.models.UserCriteria
import dev.rasul.rentasybot.queue.ErrorMessageQueue
import dev.rasul.rentasybot.queue.MessageQueue
import kotlin.math.abs

class PriceAndAreaHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper,
    private val errorMessageQueue: ErrorMessageQueue,
    private val messageQueue: MessageQueue
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.message?.text?.let { text ->

            update.chatId?.let { id ->


                val range = text.split("-").mapNotNull { it.toIntOrNull() }

                if (range.size == 2) {

                    val user = dao.getUser(telegramUserId = id)!!
                    val (minValue, maxValue) = range


                    val errorMsg: String? = if (UserCriteria.Step.valueOf(user.criteria!!.step) == UserCriteria.Step.Price) {
                        when {
                            minValue < 500 || maxValue < 500 -> resources.getTranslation(
                                user.lang,
                                "select_price_validation_error"
                            )

                            (abs(maxValue) - abs(minValue) < 500) -> resources.getTranslation(
                                user.lang,
                                "select_range_error"
                            )

                            else -> null
                        }
                    } else {
                        if (minValue < 10 || maxValue < 10) {
                            resources.getTranslation(
                                user.lang,
                                "select_area_validation_error"
                            )
                        } else {
                            null
                        }
                    }

                    if (errorMsg != null) {
                        bot.deleteMessage(
                            chatId = ChatId.fromId(id),
                            messageId = update.message!!.messageId
                        )
                        errorMessageQueue.addToStack(
                            ErrorMessage(
                                chatId = id,
                                text = errorMsg,
                                markup = ReplyKeyboardRemove()
                            )
                        )
                        return
                    }


                    val message: String
                    val replyMarkup: ReplyMarkup
                    val params = when (UserCriteria.Step.valueOf(user.criteria.step)) {
                        UserCriteria.Step.Price -> {
                            replyMarkup = keyboardHelper.getAdTypeKeyboard(user.lang)
                            message = resources.getTranslation(user.lang, "select_ad_type")
                            mapOf(
                                "criteria.${UserCriteria::priceMin.name}" to minValue,
                                "criteria.${UserCriteria::priceMax.name}" to maxValue,
                                "criteria.${UserCriteria::step.name}" to UserCriteria.Step.AdType.name,
                            )
                        }

                        UserCriteria.Step.Area -> {
                            replyMarkup = ReplyKeyboardRemove()
                            message = resources.getTranslation(user.lang, "select_price")
                            mapOf(
                                "criteria.${UserCriteria::areaMin.name}" to minValue,
                                "criteria.${UserCriteria::areaMax.name}" to maxValue,
                                "criteria.${UserCriteria::step.name}" to UserCriteria.Step.Price.name,
                            )
                        }

                        else -> {
                            replyMarkup = ReplyKeyboardRemove()
                            message = ""
                            emptyMap()
                        }

                    }
                    dao.updateUser(
                        telegramChatId = id,
                        params = params
                    )
//
                    bot.deleteMessage(
                        chatId = ChatId.fromId(id),
                        messageId = update.message?.messageId!!
                    )

                    messageQueue.clear(chatId = id)
                    bot.sendMessage(
                        chatId = ChatId.fromId(user.telegramUserId),
                        text = message,
                        replyMarkup = replyMarkup
                    ).getOrNull()?.let {
                        messageQueue.addToStack(it)
                    }
                }
            }
        }
    }
}