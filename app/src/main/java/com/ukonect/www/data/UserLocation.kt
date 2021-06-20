package com.ukonect.www.data

import android.location.Location
import com.ukonect.www.Ukonect.Companion.appUser
import java.util.*

class UserLocation(location: Location?, locationAddress: String="") {
    var user_id = appUser?.user_id//required so we can use it find the user on this location
    var latitude = location?.latitude;
    var longitude = location?.longitude;
    var altitude = location?.altitude;
    var bearing = location?.bearing;
    var speed = location?.speed;
    var address = locationAddress
    var time = location?.time
}