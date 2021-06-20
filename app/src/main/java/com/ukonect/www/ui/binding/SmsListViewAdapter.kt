package com.ukonect.www.ui.binding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.R
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Message
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.SmsMessage
import com.ukonect.www.databinding.ListSubHeaderBinding
import com.ukonect.www.databinding.SmsListItemBinding
import com.ukonect.www.ui.sms.SmsListViewFragmentDirections
import com.ukonect.www.util.Constants
import me.everything.providers.android.telephony.TelephonyProvider

class SmsListViewAdapter(navCtrlr: NavController)  :
    RecyclerView.Adapter<SmsListViewAdapter.SmsListViewViewHolder>(){
    private val navController = navCtrlr
    private var sms_map = mutableMapOf<Any, Any> ()
    private  var keys = sms_map.keys.toList()
    private var all_sms_list = listOf<SmsMessage>()
    private var app_user: AppUser = AppUser();

    private val HEADER = "HEADER"
    private val FOOTER = "FOOTER"

    private val FOOTER_TYPE = 1
    private val ITEM_TYPE = 2

    private fun isFooter(i:Int): Boolean{
        return keys[i] is String &&  keys[i] == FOOTER
    }

    private fun isSmsMessageObject(i:Int): Boolean{
        return keys[i] is Int && sms_map[keys[i]] is SmsMessage
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): SmsListViewViewHolder {

        if(i == FOOTER_TYPE){
            var space = Space(viewGroup.context)
            space.layoutParams = LinearLayout.LayoutParams(
                //width
                LinearLayout.LayoutParams.MATCH_PARENT,
                //height
                viewGroup.context.resources.getDimensionPixelOffset(R.dimen.list_item_large_height)
            )

            return SmsListViewViewHolder(space)

        }else {
            val smsListItemBinding = SmsListItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return SmsListViewViewHolder(
                smsListItemBinding
            )
        }
    }

    override fun onBindViewHolder(
        smsListViewViewHolder: SmsListViewViewHolder,
        i: Int
    ) {
        if(getItemViewType(i) == FOOTER_TYPE){
            //TODO - may added click event to footer
        }else {
            val currentSms: SmsMessage = sms_map[i] as SmsMessage
            val currentUser: User = currentSms.user

            smsListViewViewHolder.smsListItemBinding?.sms = currentSms
            smsListViewViewHolder.smsListItemBinding?.user = currentUser

            smsListViewViewHolder.smsListItemBinding?.root?.setOnClickListener(SmsItemListener(currentSms))
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(isFooter(position)){
            return FOOTER_TYPE
        }else{
            return ITEM_TYPE
        }
    }

    override fun getItemCount(): Int {
        return sms_map.size
    }

    private fun setCountRead(msg: SmsMessage){
        var app_user_id = this.app_user.user_id
        var other_user_id = if (msg.to_user_id != app_user_id) msg.to_user_id else msg.from_user_id

        val count = all_sms_list.count {
            it.status != Message.MSG_STATUS_READ
                    && msg.type == TelephonyProvider.Filter.INBOX.ordinal
        }

        msg.read_count = count
    }

    fun otherUserId(msg: SmsMessage): String{
        return if (msg.type == TelephonyProvider.Filter.INBOX.ordinal) msg.from_user_id else msg.to_user_id
    }

    fun filterLastMessagePerUserAndSetUnread(sms_list_view_list: MutableList<SmsMessage>): MutableList<SmsMessage>{
        all_sms_list = sms_list_view_list
        var grp_list = mutableListOf<SmsMessage>()
        var app_user_id = this.app_user.user_id


        for(msg in sms_list_view_list){
            //get the other user id according to our definition - see PhoneSms class
            var other_user_id = otherUserId(msg)
            //get the last record of each user messages
            val last_index = sms_list_view_list.indexOfLast { it.to_user_id == other_user_id || it.from_user_id == other_user_id }

            if(last_index == -1){
                continue
            }

            val lmsg = sms_list_view_list[last_index]

            //check if we have already added it
            val has = grp_list.any { it.to_user_id == other_user_id || it.from_user_id == other_user_id }
            if(!has){
                setCountRead(lmsg)
                grp_list.add(lmsg)
            }
        }

        return grp_list
    }

    fun setSmsListViewList(app_user: AppUser, sms_list_view_list: MutableList<SmsMessage>) {
        this.app_user = app_user

        val last_msg_per_user_list = filterLastMessagePerUserAndSetUnread(sms_list_view_list)


        //sort in desceding order
        last_msg_per_user_list.sortWith(Comparator<SmsMessage> { a, b ->
            val time_a = a.time
            val time_b = b.time
            when {
                time_a < time_b -> 1
                time_a == time_b -> 0
                else -> -1
            }

        })
		
        var index = 0
        last_msg_per_user_list.forEach{
            sms_map[index]=it
            index++
        }

        sms_map[FOOTER] = ""
        keys = sms_map.keys.toList()

        notifyDataSetChanged()
    }

    inner class SmsListViewViewHolder :
        RecyclerView.ViewHolder{
        var smsListItemBinding: SmsListItemBinding? = null
        var listSubHeaderBinding: ListSubHeaderBinding? = null

        constructor(binding: SmsListItemBinding):super(binding.root){
            smsListItemBinding = binding
        }

        constructor(binding: ListSubHeaderBinding):super(binding.root){
            listSubHeaderBinding = binding
        }

        constructor(space: Space):super(space)
    }


    inner class SmsItemListener(val sms: SmsMessage) : View.OnClickListener {

        override fun onClick(v: View?) {

            val bundle = bundleOf(
                Constants.USER_ID to otherUserId(sms),
                Constants.PHONE_NO to sms.sms_phone_no,
                Constants.SMS_TYPE to sms.type
            )

            var direction = SmsListViewFragmentDirections.moveToNavGraphSmsView()
            navController.navigate(direction.actionId, bundle)
        }

    }
}

