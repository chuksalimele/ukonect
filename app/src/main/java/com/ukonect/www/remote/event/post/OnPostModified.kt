package com.ukonect.www.remote.event.post

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.msg.PostMessage
import io.reactivex.ObservableEmitter

class OnPostModified(private val emitter: ObservableEmitter<PostMessage>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, PostMessage::class.java)
        emitter.onNext(data)
    }
}