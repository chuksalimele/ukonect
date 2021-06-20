package com.ukonect.www.phone

import android.content.Context
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Call
import com.ukonect.www.data.User
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.util.Utils
import me.everything.providers.android.calllog.Call as EverythingCall
import me.everything.providers.android.calllog.Call.CallType
import me.everything.providers.android.calllog.CallsProvider
import java.util.*

class PhoneCall (private val context: Context, private val app_user : AppUser, private val usersModel: UserListModel) {


    fun getCalls(type: CallType): MutableList<Call>{

        val callsProvider = CallsProvider(context)
        var callList = callsProvider.calls.list.filter { it.type ==  type};

        var beepmeCallList = mutableListOf<Call>()
        callList.forEach {
            beepmeCallList.add(toCall(it, type))
        }

        return beepmeCallList
    }

    private fun toCall(evcall: EverythingCall, type: CallType): Call {

        var call = Call()
        call.time = Date(evcall.callDate)
        call.duration = evcall.duration

        var caller_number = evcall.number

        if(caller_number.isEmpty()){
            caller_number = "Unknown"
            call.unknown = true
        }

        call.call_id = caller_number
        call.is_read = evcall.isRead

        val phoneNo = Utils.reformPhoneNumber(context, caller_number)
        val numberE164 = phoneNo.numberE164
        var unknownUser = User()
        unknownUser.user_id = numberE164
        unknownUser.display_name = caller_number

        call.user = if(!call.unknown) //caller is known - number not hidden
            usersModel.getUser(numberE164)?: unknownUser
        else//unknown number - number hidden
            unknownUser

        when(evcall.type){
            CallType.OUTGOING -> call.type = Call.DIALLED_CALL
            CallType.INCOMING -> call.type = Call.RECEIVED_CALL
            CallType.MISSED -> call.type = Call.MISSED_CALL
        }


        return call
    }

}