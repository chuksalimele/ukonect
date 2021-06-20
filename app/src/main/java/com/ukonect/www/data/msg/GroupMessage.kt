package com.ukonect.www.data.msg

import com.ukonect.www.data.MemberAdded
import com.ukonect.www.data.MemberLeft
import com.ukonect.www.data.MemberRemoved
import com.ukonect.www.data.GroupEdited


class GroupMessage: ChatMessage() {

    var group_name = ""
    var group_edited: GroupEdited? = null;//like in WhatsApp if this is not null a different View will be used to format this message
    var member_added: MemberAdded? = null;//like in WhatsApp if this is not null a different View will be used to format this message
    var member_removed: MemberRemoved? = null;//like in WhatsApp if this is not null a different View will be used to format this message
    var member_left: MemberLeft? = null;//like in WhatsApp if this is not null a different View will be used to format this message

}

