package com.ukonect.www.remote.update

import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.remote.WampSubscriber


interface IChatUpdate {
    fun onChatMessage(chatMessage: ChatMessage)
    fun onChatModified(chatMessage: ChatMessage)
    fun onChatDeleted(chatMessage: ChatMessage)
    fun onChatSent(message_id: String)
    fun onChatSeen(message_id: String)
    fun onChatRead(message_id: String)

}