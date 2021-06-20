package com.ukonect.www.phone

import android.content.Context
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Message
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.SmsMessage
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.util.Utils
import me.everything.providers.android.telephony.Sms
import me.everything.providers.android.telephony.TelephonyProvider
import java.util.*

class PhoneSms(private val context: Context, private val app_user : AppUser, private val usersModel: UserListModel) {


    fun getMessages(filter: TelephonyProvider.Filter): MutableList<SmsMessage>{

        val telephonyProvider: TelephonyProvider = TelephonyProvider(context)
        var smsList = telephonyProvider.getSms(filter).list
        var smsMessageList = mutableListOf<SmsMessage>()
        smsList.forEach {
            smsMessageList.add(toSmsMessage(it, filter))
        }

        return smsMessageList
    }

    private fun toSmsMessage(sms: Sms, filter: TelephonyProvider.Filter): SmsMessage {
        var msg = SmsMessage()

        var phoneNo = Utils.reformPhoneNumber(context, sms.address)
        val numberE164 = phoneNo.numberE164

        msg.type = filter.ordinal
        msg.subject = sms.subject?:""
        msg.text = sms.body
        msg.sent_time = sms.sentDate
        msg.received_time = sms.receivedDate

        var unknownUser = User()
        unknownUser.user_id = numberE164
        unknownUser.display_name = sms.address

        if(filter == TelephonyProvider.Filter.INBOX){

            msg.time = if(sms.receivedDate != 0L) sms.receivedDate else sms.sentDate
            msg.sms_phone_no = sms.address // other user
            msg.from_user_id = numberE164 // other user

            //get the user from the server or phone contact
            msg.user = usersModel.getUser(numberE164)?: unknownUser //other user

        }else{//SENT, OUTBOX, DRAFT message type

            msg.time = if(sms.sentDate != 0L) sms.sentDate else sms.receivedDate
            msg.sms_phone_no = sms.address // other user
            msg.to_user_id = numberE164 // other user

            //get the user from the server or phone contact
            msg.user = usersModel.getUser(numberE164)?: unknownUser //other user
        }

        if(sms.seen) {
            msg.status = Message.MSG_STATUS_SEEN
        }

        if(sms.read) {
            msg.status = Message.MSG_STATUS_READ
        }

        return msg
    }

}