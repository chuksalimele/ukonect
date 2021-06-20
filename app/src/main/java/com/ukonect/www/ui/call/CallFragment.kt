package com.ukonect.www.ui.call

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.constraintlayout.widget.Constraints.TAG
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.databinding.CallFragmentBinding
import com.google.android.material.tabs.TabLayoutMediator


class CallFragment : Fragment() {

    private val MY_PERMISSIONS_REQUEST_CALL_PHONE = 1
    private val navController by lazy { findNavController() }
    private var viewPager: ViewPager2? = null
    private var tab_titles = arrayOf<String>("DIALLED","RECEIVED","MISSED")
    private var _binding: CallFragmentBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = CallFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = CallFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root

        //callList?.get(0)?.id != app_user.phone // it is received call

        binding?.callViewPager?.adapter = CallTabViewPagerAdapter(this, tab_titles)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.calls)
        val tabLayout = binding?.callTabLayout
        viewPager = binding?.callViewPager

        if (tabLayout != null) {
            viewPager?.let {
                TabLayoutMediator(tabLayout, it) { tab, position ->
                    tab.text = tab_titles[position]

                    //tab.icon = ...//TODO
                    //tab.badge = ...//TODO
                }.attach()
            }
        }

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun checkForPhonePermission() {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.CALL_PHONE
            ) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not yet granted. Use requestPermissions().
            Log.d(TAG, getString(R.string.permission_not_granted))
            ActivityCompat.requestPermissions(
                this.requireActivity(), arrayOf(Manifest.permission.CALL_PHONE),
                MY_PERMISSIONS_REQUEST_CALL_PHONE
            )
        } else {
            // Permission already granted.
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_CALL_PHONE -> {
                if (permissions[0]
                        .equals(Manifest.permission.CALL_PHONE, ignoreCase = true)
                    && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission was granted.
                } else {
                    // Permission denied.
                    Log.d(TAG, getString(R.string.failure_permission))
                    Toast.makeText(
                        this.requireContext(),
                        getString(R.string.failure_permission),
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.call_app_bar, menu)


        // Associate searchable configuration with the SearchView
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        (menu.findItem(R.id.search).actionView as SearchView).apply {
            setSearchableInfo(searchManager.getSearchableInfo((activity as  MainActivity).componentName))
            isIconifiedByDefault = false // Do not iconify the widget; expand it by default

            setOnQueryTextListener(getSearchQueryTextListener())
            setOnCloseListener (getSearchCloseListener())

        }

    }

    private fun getSearchCloseListener(): SearchView.OnCloseListener{
        return SearchView.OnCloseListener {

            //code body goes here

            true
        }
    }

    private fun getSearchQueryTextListener(): SearchView.OnQueryTextListener{

        return object:SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {


                return true
            }

            override fun onQueryTextChange(p0: String?): Boolean {


                return true
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
