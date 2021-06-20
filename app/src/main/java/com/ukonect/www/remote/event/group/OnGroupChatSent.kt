package com.ukonect.www.remote.event.group

import com.laurencegarmstrong.kwamp.core.messages.Dict
import io.reactivex.ObservableEmitter

class OnGroupChatSent(private val emitter: ObservableEmitter<String>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        emitter.onNext(json)
    }
}