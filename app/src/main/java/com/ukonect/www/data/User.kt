package com.ukonect.www.data

import com.ukonect.www.util.Constants
import java.util.*

/*
    This is the user model - its fields must match the users table in the remote end
 */
open class User() {

    var user_type = Constants.FREE_USERS
    var phone_number_verified_at = Date()
    var user_id = "" // which the verified phone number during registration
    var password = ""//for now we dont use password but we will reserve it here for future consideration e.g we user connect from web site
    var api_token = ""
    var groups_belong = mutableListOf<String>() //array of names of groups the user belong to
    var country_code = ""//two letters country code of the user verified phone number - e.g  Nigeria = NG, United States = US e.t.c
    var dialling_code = 0
    var country = ""
    var is_contact = false
    val is_registered get() = created_at != 0L
    var photo_url: String = ""
    var photo_base64: String = ""//Note: We will not store  this feild in the mongo document
    var photo_file_extension: String=""
    var first_name = ""
    var last_name = ""
    var home_address: String = Constants.STR_NA
    var office_address: String = Constants.STR_NA
    var location: UserLocation = UserLocation(null, "")
    var contacts: MutableList<String> = mutableListOf<String>() //array of ids of contacts of the user
    var dob: Long = 0L
    var last_seen: Long = 0L
    var mobile_phone_no  = Constants.STR_NA // which the verified phone number during registration
    var work_phone_no = Constants.STR_NA
    var personal_email = Constants.STR_NA
    var work_email = Constants.STR_NA
    var website = Constants.STR_NA
    var status_message = ""
    var online = false
    var updated_at: Long = 0L 
	var created_at: Long = 0L

    val display_name: String get() = "$first_name $last_name"
}