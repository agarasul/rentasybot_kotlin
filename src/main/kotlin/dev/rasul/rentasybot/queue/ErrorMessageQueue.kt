package dev.rasul.rentasybot.queue

import dev.rasul.rentasybot.models.ErrorMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap

class ErrorMessageQueue(
    private val scope: CoroutineScope
) {


    private var errorShown = ConcurrentHashMap<Long, Boolean>()
    private val _messages = MutableSharedFlow<ErrorMessage>(onBufferOverflow = BufferOverflow.SUSPEND)

    val messages: SharedFlow<ErrorMessage> = _messages

    fun addToStack(msg: ErrorMessage) {
        scope.launch {
            if (!errorShown.containsKey(msg.chatId)) {
                errorShown[msg.chatId] = true
                _messages.emit(msg)
                errorShown.remove(msg.chatId)
            }
        }
    }
}