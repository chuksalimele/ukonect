package com.ukonect.www.remote

import com.laurencegarmstrong.kwamp.client.core.ClientImpl
import com.ukonect.www.Ukonect
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.data.msg.GroupMessage
import com.ukonect.www.data.msg.PostMessage
import com.ukonect.www.util.Topic
import io.reactivex.Observable

class WampPublisher internal constructor(private val  wampClient: ClientImpl?) {

    companion object {

        @JvmStatic
        internal fun sendChatSeen(
            wampClient: ClientImpl?,
            msg_id: String,
            other_user_id: String
        ): Observable<Any>? {

            val app_user = Ukonect.appUser ?: AppUser()
            if (app_user.user_id.isEmpty()) {
                return null
            }
            return Observable.create { emitter ->

                try {
                    val pubFuture =
                        wampClient?.publish(Topic.chatSeenUri(app_user, other_user_id), listOf(msg_id))
                    emitter.onNext(true)
                } catch (ex: Throwable) {
                    emitter.onError(ex);
                } finally {
                    emitter.onComplete()
                }
            }
        }

    }


    fun chatMessgae(chat: ChatMessage) : Observable<Any>?{
        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->

            val other = if(chat.from_user_id != app_user?.user_id)
                chat.from_user_id
            else
                chat.to_user_id

            try{
                val pubFuture =
                    wampClient?.publish(Topic.chatMessageUri(app_user, other), listOf(chat))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun chatModifiec(chat: ChatMessage) : Observable<Any>?{
        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->

            val other = if(chat.from_user_id != app_user?.user_id)
                chat.from_user_id
            else
                chat.to_user_id

            try{
                val pubFuture =
                    wampClient?.publish(Topic.chatModifiedUri(app_user, other), listOf(chat))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun chatDeleted(msg_id:String, other_user_id: String) : Observable<Any>? {

        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.chatDeletedUri(app_user, other_user_id), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun chatSeen(msg_id:String, other_user_id: String) : Observable<Any>? {
        return WampPublisher.sendChatSeen(wampClient, msg_id, other_user_id)
    }

    fun chatRead(msg_id:String, other_user_id: String) : Observable<Any>? {

        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.chatReadUri(app_user, other_user_id), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }


    fun groupChatMessgae(groupChat: GroupMessage)  : Observable<Any> {

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.groupChatMessageUri(groupChat.group_name), listOf(groupChat))

                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun groupChatModified(groupChat: GroupMessage)  : Observable<Any> {

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.groupChatModifiedUri(groupChat.group_name), listOf(groupChat))

                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun groupChatDeleted(msg_id:String, group_name: String) : Observable<Any> {

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.groupChatDeletedUri(group_name), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun groupChatSeen(msg_id:String, group_name: String) : Observable<Any> {

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.groupChatSeenUri(group_name), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun groupChatRead(msg_id:String, group_name: String)  : Observable<Any> {

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.groupChatReadUri(group_name), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun postMessage(post: PostMessage)  : Observable<Any>? {

        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.postMessageUri(app_user.user_id), listOf(post))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun postModified(post: PostMessage)  : Observable<Any>? {

        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }

        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.postModifiedUri(app_user.user_id), listOf(post))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun postDeleted(msg_id: String)  : Observable<Any>? {

        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.postDeletedUri(app_user.user_id), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun postSeen(msg_id: String)  : Observable<Any>? {

        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->

            try{
                val pubFuture =
                    wampClient?.publish(Topic.postSeenUri(app_user.user_id), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun postRead(msg_id: String)  : Observable<Any>? {
        val app_user = Ukonect.appUser ?: AppUser()
        if (app_user.user_id.isEmpty()){
            return null
        }
        return Observable.create { emitter ->
            try{
                val pubFuture =
                    wampClient?.publish(Topic.postReadUri(app_user.user_id), listOf(msg_id))
                emitter.onNext(true)
            }catch (ex: Throwable){
                emitter.onError(ex);
            }finally {
                emitter.onComplete()
            }
        }
    }

    fun userProfileUpdate(user: User?)  : Observable<Any>? {

        if (user == null) {
            return null
        }

        if (user.location.time == null) {
            return null
        }

        return Observable.create { emitter ->
            try {
                val pubFuture =
                    wampClient?.publish(Topic.userProfileUpdate(user.user_id), listOf(user))

                emitter.onNext(true)
            } catch (ex: Throwable) {
                emitter.onError(ex);
            } finally {
                emitter.onComplete()
            }
        }
    }

    fun locationChange(user: User?)  : Observable<Any>? {

        if (user == null) {
            return null
        }

        if (user.location.time == null) {
            return null
        }

        return Observable.create { emitter ->

            try {

                val pubFuture =
                    wampClient?.publish(Topic.locationChangeUri(user.user_id), listOf(user.location))

                emitter.onNext(true)
            } catch (ex: Throwable) {
                emitter.onError(ex);
            } finally {
                emitter.onComplete()
            }
        }
    }

}