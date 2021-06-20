package com.ukonect.www.ui.sms

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.*
import android.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.MainActivity
import com.ukonect.www.R
import com.ukonect.www.data.AppUser
import com.ukonect.www.databinding.SmsViewFragmentBinding
import com.ukonect.www.phone.PhoneSms
import com.ukonect.www.ui.binding.SmsViewAdapter
import com.ukonect.www.ui.main.UserListModel
import com.ukonect.www.ui.main.MainViewModel
import com.ukonect.www.util.Constants
import com.ukonect.www.util.Utils
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import me.everything.providers.android.telephony.TelephonyProvider


class SmsViewFragment : Fragment() {
    private var disposable: Disposable? = null
    private var message: String = ""
    private var phoneNo: String = ""
    private val MY_PERMISSIONS_REQUEST_SEND_SMS = 1
    private var recyclerView: RecyclerView? = null
    private val usersModel: UserListModel by activityViewModels()
    private val authModel: MainViewModel by activityViewModels()
    private val navController by lazy { findNavController() }
    private var _binding: SmsViewFragmentBinding? = null
    private var smsViewAdapter: SmsViewAdapter? =null

    // This property is only valid between onCreateView and
    // onDestroyView.

    private val binding get() = _binding!!

    companion object {
        fun newInstance() = SmsViewFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = SmsViewFragmentBinding.inflate(inflater, container, false)
        val view = binding.root

        // bind RecyclerView
        recyclerView = binding.smsRecyclerView
        var linerLayoutMgr = LinearLayoutManager(this.context)
        //linerLayoutMgr.stackFromEnd = true //will set the view to show the last element
        recyclerView?.layoutManager = linerLayoutMgr;
        smsViewAdapter = SmsViewAdapter()
        recyclerView?.adapter = smsViewAdapter

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        (activity as MainActivity).supportActionBar?.setTitle(R.string.empty)
        display()
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun display(){

        var app_user: AppUser = authModel.app_user ?: return

        var other_user_id = arguments?.getString(Constants.USER_ID)?:""
        var other_user_phone_no = arguments?.getString(Constants.PHONE_NO)?:""
        var sms_type = arguments?.getInt(Constants.SMS_TYPE)


        disposable = Observable.fromCallable{

            var phoneSms = PhoneSms(
                requireContext(),
                app_user,
                usersModel
            )
            //return the line below
            phoneSms

        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { phoneSms ->

                    var filter: TelephonyProvider.Filter = TelephonyProvider.Filter.ALL

                    when(sms_type){
                        TelephonyProvider.Filter.INBOX.ordinal -> filter = TelephonyProvider.Filter.INBOX
                        TelephonyProvider.Filter.OUTBOX.ordinal -> filter = TelephonyProvider.Filter.OUTBOX
                        TelephonyProvider.Filter.SENT.ordinal -> filter = TelephonyProvider.Filter.SENT
                        TelephonyProvider.Filter.DRAFT.ordinal -> filter = TelephonyProvider.Filter.DRAFT
                    }

                    var is_all_type = filter == TelephonyProvider.Filter.ALL

                    var sms_list = phoneSms.getMessages(filter)
                    smsViewAdapter?.setSmsViewList(app_user, other_user_id, sms_list, is_all_type)
                    recyclerView?.scrollToPosition(smsViewAdapter?.itemCount?.minus(1)?: 0)
                    binding.smsToUserInput.smsToUserTxtSelectedPhoneNos.text.append(other_user_phone_no)
                    binding.smsInputInclude.smsInputSendMsgBtn.setOnClickListener {
                        sendSMSMessage()
                    }
                },
                { error ->  Utils.handleException(context,  error) }
            )



    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {

        menu.clear() // clear the initial ones, otherwise they are included

        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.sms_app_bar, menu)

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

    private fun sendSMSMessage() {
        phoneNo = binding.smsToUserInput.smsToUserTxtSelectedPhoneNos.text.toString().replace(" ", "")
        message = binding.smsInputInclude.smsInputText.text.toString()

        if(phoneNo.isEmpty() || message.isEmpty())
        {
            return;
        }

        if (ContextCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.SEND_SMS
            )
            == PackageManager.PERMISSION_GRANTED
        ) {
            doSendSMS()
        }else{
            ActivityCompat.requestPermissions(
                this.requireActivity(), arrayOf(Manifest.permission.SEND_SMS),
                MY_PERMISSIONS_REQUEST_SEND_SMS
            )
        }
    }

    private fun doSendSMS(){
        val smsManager: SmsManager = SmsManager.getDefault()
        smsManager.sendTextMessage(phoneNo, null, message, null, null)

        phoneNo = ""//initialize
        message = ""//initialize
        binding.smsToUserInput.smsToUserTxtSelectedPhoneNos.text.clear()//initialize
        binding.smsInputInclude.smsInputText.text.clear()//initialize

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_SEND_SMS -> {
                if (grantResults.isNotEmpty()
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    doSendSMS()
                } else {
                    //TODO send remote log indicating the error - Could not send SMS due to permission denial
                    return
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable?.dispose()
    }
}
