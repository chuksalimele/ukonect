package com.ukonect.www.data

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.app.ActivityCompat
import com.ukonect.www.util.Constants
import java.util.*

class Call {

    companion object {

        val UNKNOWN_TYPE = 0;
        val DIALLED_CALL = 1;
        val MISSED_CALL = 2;
        val RECEIVED_CALL = 3;
    }

    var unknown = false//unknown caller
    var call_id: String =
        "" // will help to retrieve the user object of the caller if a Ukonect member

    val call_phone_no get() = if (user.mobile_phone_no.isNotEmpty()
        && user.mobile_phone_no != Constants.STR_NA)
        user.mobile_phone_no
    else
        call_id

    var time: Date? = null
    var duration: Long = 0;// duration in seconds
    var type = UNKNOWN_TYPE; //whether DIALLED, MISSED , RECEIVED
    var is_read = false
    var user = User()

    /*
        Using an implicit intent to make the call
     */
    fun callPhoneNumber(context: Context) {
        val callIntent = Intent(Intent.ACTION_CALL)
        var phone_no = call_phone_no

        if(phone_no.isEmpty()){
            return
        }

        val phoneNumber = "tel:$phone_no"

        callIntent.data = Uri.parse(phoneNumber)

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        context.startActivity(callIntent)
    }

}