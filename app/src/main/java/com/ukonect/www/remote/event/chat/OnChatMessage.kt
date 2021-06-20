package com.ukonect.www.remote.event.chat

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.msg.ChatMessage
import io.reactivex.ObservableEmitter

class OnChatMessage(private val emitter: ObservableEmitter<ChatMessage>)  {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, ChatMessage::class.java)
         emitter.onNext(data)
    }

}