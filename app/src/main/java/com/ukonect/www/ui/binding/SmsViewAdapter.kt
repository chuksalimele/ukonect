
package com.ukonect.www.ui.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.msg.SmsMessage
import com.ukonect.www.databinding.SmsReceivedItemBinding
import com.ukonect.www.databinding.SmsSentItemBinding

class SmsViewAdapter :
    RecyclerView.Adapter<SmsViewAdapter.SmsViewViewHolder>() {

    private var is_all_type = false
    private var other_user_id = "";
    private var sms_view_list = listOf<SmsMessage>()
    private var app_user: AppUser = AppUser();

    private val SENT_TYPE = 1
    private val RECEIVED_TYPE = 2

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): SmsViewViewHolder {

        var match_parent = LinearLayout.LayoutParams.MATCH_PARENT
        if(i == SENT_TYPE){
            val smsSentItemBinding = SmsSentItemBinding.inflate(LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            if(!this.is_all_type) {//FOR INBOX, OUTBOX, SENT AND DRAFT ONLY
                smsSentItemBinding?.smsSentMsg?.layoutParams?.width = match_parent
            }

            return SmsViewViewHolder(smsSentItemBinding)
        }else{

            val smsReceivedItemBinding = SmsReceivedItemBinding.inflate(LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            if(!this.is_all_type) {//FOR INBOX, OUTBOX, SENT AND DRAFT ONLY
                smsReceivedItemBinding?.smsReceivedMsg?.layoutParams?.width = match_parent
            }

            return SmsViewViewHolder(smsReceivedItemBinding)
        }

    }

    override fun onBindViewHolder(
        smsViewViewHolder: SmsViewViewHolder,
        i: Int
    ) {
        val current_sms_msg: SmsMessage = sms_view_list[i]
        var currentUser = current_sms_msg.user

        smsViewViewHolder.smsSentItemBinding?.sms = current_sms_msg
        smsViewViewHolder.smsSentItemBinding?.user = currentUser

        smsViewViewHolder.smsReceivedItemBinding?.sms = current_sms_msg
        smsViewViewHolder.smsReceivedItemBinding?.user = currentUser

    }

    override fun getItemViewType(position: Int): Int {
        val currentSmsMsg: SmsMessage = sms_view_list[position]
        if(currentSmsMsg.to_user_id == other_user_id){
            return SENT_TYPE
        }else{
            return RECEIVED_TYPE
        }
    }

    override fun getItemCount(): Int {
           return sms_view_list.size
    }

    fun setSmsViewList(
        app_user: AppUser,
        other_user_id: String,
        sms_view_list: MutableList<SmsMessage>,
        is_all_type: Boolean
    ) {

        this.is_all_type = is_all_type
        this.app_user = app_user
        this.other_user_id = other_user_id

        var sms_view_list_filtered = sms_view_list.filter {
            it.from_user_id ==  other_user_id || it.to_user_id == other_user_id
        }

		//sort in asceding order
        this.sms_view_list = sms_view_list_filtered.sortedWith(Comparator<SmsMessage> { a, b ->
            val time_a = a.time
            val time_b = b.time
            when {
                time_a > time_b -> 1
                time_a == time_b -> 0
                else -> -1
            }

        })
		

        notifyDataSetChanged()
    }

    inner class SmsViewViewHolder: RecyclerView.ViewHolder {
        var smsReceivedItemBinding : SmsReceivedItemBinding? = null
        var smsSentItemBinding : SmsSentItemBinding? = null
        constructor(binding: SmsSentItemBinding):super(binding.root){
            smsSentItemBinding = binding
        }

        constructor(binding: SmsReceivedItemBinding):super(binding.root){
            smsReceivedItemBinding = binding
        }
    }

}