package com.ukonect.www.ui.contacts

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.User
import com.ukonect.www.databinding.ContactsFragmentBinding
import com.ukonect.www.ui.binding.ContactListAdapter
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.ui.main.MainViewModel

class ContactsFragment : Fragment() {

    private val usersModel: UserListModel by activityViewModels()
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var contactListAdapter: ContactListAdapter? =null
    private var _binding: ContactsFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = ContactsFragment()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = ContactsFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root

        // bind RecyclerView
        var recyclerView: RecyclerView? = binding?.contatsRecyclerView
        recyclerView?.setLayoutManager(LinearLayoutManager(this.context));
        contactListAdapter = ContactListAdapter()
        recyclerView?.adapter = contactListAdapter

        createObserversAndGetData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createObserversAndGetData(){
        var app_user = authModel.app_user

        // Create the observer which updates the UI.
        val observer = Observer<MutableList<User>> { users ->
            if (app_user != null) {

                //filter contacts from the list of users
                var contacts = users.filter { it.is_contact }.toMutableList()

                contactListAdapter?.setContactList(app_user, contacts)
                var title = contacts.size.toString() + " Contacts"
                if(contacts.size < 2){
                    title = contacts.size.toString() + " Contact"
                }

               (this.activity as MainActivity).supportActionBar?.title = title
            }
        }

        // Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        usersModel.getList().observe(viewLifecycleOwner, observer)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.contacts_app_bar, menu)

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
