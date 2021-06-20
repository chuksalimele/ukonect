package com.ukonect.www.dummy

import android.content.Context
import com.ukonect.www.data.*
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.data.msg.PostMessage
import com.ukonect.www.data.msg.SmsMessage
import com.ukonect.www.util.Utils
import java.util.*

class Dummy(val context: Context)
{
    private val dummy_suffix = 0;
    private val NG = "NG"

    fun getDummyAppUser(): AppUser{
        var u = AppUser()
         u = createUser(dummy_suffix, u) as AppUser
         u.created_at = 0L // make it unregistered
        return u
    }

    fun createUser(i: Int): User{
        var obj = User()
        return createUser(i, obj)
    }

    private fun createUser(i: Int, obj :User): User{

        val phoneNo = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG)
        obj.user_id  = phoneNo.numberE164
        obj.created_at = 0L
        obj.display_name = "Person Name_"+i
        obj.home_address = "No. " + i + " along Road_"+i +" off Place_"+i+" Ekpan, Delta"
        obj.office_address = "No. " + i + "Airport Road, Delta"
        obj.photo_url ="photo_url_"+i
        obj.personal_email = "persona_email_"+i+"4u@gmail.com"
        obj.work_email = "work_email_"+i+"4u@beepme.com"
        obj.website = "com.ukonect.www.mywebsite"+i+".com"
        obj.location = UserLocation(null, "last location address")
        //"Warri, Nigeria "+i
        obj.status_message = "This is my status message "+i
        obj.mobile_phone_no =  phoneNo.nationNumber
        obj.work_phone_no =  Utils.reformPhoneNumber(context, "071" + (22200000 + i), NG).nationNumber

        return obj
    }

    fun getTestCallInfoList(count: Int): MutableList<Call> {
       var list = mutableListOf<Call>();

        for (i in dummy_suffix..count - 1 + dummy_suffix){
            var obj = Call()

            obj.time = Date()

            var rand_type = (1..3).random()
            obj.type = rand_type;

            obj.call_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164

            if(obj.type == Call.RECEIVED_CALL) {
                obj.duration = i + 10L;
            }

            if(obj.type == Call.RECEIVED_CALL || obj.type == Call.MISSED_CALL) {
                obj.call_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164
                obj.user = createUser(dummy_suffix + 1, obj.user)
            }else if(obj.type == Call.DIALLED_CALL){
                obj.call_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164
                obj.user = createUser(dummy_suffix, obj.user)
            }

            list.add(obj)//make sure to set caller phone number as the map key
        }

        return list;
    }

    fun getTestUserList(count: Int): MutableList<User> {
        var list = mutableListOf<User>();

        for (i in dummy_suffix..count - 1 + dummy_suffix){

            val obj = createUser(i)

            list.add(obj)
        }

        return list;
    }


    fun getTestChatMessageList(count: Int): MutableList<ChatMessage> {
        var list = mutableListOf<ChatMessage>();

        for (n in dummy_suffix..count - 1 + dummy_suffix){
            var i = n

            var obj = ChatMessage()

            obj.text = this.randomDummyWord("ChatWord")
            obj.received_time = Date().time

            obj.status = (1..4).random()

            if(i% 2 == 0 && obj.status != Message.MSG_STATUS_NOT_SENT){
                i = dummy_suffix // app user

                obj.from_user_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164//app user
                obj.to_user_id = Utils.reformPhoneNumber(context, "090" + (10000000 + i + 1), NG).numberE164// other user

            }else{
                i = (dummy_suffix+1 .. dummy_suffix+5).random() // other user

                obj.from_user_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164// other user
                obj.to_user_id = Utils.reformPhoneNumber(
                    context,
                    "090" + (10000000 + dummy_suffix),
                    NG
                ).numberE164//app user
            }

            obj.user = createUser(i, obj.user)


            if(Message.MSG_STATUS_SEEN ==  obj.status
                || Message.MSG_STATUS_READ ==  obj.status
                || Message.MSG_STATUS_SENT ==  obj.status ) {
                obj.received_time = Date(Date().time - (10 *1000) * n).time
            }

            list.add(obj)
        }

        return list;
    }

    fun getTestSmsMessageList(count: Int): MutableList<SmsMessage> {
        var list = mutableListOf<SmsMessage>();

        for (n in dummy_suffix..count - 1 + dummy_suffix){
            var i = n
            var obj = SmsMessage()

            obj.text = this.randomDummyWord("SmsWord")
            obj.received_time = Date().time

            obj.status = (1..4).random()

            if(i% 2 == 0 && obj.status != Message.MSG_STATUS_NOT_SENT){
                i = dummy_suffix//app user

                obj.from_user_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164//app user
                obj.to_user_id = Utils.reformPhoneNumber(context, "090" + (10000000 + i + 1), NG).numberE164// other user
            }else{
                i = (dummy_suffix+1 .. dummy_suffix+5).random() // other user

                obj.from_user_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164// other user
                obj.to_user_id = Utils.reformPhoneNumber(
                    context,
                    "090" + (10000000 + dummy_suffix),
                    NG
                ).numberE164//app user
            }

            obj.user = createUser(i, obj.user)


            if(Message.MSG_STATUS_SEEN ==  obj.status
                || Message.MSG_STATUS_READ ==  obj.status
                || Message.MSG_STATUS_SENT ==  obj.status ) {
                obj.received_time = Date(Date().time - (10 *1000) * n).time
            }

            list.add(obj)
        }

        return list;
    }

    fun getTestPostList(count: Int): MutableList<PostMessage> {
        var list = mutableListOf<PostMessage>();


        for (n in dummy_suffix..count - 1 + dummy_suffix){
            var i = n

            var obj = PostMessage()
            obj.text = this.randomDummyWord("PostWord")
            obj.received_time = Date().time

            obj.status = (1..4).random()

            if(i% 2 == 0 && obj.status != Message.MSG_STATUS_NOT_SENT){
                i = dummy_suffix//app user

                obj.from_user_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164//app user
                obj.to_user_id = Utils.reformPhoneNumber(context, "090" + (10000000 + i + 1), NG).numberE164// other user
            }else{
                i = (dummy_suffix+1 .. dummy_suffix+5).random() // other user

                obj.from_user_id = Utils.reformPhoneNumber(context, "080" + (10000000 + i), NG).numberE164// other user
                obj.to_user_id = Utils.reformPhoneNumber(
                    context,
                    "090" + (10000000 + dummy_suffix),
                    NG
                ).numberE164//app user
            }

            obj.user = createUser(i, obj.user)

            if(Message.MSG_STATUS_SEEN ==  obj.status
                || Message.MSG_STATUS_SENT ==  obj.status ) {
                obj.received_time = Date(Date().time - (10 *1000) * n).time
            }

            list.add(obj)
        }

        return list;
    }

    private fun randomDummyWord(prefix: String): String{
        var text = ""
        var start = 1;
        var end = (1..10).random();

        for(i in start..end){
            text += prefix + i +" ";
        }

        return text.trim();
    }

}