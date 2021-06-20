package com.ukonect.www.data.msg

import com.ukonect.www.data.*;

open class ChatMessage : PostMessage() {

    var group_join_request : GroupJoinRequest? = null;//like in WhatsApp if this is not null a different View will be used to format this message

}