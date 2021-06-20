package com.ukonect.www

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.ukonect.www.exception.UkonectUncaughtExceptionHandler
import com.ukonect.www.remote.WampService
import com.ukonect.www.ui.chat.PrivateChatViewModel
import com.ukonect.www.ui.group.GroupViewModel
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.ui.post.PostViewModel
import com.ukonect.www.util.Utils
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class MainActivity : AppCompatActivity() {

    var gson = Gson()

    lateinit var wampService : WampService

    var isGPSEnable = false

    private val authModel: MainViewModel by viewModels()
    private val usersModel: UserListModel by viewModels()
    private val chatsModel: PrivateChatViewModel by viewModels()
    private val groupModel: GroupViewModel by viewModels()
    private val postModel: PostViewModel by viewModels()


    /**
     * Provides access to the Fused Location Provider API.
     */
    private lateinit var appBarConfiguration: AppBarConfiguration
    var navView: NavigationView? = null
    var drawerLayout: DrawerLayout? = null
    var toolbar: Toolbar? = null;
    var currentDestinationID: Int = 0

     val CHANNEL_ID = "channel_02"//come back abeg o!!!

    var disposable: Disposable? = null

    companion object{
        private const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

     override fun onCreate(savedInstanceState: Bundle?) {

         super.onCreate(savedInstanceState)

         wampService = (application as Ukonect).wampService

         setContentView(R.layout.main_activity)
         handleIntent(intent)

         wampService.setLocationUpdater(usersModel)
         wampService.setUserProfileUpdater(usersModel)
         wampService.setUserOnlineStatusUpdater(usersModel)
         wampService.setChatUpdater(chatsModel)
         wampService.setGroupUpdater(groupModel)
         wampService.setPostUpdater(postModel)

         val navController = findNavController(R.id.nav_host_fragment)

         navController.addOnDestinationChangedListener(getOnDestinationChangedListener())

         toolbar = findViewById(R.id.main_app_bar_toolbar)
         setSupportActionBar(toolbar)

         drawerLayout = findViewById(R.id.home_drawer_layout)
         navView = findViewById(R.id.home_nav_view)

         // Passing each menu ID as a set of Ids because each
         // menu should be considered as top level destinations.
         appBarConfiguration = AppBarConfiguration(setOf(R.id.HomeFragment), drawerLayout)
         setupActionBarWithNavController(navController, appBarConfiguration)
         navView?.setupWithNavController(navController)

         //create notification channel required only on Android 8.0 and higher
         createNotificationChannel()

         // Check if the user revoked runtime permissions.
         if (!checkLocationPermissions()) {
             requestLocationPermissions()
         }else{
             (application as Ukonect).startLocationRequest()
         }

     }

    private fun createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.ukonect_notificaton_channel)
            val descriptionText = getString(R.string.ukonect_notification_channel_desc)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constants.GPS_REQUEST) {
                isGPSEnable = true // flag maintain before get location
            }
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private fun checkLocationPermissions(): Boolean {
        val permissionState = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        return permissionState == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        val shouldProvideRationale =
            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {

        } else {

            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_PERMISSIONS_REQUEST_CODE
            )
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {

        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.size <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.

            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission was granted.
                (application as Ukonect).startLocationRequest()
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.

                Utils.logExternal(this,"Permission denied! Request code: $REQUEST_PERMISSIONS_REQUEST_CODE");
            }
        }
    }

    private fun getOnDestinationChangedListener(): NavController.OnDestinationChangedListener {
        return NavController.OnDestinationChangedListener{ controller, destination, arguments  ->

            currentDestinationID = destination.id
            when(destination.id) {
                R.id.HomeFragment -> {
                    drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
                }
                else -> {
                    drawerLayout?.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
                }

            }

            //whether to hide or show action bar
            determineWhetherToShowActionBar()

        }
    }

    /**
     * Deprecated
     */
    fun notifyLastSeen(){

        //send last seen to the backend
        var res = wampService.getCaller()?.updateLastSeen()
            ?.subscribeOn(Schedulers.io())
            ?.observeOn(AndroidSchedulers.mainThread())
            ?.subscribe({

            }, {

            })
    }

    fun determineWhetherToShowActionBar(){

        when(currentDestinationID){
            R.id.signUpConfirmVerificationCodeFragment,
            R.id.signUpFullNameFragment,
            R.id.signUpPhoneNumberVerificationFragment,
            R.id.signUpProfilePhotoFragment,
            R.id.signUpWelcomeFragment,
            R.id.PersonalProfileFragment,
            R.id.EditProfileFragment,
            R.id.ChatMeUpFragment->{
                toolbar?.visibility =  View.GONE
            }
            else ->{
                toolbar?.visibility =  View.VISIBLE
                setSupportActionBar(toolbar)
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        //whether to hide or show action bar
        determineWhetherToShowActionBar()
    }

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
        disposable?.dispose()
    }

    override fun onPause() {
        super.onPause()
        disposable?.dispose()
    }

    override fun onResume() {
        super.onResume()
        determineWhetherToShowActionBar()
    }

    override fun onRestart() {
        super.onRestart()
        determineWhetherToShowActionBar()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)


        Toast.makeText(//TODO - TO BE REMOVED
            this,
            "handleIntent(intent)",
            Toast.LENGTH_LONG
        ).show()

    }

    private fun handleIntent(intent: Intent) {

        if (Intent.ACTION_SEARCH == intent.action) {
            val query = intent.getStringExtra(SearchManager.QUERY)
            //use the query to search your data somehow
        }
    }

    override fun onSearchRequested(): Boolean {

        Toast.makeText(//TODO - TO BE REMOVED
            this,
            "onSearchRequested()",
            Toast.LENGTH_LONG
        ).show()

        val appData = Bundle().apply {
            putBoolean("MY_DATA", true)
        }
        startSearch(null, false, appData, false)
        return true
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.home_app_bar, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return item.onNavDestinationSelected(navController) || super.onOptionsItemSelected(item)
    }

}
