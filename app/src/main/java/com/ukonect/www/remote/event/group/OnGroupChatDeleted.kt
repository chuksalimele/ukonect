package com.ukonect.www.remote.event.group

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.msg.GroupMessage
import io.reactivex.ObservableEmitter

class OnGroupChatDeleted(private val emitter: ObservableEmitter<GroupMessage>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, GroupMessage::class.java)
        emitter.onNext(data)
    }

}