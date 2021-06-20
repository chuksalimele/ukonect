package com.ukonect.www.remote.update

import com.ukonect.www.data.UserLocation

interface ILocationUpdate {
    fun onLocationChange(location: UserLocation)
}