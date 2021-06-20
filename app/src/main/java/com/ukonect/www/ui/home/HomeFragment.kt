package com.ukonect.www.ui.home

import android.Manifest
import android.app.AlertDialog
import android.app.SearchManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.TelephonyManager
import android.view.*
import android.widget.*
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.Ukonect
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.AuthState
import com.ukonect.www.data.ListItemData
import com.ukonect.www.data.User
import com.ukonect.www.databinding.HomeFragmentBinding
import com.ukonect.www.ui.ViewAnimation.rotateFab
import com.ukonect.www.ui.ViewAnimation.showIn
import com.ukonect.www.ui.ViewAnimation.showOut
import com.ukonect.www.ui.binding.HomeSearchListAdapter
import com.ukonect.www.ui.binding.UserSmallCardListAdapter
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.util.Constants
import com.ukonect.www.util.GpsUtils
import com.ukonect.www.util.GpsUtils.onGpsListener
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import me.everything.providers.android.contacts.Contact
import me.everything.providers.android.contacts.ContactsProvider
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*


class HomeFragment : Fragment() {
    private var categorySearchOpt: String? = ""
    private var locationProximitySearchOptInMeters: Int = 0
    private var isRotate = false
    private var contacts: MutableList<Contact>? = null
    private var popupItemData: Any? = null
    private val model: UserListModel by activityViewModels()
    private val authModel: MainViewModel by activityViewModels()
    var searchOffset = 0 //modify accordingly
    private var userCardListAdapter: UserSmallCardListAdapter? =null

    private val navController by lazy { findNavController() }

    private var _binding: HomeFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding // TODO - Somentimes throw KotlinNullPointerException

    companion object {
        fun newInstance() = HomeFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        categorySearchOpt = PreferenceManager.getDefaultSharedPreferences(context)
                .getString(Constants.KEY_REMEBER_SEARCH_CATEGORY_OPTION, "")

        locationProximitySearchOptInMeters = PreferenceManager.getDefaultSharedPreferences(context)
            .getInt(Constants.KEY_REMEBER_SEARCH_PROXIMITY_OPTION, 0)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = HomeFragmentBinding.inflate(inflater, container, false)
            val view = binding?.root

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        authNavigation(view)
        super.onViewCreated(view, savedInstanceState)
    }

    private fun authNavigation(view: View){
        authModel.context = context
        authModel.wampService = (activity as MainActivity).wampService
        authModel.disposable = (activity as MainActivity).disposable


        val observer = Observer<AppUser> { app_user ->

                Ukonect.isAppUserAuthenticated = false

                if (app_user.authenticated) {
                    Ukonect.isAppUserAuthenticated = true
                    viewConten(view)

                } else if (app_user.auth_state == AuthState.AUTH_STAGE_NONE) {

                    view.visibility = View.GONE//come back

                    var direction = HomeFragmentDirections.moveToNavGraphSignup()
                    navController.navigate(direction)
                }

            
        }

        // Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        authModel.auth.observe(viewLifecycleOwner, observer)

    }

    private fun viewConten(view: View){

        var mainActivity = (activity as MainActivity)

        mainActivity.supportActionBar?.setTitle(R.string.app_name)

        //Enable Location service
        GpsUtils(mainActivity).turnGPSOn(object : onGpsListener {
            override fun gpsStatus(isGPSEnable: Boolean) {
                // turn on GPS
                mainActivity.isGPSEnable = isGPSEnable
            }
        })

        binding?.homeSearchFab?.setOnClickListener {
            mainActivity.onSearchRequested()
        }

        binding?.homeSearchOptionFab?.setOnClickListener (getHomeSearchOptionFabClickListener())

        binding?.homeAddFab?.setOnClickListener{

                isRotate = rotateFab(it, !isRotate)

                if (isRotate) {
                    showIn(binding?.homeSearchFab)
                    showIn(binding?.homeSearchOptionFab)
                } else {
                    showOut(binding?.homeSearchFab)
                    showOut(binding?.homeSearchOptionFab)
                }

        }



        val telemamanger =  ContextCompat.getSystemService(this.requireContext(), TelephonyManager::class.java) as TelephonyManager

        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.READ_PHONE_STATE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            var app_user = authModel.app_user
            if( app_user !=null && !app_user.is_registered){
                if(telemamanger.line1Number != null && telemamanger.line1Number.isNotEmpty()) {
                    authModel.app_user?.line1Number = telemamanger.line1Number
                    authModel.app_user?.user_id = telemamanger.line1Number
                }
            }
        }else{
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }


        var contactsProvider: ContactsProvider = ContactsProvider(context)
        contacts = contactsProvider.contacts.list;
        model.wampService = (activity as MainActivity).wampService
        model.disposable = (activity as MainActivity).disposable
        model.phoneContacts = contacts
        model.context = context

        binding?.root?.post {

            // bind RecyclerView

            var column_count = 2
            var preferred_column_width = binding?.root?.context?.resources?.getDimensionPixelSize(R.dimen.user_small_card_width)?:0

            if(binding?.root?.width != 0){
                column_count = (binding?.root?.width ?: 0) / preferred_column_width
            }

            showHomeSearchRecyclerView(false)//hide initially

            binding?.homePeopleNearbyHeader?.listSubheaderTitle?.setText(R.string.people_you_may_know_nearby)

            var recyclerView: RecyclerView? = binding?.homePeopleNearbyRecyclerView
            recyclerView?.layoutManager = GridLayoutManager(this.context, column_count)
            userCardListAdapter = UserSmallCardListAdapter(this, "", false)
            recyclerView?.adapter = userCardListAdapter


            createObserversAndGetData()

        }
    }

    private fun showHomeSearchRecyclerView(show: Boolean){
        if(show){
            binding?.homePeopleNearbyHeader?.listSubheaderTitle?.visibility = View.VISIBLE
            binding?.homeSearchResultListView?.visibility = View.VISIBLE
        }else{
            binding?.homePeopleNearbyHeader?.listSubheaderTitle?.visibility = View.GONE
            binding?.homeSearchResultListView?.visibility = View.GONE
        }

    }

    private fun getHomeSearchOptionFabClickListener(): View.OnClickListener{
        return View.OnClickListener {
            val dialogLayout: View = layoutInflater.inflate(R.layout.location_search_options, null)

            var categorySpn = dialogLayout.findViewById<Spinner>(R.id.location_search_option_category_spn)
            var proximitySpn = dialogLayout.findViewById<Spinner>(R.id.location_search_option_proximity_spn)
            var rememberSearchOpt = dialogLayout.findViewById<CheckBox>(R.id.location_search_option_remember_options_chk)

            val categoryAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.search_categories, android.R.layout.simple_spinner_item
            )

            categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

            categorySpn.adapter = categoryAdapter

            val proximityAdapter = ArrayAdapter.createFromResource(
                requireContext(),
                R.array.search_location_proximities, android.R.layout.simple_spinner_item
            )

            proximityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            proximitySpn.adapter = proximityAdapter

            categorySpn.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    arg0: AdapterView<*>?,
                    arg1: View?,
                    position: Int,
                    id: Long
                ) {
                    var category = context?.resources?.getStringArray(R.array.search_categories)?.get(position)
                    categorySearchOpt = category?:""

                    if(rememberSearchOpt.isChecked){

                        PreferenceManager.getDefaultSharedPreferences(context)
                            .edit()
                            .putString(Constants.KEY_REMEBER_SEARCH_CATEGORY_OPTION, categorySearchOpt)
                            .apply()
                    }
                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {
                    categorySearchOpt = ""
                }
            }

            proximitySpn.onItemSelectedListener = object : OnItemSelectedListener {
                override fun onItemSelected(
                    arg0: AdapterView<*>?,
                    arg1: View?,
                    position: Int,
                    id: Long
                ) {

                    locationProximitySearchOptInMeters = Utils.getLocationProximityInt(requireContext(), position)

                    if(rememberSearchOpt.isChecked){

                        PreferenceManager.getDefaultSharedPreferences(context)
                            .edit()
                            .putInt(Constants.KEY_REMEBER_SEARCH_PROXIMITY_OPTION, locationProximitySearchOptInMeters)
                            .apply()
                    }

                }

                override fun onNothingSelected(arg0: AdapterView<*>?) {
                    locationProximitySearchOptInMeters = 0
                }
            }


            var builder =  AlertDialog.Builder(context)
                .setTitle("Location search options")
                .setView(dialogLayout)
                .setPositiveButton(R.string.ok) { var1, var2 ->
                    (activity as MainActivity).onSearchRequested()
                }


            builder.create()
            builder.show()


        }
    }

    private fun createObserversAndGetData(){
        var app_user = authModel.app_user
        // Create the observer which updates the UI.
        val observer = Observer<MutableList<User>> { users ->
            if (app_user != null) {
                userCardListAdapter?.setUserCardList(app_user, users)
            }
        }

        // Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        model.getList().observe(viewLifecycleOwner, observer)

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.search -> {
                //(activity as MainActivity).onSearchRequested()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.home_app_bar, menu)

        // Associate searchable configuration with the SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo((activity as  MainActivity).componentName))
            isIconifiedByDefault = false // Do not iconify the widget; expand it by default
            queryHint = getString(R.string.search_phone_no_or_name)

            val height = (activity as MainActivity).supportActionBar?.height ?: height

            layoutParams =  LinearLayout.LayoutParams(width, height) // using app bar size

            //i am bi-passing content provider implementation by using these listeners below
            //to do the search - using content provider approach is giving me trouble abeg

            /*val columNames =
                arrayOf(SearchManager.SUGGEST_COLUMN_TEXT_1)
            val viewIds = intArrayOf(android.R.id.text1)
            val adapter: CursorAdapter = SimpleCursorAdapter(
                activity,
                android.R.layout.simple_list_item_1, null, columNames, viewIds
            )

            suggestionsAdapter = adapter
            */

            setOnQueryTextListener(getSearchQueryTextListener())//with content provider approach no need for this
            setOnCloseListener (getSearchCloseListener())
            setOnSuggestionListener(getSearchSuggestionListener())//with content provider approach no need for this

        }

    }

    private fun getSearchCloseListener(): SearchView.OnCloseListener{
        return SearchView.OnCloseListener {

            //code body goes here

            true
        }
    }

    private fun getSearchQueryTextListener(): SearchView.OnQueryTextListener{
        var mainActivity = (activity as MainActivity)
        return object:SearchView.OnQueryTextListener{

            /**
             * triggers each time the user press the submit button of the SearchView
             */
            override fun onQueryTextSubmit(searchStr: String): Boolean {
                querySearch(searchStr)
                return true
            }

            /**
             * triggers each time the user type something
             */
            override fun onQueryTextChange(searchStr: String): Boolean {
                querySearch(searchStr)
                return true
            }

            fun querySearch(searchStr: String){

                if(categorySearchOpt?.isNotEmpty() == true  && !categorySearchOpt.equals("People", true)){
                    doGeoFencingSearch()
                }

                val observable  = mainActivity.wampService.getCaller()?.searchUsers(
                    searchStr ,searchOffset,
                    Constants.DEFAULT_SEARCH_LIMIT )

                observable?.let{
                    handleObservable(it);
                }

            }

            private fun doGeoFencingSearch() {

                Utils.logExternal(context, "doGeoFencingSearch()")

            }

            private fun handleObservable(observable: Observable<MutableList<User>>) {

                mainActivity.disposable = observable?.subscribeOn(Schedulers.io())
                    ?.observeOn(AndroidSchedulers.mainThread())
                    ?.subscribe(
                        { result -> onSearchPeopleSuccess(result)},
                        { error ->  onSearchPeopleError(error)}
                    )


            }

            private fun onSearchPeopleError(error: Throwable?) {
                if (error != null) {
                    Utils.logExternal(context, error) //TODO
                }
            }

            private fun onSearchPeopleSuccess(users: MutableList<User>) {
                //first show the list view
                showHomeSearchRecyclerView(false)

                var meters = ""
                var category = ""
                var away = ""
                if(categorySearchOpt?.isNotEmpty() == true && locationProximitySearchOptInMeters > 0){
                    category = "people"

                    if(locationProximitySearchOptInMeters >= 1000){
                        away =  "${locationProximitySearchOptInMeters/1000}km away"
                    }else{
                        away =  "${locationProximitySearchOptInMeters}m away"
                    }
                }

                //now populate with the search result
                binding?.homePeopleNearbyHeader?.listSubheaderTitle?.text = "Search result for people..."+away

                var listView = binding?.homeSearchResultListView
                var adapter = HomeSearchListAdapter(requireContext())
                listView?.adapter = adapter
                adapter.setHomeSearchListViewList(createPeopleListData(users))



            }

            private fun createPeopleListData(users: MutableList<User>): MutableList<ListItemData> {

                var list = mutableListOf<ListItemData>()

                users.forEach{user->
                    var item = ListItemData()
                    item.type = HomeSearchListAdapter.Type.TYPE_1.ordinal
                    item.text = if(user.display_name.isNotEmpty()) user.display_name else "${user.first_name}  ${user.last_name}"
                    item.sub_text = user.location.address + "\n" +distanceAwayAtTime(user.location.longitude, user.location.latitude, user.location.time)
                    item.left_image_url = user.photo_url
                    item.left_image_placeholder = context?.resources?.getDrawable(R.drawable.ic_person_blue_70dp)
                    list.add(item)
                }

                return list
            }

            private fun distanceAwayAtTime(longitude: Double?, latitude: Double?, location_time: Long?): String? {

                var app_user_lat = Ukonect.appUser?.location?.latitude?:0.0
                var other_user_lat = latitude?:0.0
                var app_user_lng = Ukonect.appUser?.location?.longitude?:0.0
                var other_user_lng = longitude?:0.0

                var display = "Unknown"
                if(app_user_lat !=0.0
                    && other_user_lat !=0.0
                    && app_user_lng !=0.0
                    && other_user_lng !=0.0) {
                    var distanceInKm = Utils.distanceOnEarthInKilometers(
                        app_user_lat,
                        other_user_lat,
                        app_user_lng,
                        other_user_lng
                    )

                    val df = DecimalFormat("#.##")
                    df.roundingMode = RoundingMode.CEILING

                    if (distanceInKm < 1) {
                        display =
                            "${df.format(distanceInKm * 1000)}m ${Utils.formatLocationTime(Date(location_time?:0))}"
                    } else {
                        display =
                            "${df.format(distanceInKm)}Km ${Utils.formatLocationTime(
                                Date(
                                    location_time ?: 0
                                )
                            )}"
                    }


                }

                return "TODO e.g 10m away at (5:58pm or 3mins ago)"
            }

        }
    }


    private fun getSearchSuggestionListener(): SearchView.OnSuggestionListener{
        return object:SearchView.OnSuggestionListener{
            override fun onSuggestionSelect(p0: Int): Boolean {

                return true
            }

            override fun onSuggestionClick(p0: Int): Boolean {

                return true
            }

        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
