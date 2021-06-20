package com.ukonect.www.ui.main

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ukonect.www.data.User
import com.ukonect.www.ui.AbstractListViewModel
import com.ukonect.www.util.Utils
import com.google.gson.Gson
import com.ukonect.www.data.OnlineStatus
import com.ukonect.www.data.UserLocation
import com.ukonect.www.remote.WampService
import com.ukonect.www.remote.update.ILocationUpdate
import com.ukonect.www.remote.update.IUserOnlineStatusUpdate
import com.ukonect.www.remote.update.IUserProfileUpdate
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import me.everything.providers.android.contacts.Contact


class UserListModel : AbstractListViewModel<User>(), ILocationUpdate, IUserProfileUpdate, IUserOnlineStatusUpdate {

    val gson = Gson()
    var phoneContacts: MutableList<Contact>? = mutableListOf<Contact>()
    var context: Context? = null

    var wampService: WampService? = null
    var disposable: Disposable? = null

    val userList: MutableLiveData<MutableList<User>> by lazy {
        MutableLiveData<MutableList<User>>().also {
            load(it)
        }
    }

    override fun getList(): MutableLiveData<MutableList<User>> {
        return this.userList
    }

    fun getUser(user_id: String?): User?{
        var fl = userList.value?.filter { it.user_id == user_id }
        return if (fl!=null && fl.isNotEmpty()) fl[0] else null
    }

    fun updateUser(user: User){
        this.viewModelScope.launch {
            var index = userList.value?.indexOfLast{it.user_id == user.user_id}?: -1
            if(index > -1){
                userList.value?.set(index, user);
                userList.postValue(userList.value)
            }
        }
    }

    fun load(it: MutableLiveData<MutableList<User>>) {
        // Do an asynchronous operation to fetch users from the server.

        var result = mutableListOf<User>()

        /*(if(phoneContacts == null || phoneContacts?.size == 0) {//TODO -  TO BE REMOVED AFTER TESTING
             result =
                 context?.let { it1 -> Dummy(it1).getTestUserList(50) }?: result//TODO -  TO BE REMOVED AFTER TESTING
        }*/


        //load users remotely

        var contacts_1E64_phones = mutableListOf<String>()

        if(phoneContacts != null && context != null) {
            for (contact in phoneContacts!!) {
                val phoneNo = Utils.reformPhoneNumber(context!!, contact.phone)
                contacts_1E64_phones.add(phoneNo.numberE164)

            }
        }

        var observable = wampService?.getCaller()?.fetchContacts(contacts_1E64_phones)
        handleObservable(observable, it)
    }

    private fun onLoadUsers(users: MutableList<User>, it: MutableLiveData<MutableList<User>>){

        //merge phone contacts to users loaded
        disposable = Observable.fromCallable{

            if(phoneContacts != null && context != null) {
                for (contact in phoneContacts!!) {
                    var foundUser: User? = null
                    val phoneNo = Utils.reformPhoneNumber(context!!, contact.phone)
                    var numberE164 = phoneNo.numberE164
                    for (user in users) {
                        if (user.user_id ==  numberE164) {
                            foundUser = user
                            break;
                        }
                    }

                    if (foundUser != null) {//user is registered
                        // use the contact displayName like in other app e.g WhatsApp
                        foundUser.display_name = contact.displayName
                        foundUser.is_contact = true
                    } else {//the contact is unregistered with us
                        //create unregistered user from contact

                        var user = User()//by default the user is unregistered
                        user.user_id =  numberE164 // phone no.
                        user.mobile_phone_no = contact.phone
                        user.personal_email = if( Utils.isValidEmail(contact.email) ) contact.email else user.personal_email
                        user.display_name = contact.displayName
                        user.is_contact = true
                        users.add(user) // add the unregistered contact user
                    }
                }
            }

            //sort in ascending order
            users.sortWith(Comparator<User> { u1, u2 ->
                var comparison = u1.display_name.compareTo(u2.display_name, true)
                when {
                    comparison > 0 -> 1
                    comparison == 0 -> 0
                    else -> -1
                }
            })

            //return users below
            users

        }.subscribeOn(Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { users ->

                    it.value = users
                    //subscribe
                    wampService?.getSubscriber()?.subscribeChat(users)
                    wampService?.getSubscriber()?.subscribePost(users)
                },
                { error ->  Utils.handleException(context,  error) }
            )

    }


    private fun handleObservable(observable: Observable<MutableList<User>>?, liveData: MutableLiveData<MutableList<User>>){
        disposable = observable?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { result -> onLoadUsers(result, liveData)},
                { error ->  Utils.handleException(context, error)}
            )
    }

    override fun onLocationChange(location: UserLocation) {
        disposable = Observable.fromCallable{
            var foundUser = userList.value?.find { it.user_id == location.user_id }
            foundUser?.location = location
            foundUser
        }?.subscribeOn(Schedulers.computation())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { user ->
                    if (user != null) {
                        updateUser(user)
                    }
                },
                { error ->  Utils.handleException(context, error)}
            )
    }

    override fun onProfileUpdate(user: User) {
        updateUser(user)
    }

    override fun onOnlineStatus(online_status: OnlineStatus) {
        disposable = Observable.fromCallable{
            var foundUser = userList.value?.find { it.user_id == online_status.user_id }
            foundUser?.online = online_status.online
            foundUser?.last_seen = online_status.last_seen

            foundUser
        }?.subscribeOn(Schedulers.computation())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe(
                { user ->
                    if (user != null) {
                        updateUser(user)
                    }
                },
                { error ->  Utils.handleException(context, error)}
            )
    }
}
