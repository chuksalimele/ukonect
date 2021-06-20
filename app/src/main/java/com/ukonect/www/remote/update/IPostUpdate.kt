package com.ukonect.www.remote.update

import com.ukonect.www.data.msg.PostMessage

interface IPostUpdate {

    fun onPostMessage(postMessage: PostMessage)
    fun onPostMessageComment(postMessage: PostMessage)
    fun onPostModified(postMessage: PostMessage)
    fun onPostDeleted(postMessage: PostMessage)
    fun onPostSent(message_id: String)
    fun onPostSeen(message_id: String)
    fun onPostRead(message_id: String)
}