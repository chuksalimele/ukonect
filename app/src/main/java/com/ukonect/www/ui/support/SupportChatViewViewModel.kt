package com.ukonect.www.ui.support

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.dummy.Dummy
import com.ukonect.www.ui.AbstractListViewModel

class SupportChatViewViewModel : AbstractListViewModel<ChatMessage>() {
    private val support_chat_list: MutableLiveData<MutableList<ChatMessage>> by lazy {
        MutableLiveData<MutableList<ChatMessage>>().also {
            load(it)
        }
    }

    var context: Context? = null
    var app_user_chatmate: User? = null

    override fun getList(): MutableLiveData<MutableList<ChatMessage>> {
        return support_chat_list
    }

    fun load(it: MutableLiveData<MutableList<ChatMessage>>) {
        // Do an asynchronous operation to fetch users.

        //TODO - REPLACE DUMMY TEST WITH REAL DATA

        it.value = context?.let { it1 -> Dummy(it1).getTestChatMessageList(50) }
    }
}
