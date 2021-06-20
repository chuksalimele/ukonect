package com.ukonect.www.remote

import com.google.gson.Gson
import com.laurencegarmstrong.kwamp.client.core.ClientImpl
import com.laurencegarmstrong.kwamp.client.core.call.DeferredCallResult
import com.laurencegarmstrong.kwamp.core.Uri
import com.ukonect.www.Ukonect
import com.ukonect.www.Ukonect.Companion.appUser
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Group
import com.ukonect.www.data.GroupJoinRequest
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.data.msg.GroupMessage
import com.ukonect.www.data.msg.PostMessage
import com.ukonect.www.exception.UkonectException
import com.ukonect.www.remote.model.Model
import com.ukonect.www.util.Topic
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import kotlinx.coroutines.runBlocking

class WampCaller internal constructor(private val wampClient: ClientImpl?) {


    private inline fun<reified T>  onCallResult(emitter: ObservableEmitter<T>, callFuture: DeferredCallResult?){
        runBlocking {
            try{

                var callResult = callFuture?.await()

                var json = callResult?.arguments?.get(0).toString()
                var gson =  Gson();
                var result= gson.fromJson(json, Model.Result::class.java)


                Utils.logExternal(Ukonect.context, "onCallResult")//TESTING!!!
                Utils.logExternal(Ukonect.context, json)//TESTING!!!
                Utils.logExternal(Ukonect.context, "-------------------------------")//TESTING!!!
                Utils.logExternal(Ukonect.context, result.data)//TESTING!!!

                if(result.success) {

                    if (T::class == String::class) {
                        emitter.onNext(result.data as T)//we know T is String
                    } else {
                        var obj = gson.fromJson(result.data, T::class.java)
                        emitter.onNext(obj)
                    }
                }else {
                    emitter.onError(
                        UkonectException(result)
                    )
                }


            }catch (ex: Throwable){
                emitter.onError(
                    UkonectException(
                        ex.message,
                        ex
                    )
                )
            }finally {
                emitter.onComplete()
            }
        }

    }

    private inline fun<reified T> onCallResultList(emitter: ObservableEmitter<MutableList<T>>, callFuture: DeferredCallResult?) {

        runBlocking {
            try {

                var callResult = callFuture?.await()

                var json = callResult?.arguments?.get(0).toString()

                var result = Gson().fromJson(json, Model.Result::class.java)

                if (result.success) {
                    var obj = Utils.jsonArrayToList(result.data, T::class.java)
                    emitter.onNext(obj)
                } else {
                    emitter.onError(
                        UkonectException(result)
                    )
                }


            }catch (ex: Throwable){
                emitter.onError(
                    UkonectException(
                        ex.message,
                        ex
                    )
                )
            } finally {
                emitter.onComplete()
            }
        }

    }

    private fun signup(procedure_url:Uri, data: Map<String, String>): Observable<AppUser> {
        return Observable.create { emitter ->
            val callFuture = wampClient?.call(procedure_url, listOf(data))
            onCallResult(emitter, callFuture)
        }
    }

    fun signUpVerifyPhoneNumber(data: Map<String, String>): Observable<AppUser>{
        return signup(Uri("com.ukonectinfo.backend.signup.verify.phone.number"), data);
    }

    fun signUpComfirmPhoneNumber(data: Map<String, String>): Observable<AppUser>{
        return signup(Uri("com.ukonectinfo.backend.signup.confirm.phone.number"), data);
    }

    fun signUpName(data: Map<String, String>): Observable<AppUser>{
        return signup(Uri("com.ukonectinfo.backend.signup.name"), data);
    }

    fun signUpProfilePhoto(data: Map<String, String>): Observable<AppUser>{
        return signup(Uri("com.ukonectinfo.backend.signup.profile.photo"), data);
    }

    fun updateProfile(profile: User): Observable<AppUser>{
        return Observable.create { emitter ->
            val subscribed_topic =
                Topic.userProfileUpdate(profile.user_id)//the backend will use this topic to broadcast
            // to users subscribed to it to notify them
            // that a  user has updated his profile

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.user.update.profile"), listOf(profile, subscribed_topic))
                onCallResult(emitter, callFuture)

        }
    }

    fun getLastSeen(user_id: String): Observable<String>{
        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.user.get.last.seen"), listOf(user_id))
                onCallResult(emitter, callFuture)
        }
    }

    /**
     * Deprecated
     */
    fun updateLastSeen(): Observable<String>{

        return Observable.create { emitter ->
            appUser?.user_id.let {
                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.user.update.last.seen"), listOf(it))
                onCallResult(emitter, callFuture)
            }
        }
    }

    fun fetchChatMessages(): Observable<MutableList<ChatMessage>> {

        return Observable.create { emitter ->
            // Call a remote procedure in the backend.
            val topics = mutableListOf<Uri>()
            appUser.let { app_user ->
                app_user?.contacts?.forEach {
                    if (app_user?.user_id ?: "" != it) {
                        topics.add(Topic.chatMessageUri(app_user, it))
                    }
                }
            }

            val callFuture =
                wampClient?.call(Uri(Topic.BACKEND_FETCH_BY_TOPICS), topics)
            onCallResultList(emitter, callFuture)
        }
    }

    fun fetchGroupMessages(): Observable<MutableList<GroupMessage>> {

        return Observable.create { emitter ->
            // Call a remote procedure in the backend.
            val topics = mutableListOf<Uri>()
            appUser?.groups_belong?.forEach {
                if (appUser?.user_id ?: "" != it) {
                    topics.add(Topic.groupChatMessageUri(it))
                }
            }

            val callFuture =
                wampClient?.call(Uri(Topic.BACKEND_FETCH_BY_TOPICS), topics)

            onCallResultList(emitter, callFuture)
        }
    }

    fun fetchPostMessages(): Observable<MutableList<PostMessage>> {

        return Observable.create { emitter ->
            // Call a remote procedure in the backend.
            val topics = mutableListOf<Uri>()
            appUser?.contacts?.forEach {
                    topics.add(Topic.postMessageUri(it))
            }

            val callFuture =
                wampClient?.call(Uri(Topic.BACKEND_FETCH_BY_TOPICS), topics)

            onCallResultList(emitter, callFuture)
        }
    }

    fun searchUsers(search_str: String, offset: Int, limit: Int): Observable<MutableList<User>> {

        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.search.users"), listOf(search_str, offset, limit))
                onCallResultList(emitter, callFuture)

        }
    }

    fun fetchContacts(contacts_phone_nubers: List<String>): Observable<MutableList<User>> {
        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.user.contacts.fetch"), listOf(contacts_phone_nubers))
                onCallResultList(emitter, callFuture)
        }
    }

    fun updateContacts(contacts_phone_nubers: List<String>): Observable<String> {
        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.user.contacts.update"), listOf(appUser?.user_id ,contacts_phone_nubers))
                onCallResult(emitter, callFuture)
        }
    }

    fun fetchUserGroupsBelong(user_id: String): Observable<MutableList<Group>> {
        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.group.fetch.user.groups.belong"), listOf(user_id))
                onCallResultList(emitter, callFuture)
        }
    }

    fun createGroup(group: Group): Observable<Group> {
        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.group.create"), listOf(appUser?.user_id, group))
                onCallResult(emitter, callFuture)
        }
    }

    fun editGroup(group: Group): Observable<Group> {
        return Observable.create { emitter ->

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.group.edit"), listOf(appUser?.user_id, group))
                onCallResult(emitter, callFuture)
        }
    }

    fun groupMemberLeave(group_name: String): Observable<String> {
        return Observable.create { emitter ->
            val subscribed_topic =
                Topic.groupMemberLeftUri(group_name)//the backend will use this topic to broadcast
            // to users subscribed to it to notify them
            // that a  user has left the group - e.g 23495843850 left

            val callFuture =
                wampClient?.call(
                    Uri("com.ukonectinfo.backend.group.member.leave"),
                    listOf(
                    appUser?.user_id,
                    group_name,
                    subscribed_topic
                ))
            onCallResult(emitter, callFuture)
        }
    }

    fun groupMemberSendLinkToJoin(group_name: String, to_user_id: String): Observable<String> {
        return Observable.create { emitter ->
            var from_user_id = appUser?.user_id;

            var gjr_topic = appUser?.let {
                Topic.chatMessageUri(
                    it,
                    to_user_id
                )
            }//the topic to use to save the message in the backend and for broadcasting to the users

            var gjrChatMsg = ChatMessage()

            from_user_id?.let {
                gjrChatMsg.message_id = Utils.unique();
                gjrChatMsg.from_user_id = from_user_id;
                gjrChatMsg.to_user_id = to_user_id;
                gjrChatMsg.group_join_request =
                    GroupJoinRequest(from_user_id, to_user_id, group_name)
            }

            val callFuture =
                wampClient?.call(
                    Uri("com.ukonectinfo.backend.group.member.send.link.to.join"),
                    listOf(
                    gjrChatMsg,
                    gjr_topic,//backend will use this topic to store the message
                    wampClient.getSessionId()//the backed will exclude this wampClient id so as to send only to the desired user
                ))

            onCallResult(emitter, callFuture)
        }
    }

    fun groupMemberJoinByLink(group_name: String, invitee_user_id: String): Observable<String> {
        return Observable.create { emitter ->
            val subscribed_topic =
                Topic.groupMemberAddedUri(group_name)//the backend will use this topic to broadcast
            // to users subscribed to it to notify them of
            // that a new user is added to the group e.g 23495843850 added

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.group.member.join.by.link"),
                        listOf(
                        appUser?.user_id,//the one invited to join the group
                        invitee_user_id,//the one who invited the other user
                        group_name,
                        subscribed_topic
                    ))
                onCallResult(emitter, callFuture)
        }
    }

    fun groupAdminAddMember(group_name: String, new_member_user_id: String): Observable<String> {
        return Observable.create { emitter ->
            val subscribed_topic =
                Topic.groupMemberAddedUri(group_name)//the backend will use this topic to broadcast
                                                    // to users subscribed to it to notify them of
                                                    // that a new user is added to the group e.g 23495843850 added by Peter Emma

                val callFuture =
                    wampClient?.call(
                        Uri("com.ukonectinfo.backend.group.admin.add.member"),
                        listOf(
                        appUser?.user_id,
                        new_member_user_id,
                        group_name,
                        subscribed_topic
                    ))
                onCallResult(emitter, callFuture)
        }
    }

    fun groupAdminRemoveMember(group_name: String, member_user_id: String): Observable<String> {
        return Observable.create { emitter ->
            val subscribed_topic =
                Topic.groupMemberRemovedUri(group_name)//the backend will use this topic to broadcast
            // to users subscribed to it to notify them of
            // that a  user been removed from the group - e.g 23495843850 removed by Peter Emma

                val callFuture =
                    wampClient?.call(Uri("com.ukonectinfo.backend.group.admin.remove.member"),
                        listOf(
                        appUser?.user_id,
                        member_user_id,
                        group_name,
                        subscribed_topic
                    ))
                onCallResult(emitter, callFuture)
        }
    }

}