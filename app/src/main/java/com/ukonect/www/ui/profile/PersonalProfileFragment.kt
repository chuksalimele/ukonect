package com.ukonect.www.ui.profile

import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Bundle
import androidx.preference.PreferenceManager
import android.view.*
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.Call
import com.ukonect.www.data.User
import com.ukonect.www.databinding.PersonalProfileFragmentBinding
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants
import com.google.android.material.appbar.AppBarLayout
import com.google.gson.Gson
import com.ukonect.www.Ukonect
import com.ukonect.www.Ukonect.Companion.appUser
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.everything.providers.android.telephony.TelephonyProvider
import java.util.*

class PersonalProfileFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private var disposable: Disposable? = null

    private val model: PersonalProfileViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private val authModel: MainViewModel by activityViewModels()
    private val usersModel: UserListModel by activityViewModels()
    private var _binding: PersonalProfileFragmentBinding? = null
    private val locationProxities by lazy{
        context?.resources?.getStringArray(R.array.search_location_proximities)
    }

    private val DEFAUT_PROXIMITY_INDEX = 3
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() =
            PersonalProfileFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = PersonalProfileFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).wampService.setLocationUpdater(usersModel)

        val layout = binding?.personalProfileCollapsingToolbarLayout
        val toolbar = binding?.personalProfileToolbar
        val appBarConfiguration = AppBarConfiguration(navController.graph)
        if (toolbar != null) {
            layout?.setupWithNavController(toolbar, navController, appBarConfiguration)
        }

        var mainActivity = this.activity as MainActivity
        mainActivity.setSupportActionBar(toolbar)

        var user_id = arguments?.getString(Constants.USER_ID)

        if(user_id != null) {//other user
            createObserversAndGetData(user_id)
        }else{//app user
            updateUI(authModel.app_user);
        }

        var display_name = authModel.app_user?.display_name

        var space = " "
        (activity as MainActivity).supportActionBar?.setTitle(space)

        binding?.personalProfileAppBar?.addOnOffsetChangedListener(AppBarLayout.OnOffsetChangedListener { appBarLayout, verticalOffset -> //  Vertical offset == 0 indicates appBar is fully  expanded.

            //If the AppBarLayout’s ‘verticalOffset’ is zero, then its fully expanded

            if (Math.abs(verticalOffset) > 200) {
                //appBarExpanded = false
                //invalidateOptionsMenu()
                (activity as MainActivity).supportActionBar?.setTitle(display_name)

            } else {
                //appBarExpanded = true
                //invalidateOptionsMenu()
                (activity as MainActivity).supportActionBar?.setTitle(space)
            }

        })

    }

    private fun createObserversAndGetData(user_id: String?){

        var user = usersModel.getUser(user_id);
        //Update the UI
        if(user!= null) {
            updateUI(user)
        }

        // Create the observer which updates the UI.
        val observer = Observer<MutableList<User>> { users ->
            var user = usersModel.getUser(user_id);
            //Update the UI
            if(user!= null) {
                updateUI(user)
            }
        }

        // Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        usersModel.getList().observe(viewLifecycleOwner, observer)

    }

    private fun updateUI(user: User?){
        binding?.user  = user;
        binding?.appUser = authModel.app_user

        binding?.personalProfileScrollingContent?.user = user
        binding?.personalProfileScrollingContent?.appUser = authModel.app_user

        binding?.personalProfileScrollingContent?.personalProfilePhoneNumberInclude?.user = user
        binding?.personalProfileScrollingContent?.personalProfilePhoneNumberInclude?.appUser = authModel.app_user

        binding?.personalProfileScrollingContent?.personalProfileEmailAddressInclude?.user = user
        binding?.personalProfileScrollingContent?.personalProfileEmailAddressInclude?.appUser = authModel.app_user

        binding?.personalProfileScrollingContent?.personalProfileAddressInclude?.user = user
        binding?.personalProfileScrollingContent?.personalProfileAddressInclude?.appUser = authModel.app_user


        //hide the geofencing notification component if the user profile is of the app user himself
        if(user?.user_id == authModel.app_user?.user_id){
            binding?.personalProfileScrollingContent?.personalProfileGeoFencingInclude?.root?.visibility = View.GONE
        }

         setLocationBoundaryInfo(user)

        binding
            ?.personalProfileScrollingContent
            ?.personalProfilePhoneNumberInclude
            ?.userPhoneNumberPersonalBtn
            ?.setOnClickListener {
                var call = Call()
                call.call_id = binding
                    ?.personalProfileScrollingContent
                    ?.personalProfilePhoneNumberInclude
                    ?.userPhoneNumberPersonalLbl?.text.toString()

                if (user != null) {
                    call.user = user
                }
                call.callPhoneNumber(it.context)
            }

        binding
            ?.personalProfileScrollingContent
            ?.personalProfilePhoneNumberInclude
            ?.userPhoneNumberCallWorkBtn
            ?.setOnClickListener {
                var call = Call()
                call.call_id = binding
                    ?.personalProfileScrollingContent
                    ?.personalProfilePhoneNumberInclude
                    ?.userPhoneNumberWorkLbl?.text.toString()

                if (user != null) {
                    call.user = user
                }
                call.callPhoneNumber(it.context)
            }

        binding
            ?.personalProfileScrollingContent
            ?.personalProfilePhoneNumberInclude
            ?.userPhoneNumberSmsMobileBtn
            ?.setOnClickListener {

                var phone_no = binding
                    ?.personalProfileScrollingContent
                    ?.personalProfilePhoneNumberInclude
                    ?.userPhoneNumberPersonalLbl?.text.toString()

                val bundle = bundleOf(
                    Constants.USER_ID to user?.user_id,
                    Constants.PHONE_NO to phone_no,
                    Constants.SMS_TYPE to TelephonyProvider.Filter.INBOX.ordinal
                )

                val c = NavHostFragment.findNavController(this)
                c.navigate(R.id.action_global_SmsViewFragment, bundle)

            }

        binding
            ?.personalProfileScrollingContent
            ?.personalProfilePhoneNumberInclude
            ?.userPhoneNumberSmsWorkBtn
            ?.setOnClickListener {

                var phone_no = binding
                    ?.personalProfileScrollingContent
                    ?.personalProfilePhoneNumberInclude
                    ?.userPhoneNumberWorkLbl?.text.toString()

                val bundle = bundleOf(
                    Constants.USER_ID to user?.user_id,
                    Constants.PHONE_NO to phone_no,
                    Constants.SMS_TYPE to TelephonyProvider.Filter.INBOX.ordinal
                )

                val c = NavHostFragment.findNavController(this)
                c.navigate(R.id.action_global_SmsViewFragment, bundle)

            }

        binding
            ?.personalProfileScrollingContent
            ?.personalProfileEmailAddressInclude
            ?.userWebsiteBtn
            ?.setOnClickListener {
                //TODO open the default system browse or the user will choose app to open it
            }

        binding
            ?.personalProfileScrollingContent
            ?.personalProfileEmailAddressInclude
            ?.userEmailAddressPersonalBtn //if gmail or yahoo then open the app send email component
            ?.setOnClickListener {
                //TODO if gmail is installed go to its email composer - same with yahoo mail
            }



        var picked = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(Constants.KEY_PROFILE_LOCATION_PROXIMITY_INDEX, DEFAUT_PROXIMITY_INDEX);

        displaySelectedProximity(picked);

        binding
            ?.personalProfileScrollingContent
            ?.personalProfileGeoFencingInclude
            ?.geoFencingLocationProximityBtn
            ?.setOnClickListener {
                AlertDialog.Builder(requireContext())
                    .setTitle("Notify me when ${user?.display_name} in and out of range")
                    .setSingleChoiceItems(locationProxities, picked) { dialog, which->
                        picked = which
                    }
                    .setPositiveButton(getString(R.string.ok)) { dialog, which ->

                        displaySelectedProximity(picked);

                        PreferenceManager.getDefaultSharedPreferences(context)
                            .edit()
                            .putInt(Constants.KEY_PROFILE_LOCATION_PROXIMITY_INDEX, picked)
                            .apply()

                        setLocationBoundaryInfo(user)

                        user?.let {
                            var subscriber =
                                (activity as MainActivity).wampService.getSubscriber()

                            if (picked > 0) {//subscribe to change in location of this user
                                subscriber?.subscribeLocation(it)
                            } else {//"None" option is selected
                                subscriber?.unsubscribeLocation(it)//remove the location user and unsubscribe
                            }

                            //save the location users
                            disposable = Observable.fromCallable {
                                Gson().toJson(subscriber?.locationUsers)
                            }?.subscribeOn(Schedulers.computation())
                                ?.observeOn(AndroidSchedulers.mainThread())
                                ?.subscribe(
                                    { jsonLocationUser ->
                                        PreferenceManager.getDefaultSharedPreferences(context)
                                            .edit()
                                            .putString(
                                                Constants.KEY_PROFILE_LOCATION_PROXIMITY_USERS,
                                                jsonLocationUser
                                            )
                                            .apply()
                                    },
                                    { error -> Utils.handleException(context, error) }
                                )
                        }

                    }
                    .create()
                    .show()
            }


    }

    private fun setLocationBoundaryInfo(user: User?){
        var indexPicked = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(Constants.KEY_PROFILE_LOCATION_PROXIMITY_INDEX, DEFAUT_PROXIMITY_INDEX);
        var range = Utils.getLocationProximityInt(requireContext(), indexPicked)

        var app_user_lat = appUser?.location?.latitude?:0.0
        var other_user_lat = user?.location?.latitude?:0.0
        var app_user_lng = appUser?.location?.longitude?:0.0
        var other_user_lng = user?.location?.longitude?:0.0

        var display = "Unknown"
        if(app_user_lat !=0.0
            && other_user_lat !=0.0
            && app_user_lng !=0.0
            && other_user_lng !=0.0){
            var distanceInMetres = Utils.distanceOnEarthInMeters(app_user_lat, other_user_lat, app_user_lng, other_user_lng)
            if(range>0){

                var other_user_location_time = user?.location?.time?:0

                if(distanceInMetres > range){
                    display = "Out of range ${Utils.formatLocationTime(Date(other_user_location_time))}"
                }else{
                    display = "Within range ${Utils.formatLocationTime(Date(other_user_location_time))}"
                }
            }

        }


        binding
            ?.personalProfileScrollingContent
            ?.personalProfileGeoFencingInclude
            ?.geoFencingInAndOutRangeWithTimeLabel?.text = display
    }

    private fun displaySelectedProximity(index: Int){

        binding
            ?.personalProfileScrollingContent
            ?.personalProfileGeoFencingInclude
            ?.geoFencingSelectedPoriximityLabel?.text = locationProxities?.get(index)

    }

    override fun onStart() {
        super.onStart()
        PreferenceManager.getDefaultSharedPreferences(this.context)
            .registerOnSharedPreferenceChangeListener(this)
    }

    override fun onStop() {
        PreferenceManager.getDefaultSharedPreferences(this.context)
            .unregisterOnSharedPreferenceChangeListener(this)
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.edit_profile_fragment -> {
					
				var direction = PersonalProfileFragmentDirections.toEditProfileFragment()
				navController.navigate(direction.actionId)
                true
            }
            R.id.personal_profile_settings_fragment -> {
					
				var direction = PersonalProfileFragmentDirections.toPersonalProfileSettingsFragment()
				navController.navigate(direction.actionId)
                
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.personal_profile_app_bar, menu)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {

            when(key){
                Constants.KEY_APP_USER_LAST_LOCATION -> {
                    if(Ukonect.isAppUserAuthenticated) {
                        if(binding?.user?.user_id == authModel.app_user?.user_id) {
                            binding?.personalProfileScrollingContent?.personalProfileLocationLbl?.text = appUser?.location?.address
                        }
                    }
                }
            }
    }

}
