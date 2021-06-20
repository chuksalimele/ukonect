package com.ukonect.www.remote.update

import com.ukonect.www.data.*
import com.ukonect.www.data.msg.GroupMessage

interface IGroupUpdate {

    fun onGroupEdited(pair: Pair<Group, GroupEdited>)
    fun onGroupMemberAdded(member_added: MemberAdded)
    fun onGroupMemberRemoved(member_removed: MemberRemoved)
    fun onGroupMemberLeft(member_left: MemberLeft)
    fun onGroupChatMessage(message: GroupMessage)
    fun onGroupChatModified(message: GroupMessage)
    fun onGroupChatDeleted(message: GroupMessage)
    fun onGroupChatSent(message_id: String)
    fun onGroupChatSeen(message_id: String)
    fun onGroupChatRead(message_id: String)
}