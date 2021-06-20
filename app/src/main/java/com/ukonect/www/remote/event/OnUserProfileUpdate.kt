package com.ukonect.www.remote.event

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.User
import io.reactivex.ObservableEmitter

class OnUserProfileUpdate (private val emitter: ObservableEmitter<User>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, User::class.java)
        emitter.onNext(data)
    }
}