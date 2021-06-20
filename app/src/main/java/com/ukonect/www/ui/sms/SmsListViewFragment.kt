package com.ukonect.www.ui.sms

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Message
import com.ukonect.www.databinding.SmsListViewFragmentBinding
import com.ukonect.www.phone.PhoneSms
import com.ukonect.www.ui.binding.SmsListViewAdapter
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.ui.main.MainViewModel
import me.everything.providers.android.telephony.TelephonyProvider


class SmsListViewFragment : Fragment() {

    private var recyclerView: RecyclerView? = null
    private val usersModel: UserListModel by activityViewModels()
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var smsListViewAdapter: SmsListViewAdapter? =null
    private var _binding: SmsListViewFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!

    companion object {
        fun newInstance() = SmsListViewFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SmsListViewFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        // bind RecyclerView
        recyclerView = binding.smsListRecyclerView
        recyclerView?.setLayoutManager(LinearLayoutManager(this.context));
        refreshRecylcerView()


        return view
    }

    fun refreshRecylcerView(){
        //recyclerView?.removeAllViewsInLayout()//clear all view
        smsListViewAdapter = SmsListViewAdapter(navController)
        recyclerView?.adapter = smsListViewAdapter
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.inbox)
        displayInbox()

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun display(filter: TelephonyProvider.Filter){

        var app_user: AppUser = authModel.app_user ?: return

        var phoneSms = PhoneSms(
            this.requireContext(),
            app_user,
            usersModel
        )

        var strTitle = ""
        when(filter){
            TelephonyProvider.Filter.ALL -> strTitle = "All"
            TelephonyProvider.Filter.INBOX -> strTitle = "Inbox"
            TelephonyProvider.Filter.OUTBOX -> strTitle = "Outbox"
            TelephonyProvider.Filter.SENT -> strTitle = "Sent"
            TelephonyProvider.Filter.DRAFT -> strTitle = "Draft"
        }

        var smsMessageList = phoneSms.getMessages(filter)

        refreshRecylcerView()

        smsListViewAdapter?.setSmsListViewList(app_user, smsMessageList)
        val count = smsMessageList.count();
        strTitle = "$strTitle ($count)"

        if(filter == TelephonyProvider.Filter.INBOX) {
            val unread_count = smsMessageList.count { it.status != Message.MSG_STATUS_READ };
            strTitle = "$strTitle - $unread_count unread"
        }

        (this.activity as MainActivity).supportActionBar?.title = strTitle

    }

    private fun displayAll() {
        display(TelephonyProvider.Filter.ALL)
    }

    private fun displayDraft() {
        display(TelephonyProvider.Filter.DRAFT)
    }

    private fun displaySent() {
        display(TelephonyProvider.Filter.SENT)
    }

    private fun displayOutbox() {
        display(TelephonyProvider.Filter.OUTBOX)
    }

    private fun displayInbox() {
        display(TelephonyProvider.Filter.INBOX)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.sms_list_app_bar, menu)

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sms_app_bar_all -> {
                displayAll()
                true
            }
            R.id.sms_app_bar_inbox -> {
                displayInbox()
                true
            }
            R.id.sms_app_bar_outbox -> {
                displayOutbox()
                true
            }
            R.id.sms_app_bar_sent -> {
                displaySent()
                true
            }
            R.id.sms_app_bar_draft -> {
                displayDraft()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

}
