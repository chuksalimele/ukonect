package com.ukonect.www.ui.chat

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
import com.ukonect.www.databinding.ChatMeUpFragmentBinding
import com.ukonect.www.ui.binding.ChatMeUpAdapter
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants
import com.ukonect.www.util.Utils

class PrivateChatFragment : Fragment() {
    private var recyclerView: RecyclerView?= null
    private val privateChatModel: PrivateChatViewModel by viewModels()
    private val authModel: MainViewModel by activityViewModels()
    private val usersModel: UserListModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var chatMeUpAdapter: ChatMeUpAdapter? =null
    private var _binding: ChatMeUpFragmentBinding? = null
    private var otherUserId = arguments?.getString(Constants.USER_ID)
    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding

    companion object {
        fun newInstance() = PrivateChatFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ChatMeUpFragmentBinding.inflate(inflater, container, false)
        val view = binding?.root

        // bind RecyclerView
        recyclerView = binding?.chatMeUpRecyclerView
        var linerLayoutMgr = LinearLayoutManager(this.context)
        //linerLayoutMgr.stackFromEnd = true //will set the view to show the last element
        recyclerView?.layoutManager = linerLayoutMgr;
        chatMeUpAdapter = ChatMeUpAdapter()
        recyclerView?.adapter = chatMeUpAdapter

        createObserversAndGetData()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as MainActivity).wampService.setChatUpdater(privateChatModel)

        val toolbar = binding?.chatMeUpAppBarToolbar

        var mainActivity = this.activity as MainActivity
        mainActivity.setSupportActionBar(toolbar)

        mainActivity.supportActionBar?.setTitle(R.string.empty)

        binding?.
        chatMeUpInputInclude?.
        chatViewSendMsgBtn?.setOnClickListener {
            val text = binding?.
            chatMeUpInputInclude?.chatViewText?.text.toString()
            var msg = ChatMessage()
            msg.message_id = Utils.unique()
            msg.from_user_id = authModel.app_user?.user_id?:""
            msg.to_user_id = otherUserId?:"";
            msg.text = text
            //other properties(e.g the topic, time) of the msg will be filled by the backend

            mainActivity.wampService.getPublisher()?.chatMessgae(msg)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun createObserversAndGetData(){
        var app_user = authModel.app_user

        privateChatModel.context = context
        privateChatModel.wampService = (activity as MainActivity).wampService

        // Create the observer which updates the UI.
        val observer = Observer<MutableList<ChatMessage>> { chat_list ->
            if (app_user != null) {

                //Update the Header
                binding?.chatMeUpAppHeader?.user = usersModel.getUser(otherUserId)

                chatMeUpAdapter?.setChatMeUpList(app_user, otherUserId,  chat_list)
                recyclerView?.scrollToPosition(chatMeUpAdapter?.itemCount?.minus(1)?:0)
            }
        }

        // Observe the LiveData, passing in this fragment LifecycleOwner and the observer.
        privateChatModel.getList().observe(viewLifecycleOwner, observer)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.chat_me_up_app_bar, menu)

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
