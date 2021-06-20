package com.ukonect.www.remote.event

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.UserLocation
import io.reactivex.ObservableEmitter

class OnLocationChange(private val emitter: ObservableEmitter<UserLocation>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var json =  arguments?.get(0).toString()
        var data = Gson().fromJson(json, UserLocation::class.java)
        emitter.onNext(data)
    }
}