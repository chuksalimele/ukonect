package com.ukonect.www.ui.binding

import android.app.Dialog
import android.content.Context
import android.view.*
import androidx.appcompat.widget.PopupMenu
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
import com.ukonect.www.databinding.UserSmallCardBinding
import com.ukonect.www.util.Constants
import me.everything.providers.android.telephony.TelephonyProvider

class UserSmallCardListAdapter(
    val fragment: Fragment,
    val title: String,
    val footer: Boolean
) :
    RecyclerView.Adapter<UserSmallCardListAdapter.UserCardListViewHolder>() {

    private var map_data = mutableMapOf<Any, Any>()
    private var app_user: AppUser = AppUser();
    private  var keys = map_data.keys.toList()
    private val HEADER = "HEADER"
    private val FOOTER = "FOOTER"

    private val HEADER_TYPE = 1
    private val FOOTER_TYPE = 2
    private val USER_OBJ_TYPE = 3

    private val USER_PHOTO_ACTION  = 1
    private val POPUP_MENU_ICON_ACTION  = 2

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

            val userCardBinding = UserSmallCardBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false)

            return UserCardListViewHolder(
                userCardBinding
            )
        }else{//footer
            //not implemented yey
        }

        return null!!
    }

    override fun onBindViewHolder(
        userCarListViewListViewHolder: UserCardListViewHolder,
        i: Int
    ) {

        if(isHeader(i)){
            userCarListViewListViewHolder.listSubHeaderBinding?.listSubheaderTitle?.text = title
        }else if(isUserObject(i)){
            val currentUser: User = map_data[i] as User

            var bnd = userCarListViewListViewHolder.userCardBinding;
            bnd?.user = currentUser
            bnd?.appUser = this.app_user

            createInteractions(bnd, currentUser, app_user, {})
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
        userCardBinding: UserSmallCardBinding?,
        currentUser: User?,
        app_user: AppUser?,
        callback: () -> Unit
    ) {

        if(app_user == null || currentUser == null){
            return
        }
        this.app_user = app_user
        userCardBinding?.smallUserPopupMenuIcon?.setOnClickListener(ItemListener(
            POPUP_MENU_ICON_ACTION,
            currentUser,
            callback
        ))
        userCardBinding?.smallUserPhoto?.setOnClickListener(ItemListener(
            USER_PHOTO_ACTION,
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
    ) : View.OnClickListener,PopupMenu.OnMenuItemClickListener {
        var context: Context? = null
        override fun onClick(v: View) {
            context = v.context
            when(action){
                USER_PHOTO_ACTION -> handleUserPhotoClick(v)
                POPUP_MENU_ICON_ACTION -> handlePopupMenuIconClick(v)
            }
            callback()
        }

        private fun handleUserPhotoClick(v: View){

            val d = Dialog(v.context)
            d.requestWindowFeature(Window.FEATURE_NO_TITLE);

            var bnd = UserLargeCardBinding.inflate(LayoutInflater.from(v.context))
            bnd.card.radius = 0f // remove the corner radius if any
            bnd.user = data as User
            bnd.appUser = app_user

            UserLargeCardListAdapter(fragment, "", false).createInteractions(bnd, bnd.user, bnd.appUser) {
                d.hide()
            }

            d.setContentView(bnd.root)

            d.show()
        }

        private fun handlePopupMenuIconClick(v: View){
            val popup = PopupMenu(v.context, v)
            popup.inflate(R.menu.user_small_card_popup_menu)

            popup.setOnMenuItemClickListener(this)
            popup.show()
        }

        override fun onMenuItemClick(item: MenuItem): Boolean {
            return when (item.itemId) {
                R.id.user_small_card_popup_call -> {
                    var user = data as User
                    var call = Call()
                    call.call_id = user.user_id
                    call.user = user
                    context?.let { call.callPhoneNumber(it) }
                    true
                }

                R.id.user_small_card_popup_chat_me_up -> {
                    var other_user_id = (data as User).user_id

                    val bundle = bundleOf(Constants.USER_ID to other_user_id)
                    val c = NavHostFragment.findNavController(fragment)
                    c.navigate(R.id.action_global_ChatMeUpFragment, bundle)

                    true
                }
                R.id.user_small_card_popup_sms -> {
                    var other_user_id = (data as User).user_id
                    var other_user_phone_no = (data as User).mobile_phone_no

                    val bundle = bundleOf(
                        Constants.USER_ID to other_user_id,
                        Constants.PHONE_NO to other_user_phone_no,
                        Constants.SMS_TYPE to TelephonyProvider.Filter.INBOX.ordinal
                    )

                    val c = NavHostFragment.findNavController(fragment)
                    c.navigate(R.id.action_global_SmsViewFragment, bundle)

                    true
                }
                R.id.user_small_card_popup_view_profile -> {
                    val bundle = bundleOf(Constants.USER_ID to (data as User).user_id)
                    val c = NavHostFragment.findNavController(fragment)
                    c.navigate(R.id.action_global_PersonalProfileFragment, bundle)
                    true
                }
                else -> false
            }
        }
    }


    inner class UserCardListViewHolder :
        RecyclerView.ViewHolder {
        var userCardBinding: UserSmallCardBinding? = null
        var listSubHeaderBinding : ListSubHeaderBinding? = null
        constructor(binding: UserSmallCardBinding):super(binding.root){
            userCardBinding = binding
        }

        constructor(binding: ListSubHeaderBinding):super(binding.root){
            listSubHeaderBinding = binding
        }

    }


}

