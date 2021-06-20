package com.ukonect.www.remote.event.post

import com.laurencegarmstrong.kwamp.core.messages.Dict
import io.reactivex.ObservableEmitter

class OnPostRead(private val emitter: ObservableEmitter<String>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var message_id =  arguments?.get(0).toString()
        emitter.onNext(message_id)
    }
}