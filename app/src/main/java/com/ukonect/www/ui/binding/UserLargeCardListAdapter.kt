package com.ukonect.www.ui.binding

import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.R
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Call
import com.ukonect.www.data.User
import com.ukonect.www.databinding.ListSubHeaderBinding
import com.ukonect.www.databinding.UserLargeCardBinding
import com.ukonect.www.util.Constants
import me.everything.providers.android.telephony.TelephonyProvider


class UserLargeCardListAdapter(
    val fragment: Fragment,
    val title: String,
    val footer: Boolean
) :
    RecyclerView.Adapter<UserLargeCardListAdapter.UserCardListViewHolder>() {

    private var map_data = mutableMapOf<Any, Any>()
    private var app_user: AppUser = AppUser();
    private  var keys = map_data.keys.toList()
    private val HEADER = "HEADER"
    private val FOOTER = "FOOTER"

    private val HEADER_TYPE = 1
    private val FOOTER_TYPE = 2
    private val USER_OBJ_TYPE = 3


    val SMS_ICON_ACTION = 1
    val CALL_ICON_ACTION = 2
    val PROFILE_ICON_ACTION = 3
    val PHOTO_ACTION = 4
    val CHAT_ME_UP_ICON_ACTION = 5

    private fun isHeader(i:Int): Boolean{
        return keys[i] is String &&  keys[i] == HEADER
    }

    private fun isFooter(i:Int): Boolean{
        return keys[i] is String &&  keys[i] == FOOTER
    }

    private fun isUserObject(i:Int): Boolean{
        return keys[i] is Int && map_data[keys[i]] is User
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): UserCardListViewHolder {

        if(i == HEADER_TYPE){
            val listSubHeaderBinding = ListSubHeaderBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false)

            return UserCardListViewHolder(
                listSubHeaderBinding
            )
        }else if(i == USER_OBJ_TYPE){

            val userCardBinding = UserLargeCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false)

            return UserCardListViewHolder(
                userCardBinding
            )
        }else{//footer
            //not implemented yey
        }

        return  null!!
    }

    override fun onBindViewHolder(
        callListViewListViewHolder: UserCardListViewHolder,
        i: Int
    ) {
        if(isHeader(i)){
            callListViewListViewHolder.listSubHeaderBinding?.listSubheaderTitle?.text = title
        }else if(isUserObject(i)) {
            val currentUser: User = map_data[i] as User
            val bnd = callListViewListViewHolder.userLargeCardBinding

            bnd?.user = currentUser
            bnd?.appUser = this.app_user

            createInteractions(
                bnd,
                currentUser,
                app_user,
                {}
            )
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(isHeader(position)){
            return HEADER_TYPE
        }else if(isFooter(position)){
            return FOOTER_TYPE
        } else {
            return USER_OBJ_TYPE
        }
    }

    fun createInteractions(
        userCardBinding: UserLargeCardBinding?,
        currentUser: User?,
        app_user: AppUser?,
        callback: () -> Unit
    ) {

        if(app_user == null || currentUser == null){
            return
        }

        this.app_user = app_user
        userCardBinding?.userPhoto?.setOnClickListener(ItemListener(
            PHOTO_ACTION,
            currentUser,
            callback
        ))
        userCardBinding?.userMakeCall?.setOnClickListener(ItemListener(
            CALL_ICON_ACTION,
            currentUser,
            callback
        ))
        userCardBinding?.userInfo?.setOnClickListener(ItemListener(
            PROFILE_ICON_ACTION,
            currentUser,
            callback
        ))
        userCardBinding?.userSendSms?.setOnClickListener(ItemListener(
            SMS_ICON_ACTION,
            currentUser,
            callback
        ))
        userCardBinding?.userChatMeUp?.setOnClickListener(ItemListener(
            CHAT_ME_UP_ICON_ACTION,
            currentUser,
            callback
        ))

    }

    override fun getItemCount(): Int {
        return map_data.size
    }

    fun setUserCardList(app_user: AppUser, user_list: MutableList<User>) {
        this.app_user = app_user
        this.map_data.clear()

        if(title.isNotEmpty()) {
            this.map_data[HEADER] = title
        }

        var count = 0

        user_list.forEach{
            this.map_data[count] = it
            count++
        }

        if(footer){
            this.map_data[FOOTER] = ""
        }

        keys = map_data.keys.toList()

        notifyDataSetChanged()
    }


    inner class ItemListener(
        val action: Int,
        val data: Any,
        val callback: () -> Unit
    ) : View.OnClickListener, CompoundButton.OnCheckedChangeListener {

        override fun onClick(v: View) {

            when(action){
                SMS_ICON_ACTION -> handleSmsIconClick(v)
                CALL_ICON_ACTION -> handleCallIconClick(v)
                CHAT_ME_UP_ICON_ACTION -> handleChatMeUpIconClick(v)
                PROFILE_ICON_ACTION -> handleProfileIconClick(v)
                PHOTO_ACTION -> handlePhotoClick(v)
            }

            callback()
        }

        override fun onCheckedChanged(cbtn: CompoundButton?, b: Boolean) {

            /*when(action){
                FAVOURITE_ACTION -> handleFavouriteIconCheckChange(cbtn, b)
            }*/

        }

        private fun handleSmsIconClick(v: View){

            var other_user_id = (data as User).user_id
            var other_user_phone_no = (data as User).mobile_phone_no

            val bundle = bundleOf(
                Constants.USER_ID to other_user_id,
                Constants.PHONE_NO to other_user_phone_no,
                Constants.SMS_TYPE to TelephonyProvider.Filter.INBOX.ordinal
            )

            val c = NavHostFragment.findNavController(fragment)
            c.navigate(R.id.action_global_SmsViewFragment, bundle)
        }

        private fun handleChatMeUpIconClick(v: View){

            var other_user_id = (data as User).user_id

            val bundle = bundleOf(Constants.USER_ID to other_user_id)
            val c = NavHostFragment.findNavController(fragment)
            c.navigate(R.id.action_global_ChatMeUpFragment, bundle)
        }


        private fun handleCallIconClick(v: View){
            var user = data as User
            var call = Call()
            call.call_id = user.user_id
            call.user = user
            call.callPhoneNumber(v.context)
        }

        private fun handleProfileIconClick(v: View){
            val bundle = bundleOf(Constants.USER_ID to (data as User).user_id)
            val c = NavHostFragment.findNavController(fragment)
            c.navigate(R.id.action_global_PersonalProfileFragment, bundle)
        }

        private fun handlePhotoClick(v: View){
            val dialog =
                Dialog(v.context, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            dialog.setContentView(R.layout.profile_photo)
            dialog.show()
        }

        private fun handleFavouriteIconCheckChange(cbtn: CompoundButton?, b: Boolean){
            app_user.modifyFavourite(cbtn as Switch, (data as User).user_id)
        }

    }


    inner class UserCardListViewHolder :
        RecyclerView.ViewHolder {
        var userLargeCardBinding: UserLargeCardBinding? = null
        var listSubHeaderBinding : ListSubHeaderBinding? = null
        constructor(binding: UserLargeCardBinding):super(binding.root){
            userLargeCardBinding = binding
        }

        constructor(binding: ListSubHeaderBinding):super(binding.root){
            listSubHeaderBinding = binding
        }

    }

}