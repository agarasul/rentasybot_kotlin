package dev.rasul.rentasybot.queue

import dev.rasul.rentasybot.models.AdMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class AdsMessageQueue(
    private val scope: CoroutineScope
) {

    private val _messages = MutableSharedFlow<AdMessage>(onBufferOverflow = BufferOverflow.SUSPEND)

    val messages : SharedFlow<AdMessage> = _messages

    fun addToStack(adMessage: AdMessage) {
        scope.launch {
            _messages.emit(adMessage)
        }
    }
}