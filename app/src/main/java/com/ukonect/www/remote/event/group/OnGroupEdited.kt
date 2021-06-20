package com.ukonect.www.remote.event.group

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.data.Group
import com.ukonect.www.data.GroupEdited
import io.reactivex.ObservableEmitter

class OnGroupEdited(private val emitter: ObservableEmitter<Pair<Group, GroupEdited>>) {
    fun processEvent(arguments: List<Any?>?, argumentsKw: Dict?) {
        var group_json = arguments?.get(0).toString()
        var group_edited_json = arguments?.get(1).toString()
        var data_group = Gson().fromJson(group_json, Group::class.java)
        var data_group_edited = Gson().fromJson(group_edited_json, GroupEdited::class.java)

        emitter.onNext(Pair(data_group, data_group_edited))
    }
}