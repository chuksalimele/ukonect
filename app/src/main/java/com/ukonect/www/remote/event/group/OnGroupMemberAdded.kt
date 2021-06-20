package com.ukonect.www.remote.event.group

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.MemberAdded
import io.reactivex.ObservableEmitter

class OnGroupMemberAdded(private val emitter: ObservableEmitter<MemberAdded>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, MemberAdded::class.java)
        emitter.onNext(data)
    }
}