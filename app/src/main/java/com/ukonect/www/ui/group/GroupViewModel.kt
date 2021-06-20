package com.ukonect.www.ui.group

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ukonect.www.Ukonect.Companion.appUser
import com.ukonect.www.data.*
import com.ukonect.www.data.msg.GroupMessage
import com.ukonect.www.remote.WampService
import com.ukonect.www.remote.update.IGroupUpdate
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class GroupViewModel: ViewModel(), IGroupUpdate {
    private val group_list: MutableLiveData<MutableList<Group>> by lazy {
        MutableLiveData<MutableList<Group>>().also {
            loadGroups(it)
        }
    }
    private val group_chat_list: MutableLiveData<MutableList<GroupMessage>> by lazy {
        MutableLiveData<MutableList<GroupMessage>>()
    }

    var context: Context? = null
    var wampService: WampService? = null
    var chtDisposable: Disposable? = null
    var grpDisposable: Disposable? = null


    fun getGroupList(): MutableLiveData<MutableList<Group>> {
        return group_list
    }

    fun getChatList(): MutableLiveData<MutableList<GroupMessage>> {
        return group_chat_list
    }

    fun loadGroups(it: MutableLiveData<MutableList<Group>>) {
        grpDisposable =wampService?.getCaller()?.fetchUserGroupsBelong(appUser?.user_id?:"")
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe (
                {result->
                    onSuccessGroup(it, result)
                },
                {error->

                })//group chat
    }

    fun loadChats(it: MutableLiveData<MutableList<GroupMessage>>) {
        chtDisposable =wampService?.getCaller()?.fetchGroupMessages()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe (
                {result->
                    onSuccessChat(it, result)
                },
                {error->

                })//group chat
    }

    fun onSuccessChat(it: MutableLiveData<MutableList<GroupMessage>>, messages : MutableList<GroupMessage>){

        var d = Observable.fromCallable {
            it.value?.addAll(messages)
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    }

    fun onSuccessGroup(it: MutableLiveData<MutableList<Group>>, groups : MutableList<Group>){

        var d = Observable.fromCallable {
            it.value?.addAll(groups)
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()

    }

    override fun onGroupEdited(pair: Pair<Group, GroupEdited>) {
        var group = pair.first
        var group_edited = pair.second
        val index: Int? = group_list.value?.indexOfLast{
            it.name == group.name
        }

        if (index == null || index < 0) {
            return;
        }

        group_list.value?.set(index, group)

        group_list.postValue(group_list.value)

        //create a group chat message for this event like in WhatsApp
        var message = GroupMessage()
        message.message_id = Utils.unique()
        message.group_name = group_edited.new_group_name
        message.group_edited = group_edited

        message.text = "Group edited by ${group_edited.edited_by}\n"

        if(group_edited.new_group_name != group_edited.old_group_name){
            message.text += "Name changed from ${group_edited.old_group_name} to ${group_edited.new_group_name}\n"
        }

        if(group_edited.new_status_text != group_edited.old_status_text){
            message.text += "Status changed from ${group_edited.new_status_text} to ${group_edited.old_status_text}\n"
        }

        group_chat_list.value?.add(message)
        group_chat_list.postValue(group_chat_list.value)

    }

    override fun onGroupMemberAdded(member_added: MemberAdded) {
        var message = GroupMessage()
        message.message_id = Utils.unique()
        message.group_name = member_added.group_name
        message.member_added = member_added

        group_chat_list.value?.add(message)
        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupMemberRemoved(member_removed: MemberRemoved) {
        var message = GroupMessage()
        message.message_id = Utils.unique()
        message.group_name = member_removed.group_name
        message.member_removed = member_removed

        group_chat_list.value?.add(message)
        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupMemberLeft(member_left: MemberLeft) {
        var message = GroupMessage()
        message.message_id = Utils.unique()
        message.group_name = member_left.group_name
        message.member_left = member_left

        group_chat_list.value?.add(message)
        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupChatMessage(message: GroupMessage) {
        group_chat_list.value?.add(message)
        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupChatModified(message: GroupMessage) {
        val index: Int? = group_chat_list.value?.indexOfLast{
            it.message_id == message.message_id
        }

        if (index == null || index < 0) {
            return;
        }

        group_chat_list.value?.set(index, message)

        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupChatDeleted(message: GroupMessage) {
        val index: Int? = group_chat_list.value?.indexOfLast{
            it.message_id == message.message_id
        }

        if (index == null || index < 0) {
            return;
        }

        group_chat_list.value?.set(index, message)

        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupChatSent(message_id: String) {
        val index: Int? = group_chat_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        group_chat_list.value?.get(index)?.status = Message.MSG_STATUS_SENT

        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupChatSeen(message_id: String) {
        val index: Int? = group_chat_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        group_chat_list.value?.get(index)?.status = Message.MSG_STATUS_SEEN

        group_chat_list.postValue(group_chat_list.value)
    }

    override fun onGroupChatRead(message_id: String) {
        val index: Int? = group_chat_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        group_chat_list.value?.get(index)?.status = Message.MSG_STATUS_READ

        group_chat_list.postValue(group_chat_list.value)
    }

}
