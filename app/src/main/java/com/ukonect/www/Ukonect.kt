package com.ukonect.www

import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import android.os.Looper
import androidx.multidex.MultiDexApplication
import androidx.preference.PreferenceManager
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.User
import com.ukonect.www.data.UserLocation
import com.ukonect.www.exception.UkonectUncaughtExceptionHandler
import com.ukonect.www.remote.WampService
import com.ukonect.www.util.Constants
import com.ukonect.www.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class Ukonect : MultiDexApplication() , SharedPreferences.OnSharedPreferenceChangeListener{

    var gson =  Gson()
    private lateinit var locationRequest: LocationRequest
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    companion object {
        var appUser: AppUser? = null;
        var isAppUserAuthenticated = false
        var context: Context? = null
        /**
         * The desired interval for location updates. Inexact. Updates may be more or less frequent.
         */
        private const val UPDATE_INTERVAL: Long = 60000 // Every 60 seconds.

        /**
         * The fastest rate for active location updates. Updates will never be more frequent
         * than this value, but they may be less frequent.
         */
        private const val FASTEST_UPDATE_INTERVAL: Long = 30000 // Every 30 seconds

        /**
         * The max time before batched results are delivered by location services. Results may be
         * delivered sooner than this interval.
         */
        private const val MAX_WAIT_TIME = UPDATE_INTERVAL * 5 // Every 5 minutes.
    }

    val wampService by lazy {
        WampService()
    }

    override fun onCreate() {
        super.onCreate()

        Thread.setDefaultUncaughtExceptionHandler(UkonectUncaughtExceptionHandler(this.applicationContext))

        Ukonect.context = this.applicationContext

        PreferenceManager.getDefaultSharedPreferences(this)
            .registerOnSharedPreferenceChangeListener(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        createLocationRequest()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                setLocationUpdatesResult(locationResult.locations)
            }
        }


        wampInitialize()
    }

    fun startLocationRequest(){
        requestLastKnownLocation()
        startLocationUpdates()
    }

    private fun requestLastKnownLocation(){
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                location?.let{
                    setLocationUpdatesResult(listOf<Location>(location))
                }
            }
    }

     private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.getMainLooper())
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    private fun wampInitialize(){
        //setup users who locations are being monitored
        var jsonLocationUsers = PreferenceManager.getDefaultSharedPreferences(this)
            .getString(Constants.KEY_PROFILE_LOCATION_PROXIMITY_USERS, null)

        jsonLocationUsers?.let{
            wampService.getSubscriber()?.locationUsers = Utils.jsonArrayToList(it, User::class.java)
        }

        wampService.start()
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * `ACCESS_COARSE_LOCATION` and `ACCESS_FINE_LOCATION`. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     *
     *
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     *
     *
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private fun createLocationRequest() {
        locationRequest = LocationRequest()

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        // Note: apps running on "O" devices (regardless of targetSdkVersion) may receive updates
        // less frequently than this interval when the app is no longer in the foreground.
        locationRequest.interval = UPDATE_INTERVAL

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        locationRequest.fastestInterval = FASTEST_UPDATE_INTERVAL
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        // Sets the maximum time when batched location updates are delivered. Updates may be
        // delivered sooner than this interval.
        locationRequest.maxWaitTime = MAX_WAIT_TIME
    }

    fun setLocationUpdatesResult(locations: List<Location>) {

        if(locations.isEmpty()) return

        val last_location = locations.last();

        val lastLocation :UserLocation = UserLocation(last_location, Utils.formatLocationAddress(this, last_location))

        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putString(Constants.KEY_APP_USER_LAST_LOCATION, gson.toJson(lastLocation))
            .apply()


    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?) {

        when(key){
            Constants.KEY_APP_USER_LAST_LOCATION -> {
                if(isAppUserAuthenticated) {
                    var locationStr: String? = PreferenceManager.getDefaultSharedPreferences(this)
                        .getString(Constants.KEY_APP_USER_LAST_LOCATION, null)
                        ?: return
                    val location = Gson().fromJson(locationStr, UserLocation::class.java)
                    appUser?.location = location;

                    //Send the user location to the server
                    wampService.getPublisher()?.locationChange(appUser)
                        ?.subscribeOn(Schedulers.io())
                        ?.observeOn(AndroidSchedulers.mainThread())
                        ?.subscribe ({result->

                        } ,{ error ->  Utils.handleException(this, error)})


                }
            }
        }
    }

}