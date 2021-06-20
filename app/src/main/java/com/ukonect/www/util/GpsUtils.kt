package com.ukonect.www.util
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.IntentSender.SendIntentException
import android.location.LocationManager
import android.util.Log
import android.widget.Toast
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*


class GpsUtils(private val context: Context) {
    private val mSettingsClient: SettingsClient
    private val mLocationSettingsRequest: LocationSettingsRequest
    private val locationManager: LocationManager
    private val locationRequest: LocationRequest

    init {
        locationManager =
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        mSettingsClient = LocationServices.getSettingsClient(context)
        locationRequest = LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 10 * 1000L
        locationRequest.fastestInterval = 2 * 1000L
        val builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        //**************************
        builder.setAlwaysShow(true) //this is the key ingredient
        //**************************
        mLocationSettingsRequest = builder.build()
    }

    // method for turn on GPS
    fun turnGPSOn(onGpsListener: onGpsListener?) {
        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            Utils.logExternal(context, "turnGPSOn locationManager.isProviderEnabled")//TO BE REMOVED

            onGpsListener?.gpsStatus(true)
        } else {

            Utils.logExternal(context, "turnGPSOn else block")//TO BE REMOVED

            mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(
                    (context as Activity)
                ) { //  GPS is already enable, callback GPS status through listener

                    Utils.logExternal(context, "turnGPSOn else block - GPS is already enable")//TO BE REMOVED

                    onGpsListener?.gpsStatus(true)
                }
                .addOnFailureListener(
                    context
                ) { e ->
                    val statusCode = (e as ApiException).statusCode
                    when (statusCode) {
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {

                            Utils.logExternal(context, "turnGPSOn else block - LocationSettingsStatusCodes.RESOLUTION_REQUIRED")//TO BE REMOVED

                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            val rae = e as ResolvableApiException
                            rae.startResolutionForResult(
                                context,
                                Constants.GPS_REQUEST
                            )
                        } catch (sie: SendIntentException) {

                            Utils.logExternal(context, "turnGPSOn else block - PendingIntent unable to execute request")//TO BE REMOVED

                            Log.i(
                                ContentValues.TAG,
                                "PendingIntent unable to execute request."
                            )
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {

                            Utils.logExternal(context, "turnGPSOn else block - LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE")//TO BE REMOVED

                            val errorMessage =
                                "Location settings are inadequate, and cannot be " +
                                        "fixed here. Fix in Settings."
                            Log.e(ContentValues.TAG, errorMessage)
                            Toast.makeText(
                                context,
                                errorMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
        }
    }

    interface onGpsListener {
        fun gpsStatus(isGPSEnable: Boolean)
    }

}