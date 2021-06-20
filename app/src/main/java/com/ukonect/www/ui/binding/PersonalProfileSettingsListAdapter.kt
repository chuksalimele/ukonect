package com.ukonect.www.ui.binding

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.ukonect.www.data.ListItemData
import com.ukonect.www.databinding.ListTextSwitchButtonItemBinding
import com.ukonect.www.databinding.ListTwoTextRightImageBinding
import com.ukonect.www.util.Constants


class PersonalProfileSettingsListAdapter(private val cxt: Context) : ArrayAdapter<String?>(cxt, 0) {

    var items = mutableListOf<ListItemData>().also {
            createListData(it)
        }

    enum class Type {
        TYPE_1, TYPE_2
    }


    private fun createListData(it: MutableList<ListItemData>) {

        var location_proximity = ListItemData()
        location_proximity.type = Type.TYPE_1.ordinal
        location_proximity.text = Constants.STR_LOCATION_PROXIMITY
        location_proximity.sub_text = "Default"


        var record_call = ListItemData()
        record_call.type = Type.TYPE_2.ordinal
        record_call.text = Constants.STR_RECORD_CALLS
        record_call.sub_text = "Default"

        it.add(location_proximity)
        it.add(record_call)
    }


    @SuppressLint("ViewHolder")
    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup
    ): View {


        val type = getItemViewType(position)

        if(type == Type.TYPE_1.ordinal) {

            var listViewItem = ListTwoTextRightImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            listViewItem.item = items[position]

            listViewItem.root.setOnClickListener(ItemClickListener(position))

            return listViewItem.root

        }else if(type == Type.TYPE_2.ordinal) {

            var listViewItem = ListTextSwitchButtonItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            listViewItem.item = items[position]

            listViewItem.root.setOnClickListener(ItemClickListener(position))

            return listViewItem.root

        }



        return null!!
    }

    // Returns the number of types of Views that will be created by getView(int, View, ViewGroup)
    override fun getViewTypeCount(): Int {
        // Returns the number of types of Views that will be created by this adapter
        // Each type represents a set of views that can be converted

        return Type.values().size
    }

    // Get the type of View that will be created by getView(int, View, ViewGroup)
    // for the specified item.
    override fun getItemViewType(position: Int): Int {
        // Return an integer here representing the type of View.
        // Note: Integers must be in the range 0 to getViewTypeCount() - 1
        return items[position].type
    }

    override fun getCount(): Int {
        return items.size
    }

    private fun handleRecordCalls() {

    }

    private fun handleLocationProximity() {

        val builder = AlertDialog.Builder(this.cxt)
        // Set the dialog title
        builder.setTitle("Location Proximity")
            .setMessage("TODO")


        builder.create()
        builder.show()
    }

    inner class ItemClickListener(val position: Int)  : View.OnClickListener{
        override fun onClick(p0: View?) {
            val item = items[position]

            if(item.text == Constants.STR_LOCATION_PROXIMITY){
                handleLocationProximity()
            }

            if(item.text == Constants.STR_RECORD_CALLS){
                handleRecordCalls()
            }
        }

    }
}