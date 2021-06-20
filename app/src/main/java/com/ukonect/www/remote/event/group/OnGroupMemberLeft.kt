package com.ukonect.www.remote.event.group

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.MemberLeft
import io.reactivex.ObservableEmitter

class OnGroupMemberLeft(private val emitter: ObservableEmitter<MemberLeft>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, MemberLeft::class.java)
        emitter.onNext(data)
    }
}