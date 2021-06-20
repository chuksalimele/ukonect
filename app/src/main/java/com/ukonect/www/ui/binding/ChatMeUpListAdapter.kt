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
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.databinding.ListSubHeaderBinding
import com.ukonect.www.databinding.ChatMeUpListItemBinding
import com.ukonect.www.ui.chat.PrivateChatListFragmentDirections
import com.ukonect.www.util.Constants

class ChatMeUpListAdapter(navCtrlr: NavController)  :
    RecyclerView.Adapter<ChatMeUpListAdapter.ChatMeUpListViewViewHolder>(){
    private val navController = navCtrlr
    private var chat_map = mutableMapOf<Any, Any> ()
    private  var keys = chat_map.keys.toList()
    private var all_chat_list = listOf<ChatMessage>()
    private var app_user: AppUser = AppUser();

    private val HEADER = "HEADER"
    private val FOOTER = "FOOTER"

    private val FOOTER_TYPE = 1
    private val ITEM_TYPE = 2

    private fun isFooter(i:Int): Boolean{
        return keys[i] is String &&  keys[i].equals(FOOTER)
    }

    private fun isChatMessageObject(i:Int): Boolean{
        return keys[i] is Int && chat_map[keys[i]] is ChatMessage
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): ChatMeUpListViewViewHolder {

        if(i == FOOTER_TYPE){
            var space = Space(viewGroup.context)
            space.layoutParams = LinearLayout.LayoutParams(
                //width
                LinearLayout.LayoutParams.MATCH_PARENT,
                //height
                viewGroup.context.resources.getDimensionPixelOffset(R.dimen.list_item_large_height)
            )

            return ChatMeUpListViewViewHolder(space)

        }else {
            val chatListItemBinding = ChatMeUpListItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return ChatMeUpListViewViewHolder(
                chatListItemBinding
            )
        }
    }

    override fun onBindViewHolder(
        chatListViewViewHolder: ChatMeUpListViewViewHolder,
        i: Int
    ) {

        if(getItemViewType(i) == FOOTER_TYPE){
            //TODO - may added click event to footer
        }else {
            val currentChat: ChatMessage = chat_map[i] as ChatMessage
            val currentUser: User = currentChat.user

            chatListViewViewHolder.chatListItemBinding?.chatMsg = currentChat
            chatListViewViewHolder.chatListItemBinding?.user = currentUser

            chatListViewViewHolder.chatListItemBinding?.root?.setOnClickListener(ChatItemListener(currentChat))

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
        return chat_map.size
    }

    private fun setCountRead(msg: ChatMessage){
        var app_user_id = this.app_user.user_id
        var other_user_id = if (msg.to_user_id != app_user_id) msg.to_user_id else msg.from_user_id

        val count = all_chat_list.count {
            it.status != Message.MSG_STATUS_READ
                && (it.to_user_id == other_user_id || it.from_user_id == other_user_id)
        }

        msg.read_count = count
    }

    fun filterLastMessagePerUserAndSetUnread(chat_list: MutableList<ChatMessage>): MutableList<ChatMessage>{

        var grp_list = mutableListOf<ChatMessage>()
        var app_user_id = this.app_user.user_id

        for(msg in chat_list){
            var other_user_id = if (msg.to_user_id != app_user_id) msg.to_user_id else msg.from_user_id
            //get the last record of each user messages
            val last_index = chat_list.indexOfLast { it.to_user_id == other_user_id || it.from_user_id == other_user_id }

            if(last_index == -1){
                continue
            }

            val lmsg = chat_list[last_index]

            //check if we have already added it
            val has = grp_list.any { it.to_user_id == other_user_id || it.from_user_id == other_user_id }
            if(!has){
                setCountRead(lmsg)
                grp_list.add(lmsg)
            }
        }


        return grp_list
    }

    fun setChatMeUpList(app_user: AppUser, chat_list_view_list: MutableList<ChatMessage>) {
        this.app_user = app_user
        this.all_chat_list = chat_list_view_list

        val last_msg_per_user_list = filterLastMessagePerUserAndSetUnread(chat_list_view_list)

        var index = 0
        last_msg_per_user_list.forEach{
            chat_map[index]=it
            index++
        }

        chat_map[FOOTER] = ""
        keys = chat_map.keys.toList()

        notifyDataSetChanged()
    }

    inner class ChatMeUpListViewViewHolder :
        RecyclerView.ViewHolder{
        var chatListItemBinding: ChatMeUpListItemBinding? = null
        var listSubHeaderBinding: ListSubHeaderBinding? = null

        constructor(binding: ChatMeUpListItemBinding):super(binding.root){
            chatListItemBinding = binding
        }

        constructor(binding: ListSubHeaderBinding):super(binding.root){
            listSubHeaderBinding = binding
        }

        constructor(space: Space):super(space)
    }


    inner class ChatItemListener(val chat: ChatMessage) : View.OnClickListener {

        override fun onClick(v: View?) {
            val other_user_id = if (chat.from_user_id == app_user.user_id)
                chat.to_user_id
            else
                chat.from_user_id

            val bundle = bundleOf(Constants.USER_ID to other_user_id)
            var direction = PrivateChatListFragmentDirections.moveToNavGraphChatMeUp()
            navController.navigate(direction.actionId, bundle)
        }

    }
}

