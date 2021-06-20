package com.ukonect.www.ui.post

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.ukonect.www.data.Message
import com.ukonect.www.data.msg.PostMessage
import com.ukonect.www.remote.WampService
import com.ukonect.www.remote.update.IPostUpdate
import com.ukonect.www.ui.AbstractListViewModel
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class PostViewModel  : AbstractListViewModel<PostMessage>(),
    IPostUpdate {
    private val post_list: MutableLiveData<MutableList<PostMessage>> by lazy {
        MutableLiveData<MutableList<PostMessage>>().also {
            load(it)
        }
    }
    var context: Context? = null
    var wampService: WampService? = null
    var prvDisposable: Disposable? = null
    var grpDisposable: Disposable? = null


    override fun getList(): MutableLiveData<MutableList<PostMessage>> {
        return post_list
    }

    fun load(it: MutableLiveData<MutableList<PostMessage>>) {
        prvDisposable = wampService?.getCaller()
            ?.fetchPostMessages()//private post
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe (
                {result->
                    onSuccess(it, result)
                },
                {error->

                })

    }

    fun onSuccess(it: MutableLiveData<MutableList<PostMessage>>, messages : MutableList<PostMessage>){

        var d = Observable.fromCallable {
            it.value?.addAll(messages)
        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe()


    }

    override fun onPostMessage(postMessage: PostMessage) {
        post_list.value?.add(postMessage)
        post_list.postValue(post_list.value)
    }

    override fun onPostMessageComment(postMessage: PostMessage) {
        post_list.value?.add(postMessage)
        post_list.postValue(post_list.value)
    }

    override fun onPostModified(postMessage: PostMessage) {
        val index: Int? = post_list.value?.indexOfLast{
            it.message_id == postMessage.message_id
        }

        if (index == null || index < 0) {
            return;
        }

        post_list.value?.set(index, postMessage)

        post_list.postValue(post_list.value)
    }

    override fun onPostDeleted(postMessage: PostMessage) {
        val index: Int? = post_list.value?.indexOfLast{
            it.message_id == postMessage.message_id
        }

        if (index == null || index < 0) {
            return;
        }

        post_list.value?.set(index, postMessage)

        post_list.postValue(post_list.value)
    }

    override fun onPostSent(message_id: String) {
        val index: Int? = post_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        post_list.value?.get(index)?.status = Message.MSG_STATUS_SENT

        post_list.postValue(post_list.value)
    }

    override fun onPostSeen(message_id: String) {
        val index: Int? = post_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        post_list.value?.get(index)?.status = Message.MSG_STATUS_SEEN

        post_list.postValue(post_list.value)
    }

    override fun onPostRead(message_id: String) {
        val index: Int? = post_list.value?.indexOfLast{
            it.message_id == message_id
        }

        if (index == null || index < 0) {
            return;
        }

        post_list.value?.get(index)?.status = Message.MSG_STATUS_READ

        post_list.postValue(post_list.value)
    }

}
