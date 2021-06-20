
package com.ukonect.www.ui.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.databinding.SupportChatReceivedItemBinding
import com.ukonect.www.databinding.SupportChatSentItemBinding

class SupportChatAdapter :
    RecyclerView.Adapter<SupportChatAdapter.SupportChatViewHolder>() {

    private var support_chat_list = listOf<ChatMessage>()
    private var app_user: AppUser = AppUser();

    private val SENT_TYPE = 1
    private val RECEIVED_TYPE = 2

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): SupportChatViewHolder {


        if(i == SENT_TYPE){
            val supportChatSentItemBinding = SupportChatSentItemBinding.inflate(LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return SupportChatViewHolder(supportChatSentItemBinding)
        }
        else{

            val supportChatReceivedItemBinding = SupportChatReceivedItemBinding.inflate(LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return SupportChatViewHolder(supportChatReceivedItemBinding)
        }

    }

    override fun onBindViewHolder(
        supportChatViewHolder: SupportChatViewHolder,
        i: Int
    ) {
        val current_support_chat_msg: ChatMessage = support_chat_list[i]
        var currentUer = current_support_chat_msg.user

        supportChatViewHolder.supportChatSentItemBinding?.chatMsg = current_support_chat_msg
        supportChatViewHolder.supportChatSentItemBinding?.user = currentUer

        supportChatViewHolder.supportChatReceivedItemBinding?.chatMsg = current_support_chat_msg
        supportChatViewHolder.supportChatReceivedItemBinding?.user = currentUer
    }

    override fun getItemViewType(position: Int): Int {

        val currentSupportChatMsg: ChatMessage = support_chat_list[position]

        if(currentSupportChatMsg.user.user_id == app_user.user_id){
            return SENT_TYPE
        }else{
            return RECEIVED_TYPE
        }
    }

    override fun getItemCount(): Int {
        return support_chat_list.size
    }

    fun setSupportChatList(app_user: AppUser, support_chat_list: MutableList<ChatMessage>) {
        this.app_user = app_user
        this.support_chat_list = support_chat_list
        notifyDataSetChanged()
    }

    inner class SupportChatViewHolder: RecyclerView.ViewHolder {
        var supportChatReceivedItemBinding : SupportChatReceivedItemBinding? = null
        var supportChatSentItemBinding : SupportChatSentItemBinding? = null
        constructor(binding: SupportChatSentItemBinding):super(binding.root){
            supportChatSentItemBinding = binding
        }

        constructor(binding: SupportChatReceivedItemBinding):super(binding.root){
            supportChatReceivedItemBinding = binding
        }
    }

}