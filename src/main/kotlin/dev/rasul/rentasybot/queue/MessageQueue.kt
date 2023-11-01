package dev.rasul.rentasybot.queue

import com.github.kotlintelegrambot.entities.Message
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch


typealias Callback = (Pair<Long, Long>) -> Unit

class MessageQueue(
    private val scope: CoroutineScope
) {

    val messages = mutableListOf<Pair<Long, Long>>()

    var callback: Callback? = null

    fun addToStack(msg: Message) {
        scope.launch {
            messages.add(msg.chat.id to msg.messageId)
        }
    }


    fun clear(chatId: Long) {
        messages.forEach {
            if (it.first == chatId) {
                callback?.invoke(it)
            }
        }

    }
}