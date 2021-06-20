package com.ukonect.www.ui.binding

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Space
import androidx.recyclerview.widget.RecyclerView
import com.ukonect.www.R
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.Call
import com.ukonect.www.data.User
import com.ukonect.www.databinding.CallListItemBinding


class CallListAdapter(val type: Int) :
    RecyclerView.Adapter<CallListAdapter.CallListViewHolder>() {
    private var call_map_by_type = mutableMapOf<Any, Any> ()
    private  var keys = call_map_by_type.keys.toList()
    private var call_list_all = listOf<Call> ()
    private var app_user: AppUser = AppUser();

    private val FOOTER = "FOOTER"

    private val FOOTER_TYPE = 1
    private val ITEM_TYPE = 2


    private fun isFooter(i:Int): Boolean{
        return keys[i] is String &&  keys[i] == FOOTER
    }

    private fun isCallObject(i:Int): Boolean{
        return keys[i] is Int && call_map_by_type[keys[i]] is Call
    }

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        i: Int
    ): CallListViewHolder {

        if(i == FOOTER_TYPE){
            var space =Space(viewGroup.context)
            space.layoutParams = LinearLayout.LayoutParams(
                //width
                LinearLayout.LayoutParams.MATCH_PARENT,
                //height
                viewGroup.context.resources.getDimensionPixelOffset(R.dimen.list_item_large_height)
            )

            return CallListViewHolder(space)

        }else {
            val callListItemBinding = CallListItemBinding.inflate(
                LayoutInflater.from(viewGroup.context),
                viewGroup,
                false
            )

            return CallListViewHolder(
                callListItemBinding
            )
        }

    }

    override fun onBindViewHolder(
        callListViewListViewHolder: CallListViewHolder,
        i: Int
    ) {

        if(isFooter(i)){
            //TODO - may added click event to footer
        }else {
            val currentCall: Call = call_map_by_type[i] as Call
            val currentUser: User = currentCall.user

            callListViewListViewHolder.callListItemBinding?.call = currentCall
            callListViewListViewHolder.callListItemBinding?.user = currentUser
            callListViewListViewHolder.callListItemBinding?.appUser = this.app_user

            callListViewListViewHolder.callListItemBinding?.callImgBtn?.setOnClickListener{
                currentCall.callPhoneNumber(it.context)
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        if(isFooter(position)){
            return FOOTER_TYPE
        }else{
            return ITEM_TYPE
        }
    }

    override fun getItemCount(): Int {
        return call_map_by_type.size
    }

    private fun filterByType(){
        //filter the call list based on the call type
        call_list_all.filter { it.type== type }.also { list ->
            var index = 0
            list.forEach{
                call_map_by_type[index]=it
                index++
            }
            call_map_by_type[FOOTER] = ""
            keys = call_map_by_type.keys.toList()
        }
    }

    fun setCallList(app_user: AppUser, call_list: MutableList<Call>) {
        this.app_user = app_user


        //sort in desceding order
        this.call_list_all = call_list.sortedWith(Comparator<Call> { a, b ->
            val time_a = a.time?.time ?: 0
            val time_b = b.time?.time ?: 0
            when {
                time_a < time_b -> 1
                time_a == time_b -> 0
                else -> -1
            }
        })

        this.filterByType()

        notifyDataSetChanged()
    }

    inner class CallListViewHolder : RecyclerView.ViewHolder{
        var callListItemBinding: CallListItemBinding? = null
        constructor(binding: CallListItemBinding):super(binding.root){
            callListItemBinding = binding
        }
        constructor(space: Space):super(space)

    }
}