package com.ukonect.www.remote

import com.laurencegarmstrong.kwamp.client.core.ClientImpl
import com.laurencegarmstrong.kwamp.client.core.pubsub.SubscriptionHandle
import com.ukonect.www.Ukonect
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.*
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.data.msg.GroupMessage
import com.ukonect.www.data.msg.PostMessage
import com.ukonect.www.remote.event.*
import com.ukonect.www.remote.event.chat.*
import com.ukonect.www.remote.event.group.*
import com.ukonect.www.remote.event.post.*
import com.ukonect.www.remote.update.*
import com.ukonect.www.util.Topic
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class WampSubscriber internal constructor(private val wampClient: ClientImpl?) {

    internal var locationUpdater: ILocationUpdate? = null
    internal var groupUpdater: IGroupUpdate? = null
    internal var postUpdater: IPostUpdate? = null
    internal var chatUpdater: IChatUpdate? = null
    internal var userProfileUpdater: IUserProfileUpdate? = null
    internal var userOnlineStatusUpdater: IUserOnlineStatusUpdate? = null
    private var locationChangeSubsciptions = mutableMapOf<String, SubscriptionHandle?>()

    private val disposables = CompositeDisposable()

    internal var isJoin = false
    internal var users: MutableList<User>? = null
    internal val group_names get() = Ukonect.appUser?.groups_belong?: mutableListOf<String>()
    internal var locationUsers: MutableList<User>? = null


    private fun getOnError(): Consumer<Throwable> {
        return Consumer{error->

        }
    }

    fun subscribeChat(users: MutableList<User>) {
        this.users = users
        if (!isJoin) {
            return
        }
        users.forEach {

            Ukonect.appUser?.let { app_user ->
                disposables.add(
                    subscribeChatMessage(app_user, it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            //first send chat seen event message
                            WampPublisher.sendChatSeen(wampClient, result.message_id, result.from_user_id)
                            //now notify the observers
                            chatUpdater?.onChatMessage(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribeChatSent(app_user, it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            chatUpdater?.onChatSent(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribeChatSeen(app_user, it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            chatUpdater?.onChatSeen(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribeChatRead(app_user, it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            chatUpdater?.onChatRead(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribeChatModified(app_user, it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            chatUpdater?.onChatModified(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribeChatDeleted(app_user, it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            chatUpdater?.onChatDeleted(result)
                        }, getOnError())
                )

            }

        }

    }
    fun subscribeGroup(group_names: MutableList<String>) {
        if (!isJoin) {
            return
        }

        group_names.forEach {
            disposables.add(
                subscribeGroupEdited(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupEdited(result)
                    }, getOnError())
            )

            disposables.add(
                subscribeGroupMemberAdded(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupMemberAdded(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupMemberRemoved(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupMemberRemoved(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupMemberLeft(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupMemberLeft(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupChatMessage(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupChatMessage(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupChatSent(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupChatSent(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupChatSeen(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupChatSeen(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupChatRead(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupChatRead(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupChatModified(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupChatModified(result)
                    }, getOnError())
            )
            disposables.add(
                subscribeGroupChatDeleted(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe (Consumer{ result->
                        groupUpdater?.onGroupChatDeleted(result)
                    }, getOnError())
            )


        }
    }

    fun subscribePost(users: MutableList<User>) {
        this.users = users
        if (!isJoin) {
            return
        }

        users.forEach {

                disposables.add(
                    subscribePostMessage(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostMessage(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribePostMessageComment(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostMessageComment(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribePostSent(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostSent(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribePostSeen(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostSeen(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribePostRead(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostRead(result)
                        }, getOnError())
                )

                disposables.add(
                    subscribePostModified(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostModified(result)
                        }, getOnError())
                )
                disposables.add(
                    subscribePostDeleted(it.user_id) // Run on a background thread
                        .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                        .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                        .subscribe (Consumer{ result->
                            postUpdater?.onPostDeleted(result)
                        }, getOnError())
                )


        }

    }

    fun subscribeUserProfile(users: MutableList<User>) {
        this.users = users
        if (!isJoin) {
            return
        }

        users.forEach {

            disposables.add(
                subscribeUserProfileUpdate(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe(Consumer { result ->
                        userProfileUpdater?.onProfileUpdate(result)
                    }, getOnError())
            )
        }

    }

    fun subscribeUserOnlineStatus(users: MutableList<User>) {
        this.users = users
        if (!isJoin) {
            return
        }

        users.forEach {

            disposables.add(
                subscribeUserOnlineStatusUpdate(it) // Run on a background thread
                    .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                    .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                    .subscribe(Consumer { result ->
                        userOnlineStatusUpdater?.onOnlineStatus(result)
                    }, getOnError())
            )
        }

    }

    fun subscribeLocation(user: User) {
        var index = this.locationUsers?.indexOfLast { it.user_id == user.user_id } ?:-1
        if(index > -1) {
            return//leave since we have already subscribed to it
        }

        this.locationUsers?.add(user)

        if (!isJoin) {
            return
        }

        disposables.add(
            subscribeLocationChange(user) // Run on a background thread
                .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
                .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
                .subscribe (Consumer{ result->
                    locationUpdater?.onLocationChange(result)
                }, getOnError())
        )

    }

    fun unsubscribeLocation(user: User) {
        var index = this.locationUsers?.indexOfLast { it.user_id == user.user_id } ?:-1
        if(index > -1) {
            return//leave since we have already subscribed to it
        }

        this.locationUsers?.remove(user)

        if (!isJoin) {
            return
        }

        unsubscribeLocationChange(user) // Run on a background thread
            .subscribeOn(Schedulers.io()) //Schedule on the io changing threads
            .observeOn(AndroidSchedulers.mainThread())// Be notified on the main thread
            .subscribe (Consumer{ result->

            }, getOnError())
    }

    private fun subscribeChatMessage(app_user: AppUser, other_user_id: String): Observable<ChatMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.chatMessageUri(app_user, other_user_id)
               val event = OnChatMessage(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }


    private fun subscribeChatSent(app_user: AppUser, other_user_id: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                val topic = Topic.chatSentUri(app_user, other_user_id)
                val event = OnChatSent(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeChatSeen(app_user: AppUser, other_user_id: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                val topic = Topic.chatSeenUri(app_user, other_user_id)
                val event = OnChatSeen(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeChatRead(app_user: AppUser, other_user_id: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.chatReadUri(app_user, other_user_id)
                val event = OnChatRead(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeChatModified(app_user: AppUser, other_user_id: String): Observable<ChatMessage> {

        return Observable.create { emitter ->
            try {
                val topic = Topic.chatModifiedUri(app_user, other_user_id)
                val event = OnChatModified(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeChatDeleted(app_user: AppUser, other_user_id: String): Observable<ChatMessage> {

        return Observable.create { emitter ->
            try {
                val topic = Topic.chatDeletedUri(app_user, other_user_id)
                val event = OnChatDeleted(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupEdited(group_name: String): Observable<Pair<Group, GroupEdited>> {

        return Observable.create { emitter ->
            try {
                val topic = Topic.groupEditedUri(group_name)
                val event = OnGroupEdited(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupMemberAdded(group_name: String): Observable<MemberAdded> {

        return Observable.create { emitter ->
            try {
                val topic = Topic.groupMemberAddedUri(group_name)
                val event = OnGroupMemberAdded(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupMemberRemoved(group_name: String): Observable<MemberRemoved> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupMemberRemovedUri(group_name)
                val event = OnGroupMemberRemoved(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupMemberLeft(group_name: String): Observable<MemberLeft> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupMemberLeftUri(group_name)
                val event = OnGroupMemberLeft(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupChatMessage(group_name: String): Observable<GroupMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupChatMessageUri(group_name)
                val event = OnGroupChatMessage(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupChatSent(group_name: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupChatSentUri(group_name)
                val event = OnGroupChatSent(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupChatSeen(group_name: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupChatSeenUri(group_name)
                val event = OnGroupChatSeen(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupChatRead(group_name: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupChatReadUri(group_name)
                val event = OnGroupChatRead(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupChatModified(group_name: String): Observable<GroupMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupChatModifiedUri(group_name)
                val event = OnGroupChatModified(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeGroupChatDeleted(group_name: String): Observable<GroupMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.groupChatDeletedUri(group_name)
                val event = OnGroupChatDeleted(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostMessage(user_id: String): Observable<PostMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postMessageUri(user_id)
                val event = OnPostMessage(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostMessageComment(user_id: String): Observable<PostMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postMessageCommentUri(user_id)
                val event = OnPostMessageComment(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostSent(user_id: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postSentUri(user_id)
                val event = OnPostSent(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostSeen(user_id: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postSeenUri(user_id)
                val event = OnPostSeen(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostRead(user_id: String): Observable<String> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postReadUri(user_id)
                val event = OnPostRead(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostModified(user_id: String): Observable<PostMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postModifiedUri(user_id)
                val event = OnPostModified(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribePostDeleted(user_id: String): Observable<PostMessage> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.postDeletedUri(user_id)
                val event = OnPostDeleted(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }


    private fun subscribeLocationChange(user: User): Observable<UserLocation> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.locationChangeUri(user.user_id)
                val event = OnLocationChange(emitter)
                val subscription: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)

                locationChangeSubsciptions.put(topic.text , subscription)

            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun unsubscribeLocationChange(user: User): Observable<UserLocation> {
        return Observable.create { emitter ->
            try {
                var topic = Topic.locationChangeUri(user.user_id)
                locationChangeSubsciptions[topic.text]?.unsubscribe()
                locationChangeSubsciptions.remove(topic.text)

            } catch (ex: Throwable) {
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeUserProfileUpdate(user: User): Observable<User> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.userProfileUpdate(user.user_id)
                val event = OnUserProfileUpdate(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)
                
            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }

    private fun subscribeUserOnlineStatusUpdate(user: User): Observable<OnlineStatus> {

        return Observable.create { emitter ->
            try {
                var topic = Topic.userOnlineStatus(user.user_id)
                val event = OnUserOnlineStatus(emitter)
                val subFuture: SubscriptionHandle? =wampClient?.subscribe(topic, event::processEvent)

            }catch (ex: Throwable){
                emitter.onError(ex)
            }
        }
    }
}


