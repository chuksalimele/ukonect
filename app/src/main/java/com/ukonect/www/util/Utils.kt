package com.ukonect.www.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import android.telephony.TelephonyManager
import android.util.Base64
import android.util.Base64OutputStream
import android.util.Patterns
import androidx.appcompat.app.AlertDialog
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.snatik.storage.Storage
import com.ukonect.www.BuildConfig
import com.ukonect.www.R
import com.ukonect.www.data.Message
import com.ukonect.www.exception.UkonectException
import com.ukonect.www.phone.PhoneNo
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber.PhoneNumber
import java.io.*
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Type
import java.util.*
import java.util.Base64.getEncoder
import kotlin.math.*


class Utils {

    companion object {

        private val NORMAL_TIME = 0
        private val LOCATION_TIME = 1

        @JvmStatic
        fun getBase64StringFromFile(f: File): String {
            return FileInputStream(f).use { inputStream ->
                ByteArrayOutputStream().use { outputStream ->
                    Base64OutputStream(outputStream, Base64.DEFAULT).use { base64FilterStream ->
                        inputStream.copyTo(base64FilterStream)
                        base64FilterStream.close() // This line is required
                        outputStream.toString()
                    }
                }
            }
        }

        /**
         * To use do this
         *   val jsonArray: String = ...
         *   val user: MutableList<User> = jsonArrayToList(jsonArray, User::class.java);
         */
        @JvmStatic
        fun <T> jsonArrayToList(
            jsonArray: String?,
            clazz: Class<T>
        ): MutableList<T> {
            val typeOfT: Type =
                TypeToken.getParameterized(MutableList::class.java, clazz)
                    .type
            return Gson().fromJson(jsonArray, typeOfT)
        }

        @JvmStatic
        fun unique(): String {
            return UUID.randomUUID().toString().replace("-", "").toUpperCase()
        }
        /*
          Get the distance between in meters two points (latitude/longitude) in km using Haversine formula
         */
        @JvmStatic
        fun distanceOnEarthInMeters(
            lat1: Double,
            lat2: Double,
            lon1: Double,
            lon2: Double
        ): Double {
            return distanceOnEarthInKilometers(lat1, lat2, lon1, lon2) / 1000
        }
        /*
          Get the distance in kilometers between two points (latitude/longitude) in km using Haversine formula
         */
        @JvmStatic
        fun distanceOnEarthInKilometers(
            lat1: Double,
            lat2: Double,
            lon1: Double,
            lon2: Double
        ): Double {

            // The math module contains a function
            // named toRadians which converts from
            // degrees to radians.
            var lat1 = lat1
            var lat2 = lat2
            var lon1 = lon1
            var lon2 = lon2
            lon1 = Math.toRadians(lon1)
            lon2 = Math.toRadians(lon2)
            lat1 = Math.toRadians(lat1)
            lat2 = Math.toRadians(lat2)

            // Haversine formula
            val dlon = lon2 - lon1
            val dlat = lat2 - lat1
            val a = (sin(dlat / 2).pow(2.0)
                    + (cos(lat1) * Math.cos(lat2)
                    * sin(dlon / 2).pow(2.0)))
            val c = 2 * asin(sqrt(a))

            // Radius of earth in kilometers. Use 3956
            // for miles
            val r = 6371.0

            // calculate the result
            return c * r
        }

		@JvmStatic
		fun formatLocationAddress(context: Context, location: Location): String{

            val geocoder = Geocoder(context, Locale.getDefault())
            var address = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            if(address.isEmpty()) return ""

            return "${address[0].locality}, ${address[0].countryName}"
		}

        @JvmStatic
        fun isValidEmail(email: String):Boolean{
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        @JvmStatic
        fun reformPhoneNumber(
            context: Context,
            number: String,
            two_letters_country_code: String = ""
        ): PhoneNo {

            var phoneNo = PhoneNo()
            if(number.isEmpty()) return phoneNo

            var phone_no = PhoneNumberUtil.normalizeDiallableCharsOnly(number)

            val phoneUtil = PhoneNumberUtil.createInstance(context)
            var phoneNumber = PhoneNumber()

            try {
                phoneNumber = phoneUtil.parse(phone_no, two_letters_country_code)

            } catch (ex: NumberParseException) {

                //try to make the number valid after parse failure
                phone_no = PhoneNumberUtil.normalizeDigitsOnly(number) //important
                phoneNumber.countryCode = countryNumericCode(context)//using default user device locale
                phoneNumber.nationalNumber = if(phone_no.isNotEmpty()) phone_no.toLong() else 0

                if (!phoneUtil.isValidNumber(phoneNumber)) { //we cannot make the number valid
                    phoneNo.countryNumericCode = phoneNumber.countryCode
                    phoneNo.nationNumber = "$phoneNumber.nationalNumber"
                    phoneNo.numberE164 = phoneNo.nationNumber
                    return phoneNo // return as it is - there is nothing else we can do about it
                }
            }

            //At this point the number is valid

            phoneNo.countryNumericCode = phoneNumber.countryCode
            phoneNo.nationNumber = phoneUtil.getNationalSignificantNumber(phoneNumber)
            phoneNo.numberE164 = phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164)

            return phoneNo
        }

        /**
         * Gets the numeric country code e.g 234 for Nigeria
         * Returns -1 if not found or it fails
         */
        @JvmStatic
        fun countryNumericCode(context: Context, two_letters_country_code: String? = null): Int{

            var country_code = two_letters_country_code

            if(country_code == null || country_code.isEmpty()){
                country_code = countryNameCode(context)
            }

            var fail = -1;

            try {

                var code = context.resources.getStringArray(R.array.DialingCountryCode)
                    .find { it.endsWith(country_code) }
                    ?.split(",")
                    ?.get(0)
                    ?.toInt()
                    ?: fail

                return code

            }catch(ex: NumberFormatException){
                return fail
            }

        }

        /**
         * Gets the two letters country code e.g NG for Nigeria
         * Returns empty string if not found or it fails
         */
        @JvmStatic
        fun countryNameCode(context: Context): String {
            var countryCode: String?

            // try to get country code from TelephonyManager service
            val tm =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            if (tm != null) {
                // query first getSimCountryIso()
                countryCode = tm.simCountryIso
                if (countryCode != null && countryCode.length == 2)
                    return countryCode.toUpperCase()

                countryCode = if (tm.phoneType == TelephonyManager.PHONE_TYPE_CDMA) {
                    // special case for CDMA Devices
                    getCDMACountryIso()
                } else {
                    // for 3G devices (with SIM) query getNetworkCountryIso()
                    tm.networkCountryIso
                }
                if (countryCode != null && countryCode.length == 2)
                    return countryCode.toUpperCase()
            }

            // if network country not available (tablets maybe), get country code from Locale class
            countryCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                context.resources.configuration.locales.get(0).country
            } else {
                context.resources.configuration.locale.country
            }
            return if (countryCode != null && countryCode.length == 2) countryCode.toUpperCase() else ""
        }

        @SuppressLint("PrivateApi")
        @JvmStatic
        private fun getCDMACountryIso(): String? {
            try {
                // try to get country code from SystemProperties private class
                val systemProperties =
                    Class.forName("android.os.SystemProperties")
                val get: Method = systemProperties.getMethod("get", String::class.java)

                // get homeOperator that contain MCC + MNC
                val homeOperator = get.invoke(
                    systemProperties,
                    "ro.cdma.home.operator.numeric"
                ) as String

                // first 3 chars (MCC) from homeOperator represents the country code
                val mcc = homeOperator.substring(0, 3).toInt()
                when (mcc) {
                    330 -> return "PR"
                    310 -> return "US"
                    311 -> return "US"
                    312 -> return "US"
                    316 -> return "US"
                    283 -> return "AM"
                    460 -> return "CN"
                    455 -> return "MO"
                    414 -> return "MM"
                    619 -> return "SL"
                    450 -> return "KR"
                    634 -> return "SD"
                    434 -> return "UZ"
                    232 -> return "AT"
                    204 -> return "NL"
                    262 -> return "DE"
                    247 -> return "LV"
                    255 -> return "UA"
                }
            } catch (ignored: ClassNotFoundException) {
            } catch (ignored: NoSuchMethodException) {
            } catch (ignored: IllegalAccessException) {
            } catch (ignored: InvocationTargetException) {
            } catch (ignored: NullPointerException) {
            }
            return null
        }
        /**
        format to  time part only if today and from yesterday
        format as follow
        yesterday -> yesterday, 12:23
        this week -> fri, 12:00
        beyond that ->21 days 12:12
         */
        @JvmStatic
        fun formatTime(date: Date?): String {
            return formaTime0(date, NORMAL_TIME)
        }
        /**
        format to  time part only if today and from yesterday
        format as follow
        yesterday -> yesterday, 12:23
        this week -> fri, 12:00
        beyond that ->21 days 12:12
         */
        @JvmStatic
        fun formatTime(date_long: Long): String {
            return formaTime0(Date(date_long), NORMAL_TIME)
        }

        /**
        format to  time part only if today and from yesterday
        format as follow
        yesterday -> 12:23 yesterday
        this week -> 12:00 on Friday
        beyond that ->21 days 12:12
         */
        @JvmStatic
        fun formatLocationTime(date: Date?): String {
            return formaTime0(date, LOCATION_TIME)
        }


        @JvmStatic
        private fun formaTime0(date: Date?, time_type: Int): String {

            if (date == null) {
                return ""
            }

            var dateStr = ""
            var now = Date()
            var todayBegin = Date(now.year, now.month, now.date, 0, 0)

            val sec = 1000.0
            val min = sec * 60.0
            val hr = min * 60.0
            var day = hr * 24.0

            val calendar: Calendar = GregorianCalendar()
            calendar.time = date

            var hour = calendar[Calendar.HOUR_OF_DAY]
            var mins = calendar[Calendar.MINUTE]

            if (date.time >= todayBegin.time) {//today
                when (time_type) {
                    NORMAL_TIME -> dateStr = "Today, ${toTwoDigit(hour)}:${toTwoDigit(mins)}"
                    LOCATION_TIME -> dateStr = "${toTwoDigit(hour)}:${toTwoDigit(mins)} today"
                }
            } else if (date.time >= todayBegin.time - day) {//yesterday
                when (time_type) {
                    NORMAL_TIME -> dateStr = "Yesterday, ${toTwoDigit(hour)}:${toTwoDigit(mins)}"
                    LOCATION_TIME -> dateStr = "${toTwoDigit(hour)}:${toTwoDigit(mins)} yesterday"
                }

            } else if (date.time >= todayBegin.time - 7 * day) {//at least 7 days ago

                var day_of_week = calendar[Calendar.DAY_OF_WEEK]
                var day_name = ""
                when (time_type) {
                    NORMAL_TIME -> {
                        when (day_of_week) {
                            0 -> day_name = "Sun"
                            1 -> day_name = "Mon"
                            2 -> day_name = "Tue"
                            3 -> day_name = "Wed"
                            4 -> day_name = "Thu"
                            5 -> day_name = "Fri"
                            6 -> day_name = "Sat"
                        }

                        dateStr = "$day_name, ${toTwoDigit(hour)}:${toTwoDigit(mins)}"
                    }
                    LOCATION_TIME -> {
                        when (day_of_week) {
                            0 -> day_name = "Sunday"
                            1 -> day_name = "Monday"
                            2 -> day_name = "Tuesday"
                            3 -> day_name = "Wednesday"
                            4 -> day_name = "Thursday"
                            5 -> day_name = "Friday"
                            6 -> day_name = "Saturday"
                        }

                        dateStr = "${toTwoDigit(hour)}:${toTwoDigit(mins)} on $day_name"
                    }
                }

            } else {//greater than 7 days

                val year = calendar[Calendar.YEAR]
                var month = calendar[Calendar.MONTH]
                val day = calendar[Calendar.DAY_OF_MONTH]

                var month_name = ""
                when (month) {
                    0 -> month_name = "Jan"
                    1 -> month_name = "Feb"
                    2 -> month_name = "Mar"
                    3 -> month_name = "Apr"
                    4 -> month_name = "May"
                    5 -> month_name = "Jun"
                    6 -> month_name = "Jul"
                    7 -> month_name = "Aug"
                    8 -> month_name = "Sep"
                    9 -> month_name = "Oct"
                    10 -> month_name = "Nov"
                    11 -> month_name = "Dec"
                }

                dateStr = "$month_name $day, $year"
            }

            return dateStr
        }

        @JvmStatic
        private fun toTwoDigit(num: Int): String {
            return if (num < 10) "0" + num else "" + num
        }

        @JvmStatic
        fun contentExcerpt(content: String): String {
            return contentExcerpt(content, 50)
        }

        @JvmStatic
        fun contentExcerpt(content: String, max_len: Int): String {
            var len = max_len;
            if (len < 0) {
                len = 0;
            }

            if (len > content.length) {
                len = content.length;
            }

            return content.substring(0..len - 1) + "..."//come back abeg o!!!
        }

        @JvmStatic
        fun msgStatusTick(status: Int): String {
            var tick = ""
            var symbol = "\u2713"

            when (status) {
                Message.MSG_STATUS_NOT_SENT -> {
                    tick = "⏰"
                }
                Message.MSG_STATUS_SENT -> {
                    tick = symbol
                }
                Message.MSG_STATUS_SEEN -> {
                    tick = symbol + symbol
                }
                Message.MSG_STATUS_READ -> {
                    tick = symbol + symbol
                }
            }

            return tick
        }


        @JvmStatic
        fun logExternal(context: Context?, ex: Throwable) {
            var storage  = Storage(context)
            ex.stackTrace.forEach {
                writeExternal0(storage, it.toString())
            }
        }

        @JvmStatic
        fun logExternal(context: Context?, ex: Exception) {
            var storage  = Storage(context)
            ex.stackTrace.forEach {
                writeExternal0(storage, it.toString())
            }
        }

        @JvmStatic
        fun logExternal(context: Context?, data: String) {
            var str = data + "\n"
            var storage  = Storage(context)
            writeExternal0(storage, str)
        }

        @JvmStatic
        private fun writeExternal0(storage: Storage, data: String) {

            var dir = storage.externalStorageDirectory+ File.separator+ BuildConfig.APPLICATION_ID + File.separator+"log"
            var file = dir+File.separator+"log.txt"

            if(!storage.isDirectoryExists(dir)){
                storage.createDirectory(dir)
            }

            if(!storage.isFileExist(file)){
                storage.createFile(file, data)
            }else {
                storage.appendFile(file, data)
            }
        }

        fun handleException(context: Context?, ex: Throwable, errorFn: ((err: String)-> Unit)? = null) {
                if(ex !is UkonectException){
                    if(errorFn != null){
                        errorFn(ex.message?:"")
                    }else{
                        logExternal(context, ex)
                    }

                    return//leave
                }

                //At this point it is instance of UkonectException

                if(errorFn != null){
                    errorFn(ex.errMsg)
                }else if(ex.isRemoteError()){
                    if (ex?.errMsg?.isNotEmpty() == true) {
                        if (context != null) {
                            AlertDialog.Builder(context)
                                .setMessage(ex?.errMsg)
                                .create()
                                .show()
                        }
                    }
                }else{//not remote error
                    logExternal(context, ex)
                }

                if(ex.debugMsg.isNotEmpty()){
                    logExternal(context, ex.debugMsg)//TODO - IN PRODUCTION USE ACCRA TO SEND THE ERROR REPORT
                }
                if(ex.stackTraceMsg.isNotEmpty()){
                    logExternal(context, ex.stackTraceMsg)//TODO - IN PRODUCTION USE ACCRA TO SEND THE ERROR REPORT
                }

        }

        fun getLocationProximityInt(context: Context, position: Int): Int {
            if(position == 0){
                return 0//"None" option
            }
            var proximity: String? =
                context?.resources?.getStringArray(R.array.search_location_proximities)?.get(position)
                    ?: null
            proximity = proximity?.replace(",","")
            var locationProximitySearchOptInMeters = 0

            if(proximity?.endsWith("km") == true){
                proximity = proximity.substring(0, proximity.length -2)
                locationProximitySearchOptInMeters = proximity.toInt() * 1000 // convert to meters
            }else if(proximity?.endsWith("m") == true){
                proximity = proximity.substring(0, proximity.length -1)
                locationProximitySearchOptInMeters = proximity.toInt()
            }
            return locationProximitySearchOptInMeters
        }


    }


}