package com.ukonect.www.ui.chat

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.ukonect.www.Ukonect
import com.ukonect.www.data.*
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.data.msg.GroupMessage
import com.ukonect.www.remote.WampService
import com.ukonect.www.remote.update.IChatUpdate
import com.ukonect.www.remote.update.IGroupUpdate
import com.ukonect.www.ui.AbstractListViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.everything.providers.android.contacts.Contact

class PrivateChatViewModel : AbstractListViewModel<ChatMessage>(),
    IChatUpdate {
    private val chat_list: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().also {
            load(it)
        }
    }
    var context: Context? = null
    var wampService: WampService? = null
    var prvDisposable: Disposable? = null
    var grpDisposable: Disposable? = null


    override fun getList(): MutableLiveData<MutableList<ChatMessage>> {
        return chat_list
    }

    fun load(it: MutableLiveData<MutableList<ChatMessage>>) {
        prvDisposable = wampService?.getCaller()
            ?.fetchChatMessages()//private chat
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { result ->
                    onSuccess(it, result)
                },
                { error ->

                })

    }

    fun onSuccess(it: MutableLiveData<MutableList<ChatMessage>>, messages : MutableList<ChatMessage>){

        var d =Observable.fromCallable {
            it.value?.addAll(messages)
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()


    }

    override fun onChatMessage(chatMessage: ChatMessage) {
        chat_list.value?.add(chatMessage)
        chat_list.postValue(chat_list.value)
    }

    override fun onChatModified(chatMessage: ChatMessage) {
        val index: Int? = chat_list.value?.indexOfLast{
            it.message_id == chatMessage.message_id
        }

        if (index == null || index < 0) {
            return;
        }

        chat_list.value?.set(index, chatMessage)

        chat_list.postValue(chat_list.value)
    }

    override fun onChatDeleted(chatMessage: ChatMessage) {
        val index: Int? = chat_list.value?.indexOfLast{
            it.message_id == chatMessage.message_id
        }

        if (index == null || index < 0) {
            return;
        }

        chat_list.value?.set(index, chatMessage)

        chat_list.postValue(chat_list.value)
    }

    override fun onChatSent(message_id: String) {
        val index: Int? = chat_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        chat_list.value?.get(index)?.status = Message.MSG_STATUS_SENT

        chat_list.postValue(chat_list.value)
    }

    override fun onChatSeen(message_id: String) {
        val index: Int? = chat_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        chat_list.value?.get(index)?.status = Message.MSG_STATUS_SEEN

        chat_list.postValue(chat_list.value)
    }

    override fun onChatRead(message_id: String) {
        val index: Int? = chat_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        chat_list.value?.get(index)?.status = Message.MSG_STATUS_READ

        chat_list.postValue(chat_list.value)
    }

}