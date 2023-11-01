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
import dev.rasul.rentasybot.helper.MessageCaptionFormatter
import dev.rasul.rentasybot.models.UserCriteria

class AdTypeHandler(
    override val dao: UserConfigDao,
    override val resources: Resources,
    override val keyboardHelper: KeyboardHelper,
    private val messageCaptionFormatter: MessageCaptionFormatter
) : MessageHandler {

    override suspend fun handle(bot: Bot, update: Update) {
        update.getCallBackInfo()?.let { info ->
            try {
                update.chatIdFromCallback?.let { id ->
                    val onlyFromOwners = info.data == "private"
                    dao.updateUser(
                        telegramChatId = id,
                        params = mapOf<String, Any>(
                            "criteria.${UserCriteria::onlyFromOwners.name}" to onlyFromOwners,
                            "criteria.${UserCriteria::step.name}" to UserCriteria.Step.Confirmation.name,
                        )
                    )?.let { user ->
                        bot.editMessage(
                            messageId = update.callbackQuery?.message?.messageId,
                            chatId = ChatId.fromId(user.telegramUserId),
                            text = messageCaptionFormatter.prepareConfirmationMessage(user),
                            replyMarkup = keyboardHelper.getConfirmationKeyboard(user.lang)
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}