package com.ukonect.www.ui.binding


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.databinding.ChatMeUpReceivedItemBinding
import com.ukonect.www.databinding.ChatMeUpSentItemBinding

class ChatMeUpAdapter :
    RecyclerView.Adapter<ChatMeUpAdapter.ChatMeUpViewHolder>() {

    private var chat_list = listOf<ChatMessage>()
    private var app_user: AppUser = AppUser();

    private val SENT_TYPE = 1
    private val RECEIVED_TYPE = 2

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ChatMeUpViewHolder {


        if(i == SENT_TYPE){
            val chatMeUpSentItemBinding = ChatMeUpSentItemBinding.inflate(LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return ChatMeUpViewHolder(chatMeUpSentItemBinding)
        }
        else{

            val chatMeUpReceivedItemBinding = ChatMeUpReceivedItemBinding.inflate(LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return ChatMeUpViewHolder(chatMeUpReceivedItemBinding)
        }

    }

    override fun onBindViewHolder(
        chatMeUpViewHolder: ChatMeUpViewHolder,
        i: Int
    ) {
        val current_chat_msg: ChatMessage = chat_list[i]
        var currentUser = current_chat_msg.user

        chatMeUpViewHolder.chatMeUpSentItemBinding?.chatMsg = current_chat_msg
        chatMeUpViewHolder.chatMeUpSentItemBinding?.user = currentUser

        chatMeUpViewHolder.chatMeUpReceivedItemBinding?.chatMsg = current_chat_msg
        chatMeUpViewHolder.chatMeUpReceivedItemBinding?.user = currentUser
    }

    override fun getItemViewType(position: Int): Int {

        val currentChatMeUpMsg: ChatMessage = chat_list[position]

        if(currentChatMeUpMsg.user.user_id == app_user.user_id){
            return SENT_TYPE
        }else{
            return RECEIVED_TYPE
        }
    }

    override fun getItemCount(): Int {
        return chat_list.size
    }

    fun setChatMeUpList(app_user: AppUser, other_user_id: String? , chat_list: MutableList<ChatMessage>) {
        this.app_user = app_user

        this.chat_list = chat_list.filter {
            it.from_user_id ==  other_user_id || it.to_user_id == other_user_id
        }

        this.chat_list = chat_list
        notifyDataSetChanged()
    }

    inner class ChatMeUpViewHolder: RecyclerView.ViewHolder {
        var chatMeUpReceivedItemBinding : ChatMeUpReceivedItemBinding? = null
        var chatMeUpSentItemBinding : ChatMeUpSentItemBinding? = null
        constructor(binding: ChatMeUpSentItemBinding):super(binding.root){
            chatMeUpSentItemBinding = binding
        }

        constructor(binding: ChatMeUpReceivedItemBinding):super(binding.root){
            chatMeUpReceivedItemBinding = binding
        }
    }

}