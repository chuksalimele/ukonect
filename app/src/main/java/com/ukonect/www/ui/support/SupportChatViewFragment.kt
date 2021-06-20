package com.ukonect.www.ui.support

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.msg.ChatMessage
import com.ukonect.www.databinding.SupportChatViewFragmentBinding
import com.ukonect.www.ui.binding.SupportChatAdapter
import com.ukonect.www.ui.main.MainViewModel

class SupportChatViewFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private val model: SupportChatViewViewModel by viewModels()
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var suppertChatAdapter: SupportChatAdapter? =null

    private var _binding: SupportChatViewFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = SupportChatViewFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = SupportChatViewFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root


        // bind RecyclerView
        recyclerView = binding?.supportChatViewRecyclerView
        var linerLayoutMgr = LinearLayoutManager(this.context)
        //linerLayoutMgr.stackFromEnd = true //will set the view to show the last element
        recyclerView?.layoutManager = linerLayoutMgr;
        suppertChatAdapter = SupportChatAdapter()
        recyclerView?.adapter = suppertChatAdapter

        createObserversAndGetData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.support_chat)
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createObserversAndGetData(){
        var app_user = authModel.app_user
        model.context = requireContext()
        // Create the observer which updates the UI.
        val observer = Observer<MutableList<ChatMessage>> { chat_list ->
            if (app_user != null) {

                binding?.supportChatViewSubheader?.chateMate  = model.app_user_chatmate
                suppertChatAdapter?.setSupportChatList(app_user, chat_list)

                recyclerView?.scrollToPosition(suppertChatAdapter?.itemCount?.minus(1)?: 0)

            }
        }

        // Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        model.getList().observe(viewLifecycleOwner, observer)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.support_chat_view_app_bar, menu)

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
