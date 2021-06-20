package com.ukonect.www.data

import com.ukonect.www.data.msg.GroupMessage
import java.util.*

class Group {
    var name = ""
    var status_text = ""
    var photo_url = ""
    var photo_base64 = ""//Note: We will not store  this feild in the mongo document
    var photo_file_extension =""
    var created_by = ""//user id of user who created the group
    var admin_ids = mutableListOf<String>()//must be user ids of admins
    var member_ids = mutableListOf<String>()//must be user ids of members
    var updated_at: Long = 0L
	var created_at: Long = 0L 
}