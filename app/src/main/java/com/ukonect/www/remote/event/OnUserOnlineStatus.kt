package com.ukonect.www.remote.event

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.OnlineStatus
import io.reactivex.ObservableEmitter

class OnUserOnlineStatus (private val emitter: ObservableEmitter<OnlineStatus>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, OnlineStatus::class.java)
        emitter.onNext(data)
    }
}