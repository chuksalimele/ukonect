package com.ukonect.www.ui.binding

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.ukonect.www.data.ListItemData
import com.ukonect.www.databinding.ListTwoTextLeftImageBinding
import com.ukonect.www.util.Constants

class HomeSearchListAdapter (private val cxt: Context) : ArrayAdapter<String?>(cxt, 0) {

    var items = mutableListOf<ListItemData>()

    enum class Type {
        TYPE_1, TYPE_2
    }



    @SuppressLint("ViewHolder")
    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup
    ): View {

        val type = getItemViewType(position)

        if(type == Type.TYPE_1.ordinal) {
            var listViewItem = ListTwoTextLeftImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            listViewItem.item = items[position]

            listViewItem.root.setOnClickListener(getViewOnClickListener(position))

            return listViewItem.root

        }


        return null!!
    }

    private fun getViewOnClickListener(position: Int):View.OnClickListener{
        return View.OnClickListener{

            val item = items[position]

            if(item.text == Constants.STR_NOTIFICATION){
                //handleNotification()
            }

            if(item.text == Constants.STR_LOCATION_PROXIMITY){
                //handleLocationProximity()
            }

            if(item.text == Constants.STR_GEOSENSING){
                //handleGeosensing()
            }

            if(item.text == Constants.STR_FONT_SIZE){
                //handleFontSize()
            }

            if(item.text == Constants.STR_AUTO_BACKUP_CONTACTS){
                //handleBackupContacts()
            }

        }
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

    fun setHomeSearchListViewList(list_itmes: MutableList<ListItemData>){
        this.items = list_itmes
        notifyDataSetChanged()
    }
}