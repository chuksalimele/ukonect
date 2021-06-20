package com.ukonect.www.remote.event.group

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.MemberRemoved
import io.reactivex.ObservableEmitter

class OnGroupMemberRemoved(private val emitter: ObservableEmitter<MemberRemoved>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, MemberRemoved::class.java)
        emitter.onNext(data)
    }
}