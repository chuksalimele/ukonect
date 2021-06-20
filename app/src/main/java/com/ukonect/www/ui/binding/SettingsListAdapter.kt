package com.ukonect.www.ui.binding

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import com.ukonect.www.data.ListItemData
import com.ukonect.www.databinding.ListTextSwitchButtonItemBinding
import com.ukonect.www.databinding.ListTwoTextRightImageBinding
import com.ukonect.www.util.Constants

class SettingsListAdapter (private val cxt: Context) : ArrayAdapter<String?>(cxt, 0) {

    var items = mutableListOf<ListItemData>().also {
        createListData(it)
    }

    enum class Type {
        TYPE_1, TYPE_2
    }


    private fun createListData(it: MutableList<ListItemData>) {

        var notification = ListItemData()
        notification.type = Type.TYPE_1.ordinal
        notification.text = Constants.STR_NOTIFICATION
        notification.sub_text = "Vibrate only" //e.g Mute, Vibrate only, Vibrate & Sound

        var geosensing = ListItemData()
        geosensing.type = PersonalProfileSettingsListAdapter.Type.TYPE_1.ordinal
        geosensing.text = Constants.STR_GEOSENSING
        geosensing.sub_text = "Default"

        var location_proximity = ListItemData()
        location_proximity.type = Type.TYPE_1.ordinal
        location_proximity.text = Constants.STR_LOCATION_PROXIMITY
        location_proximity.sub_text = "Default"

        var font_size = ListItemData()
        font_size.type = Type.TYPE_1.ordinal
        font_size.text = Constants.STR_FONT_SIZE
        font_size.sub_text = "Medium"

        var backup_contacts = ListItemData()
        backup_contacts.type = Type.TYPE_2.ordinal
        backup_contacts.text = Constants.STR_AUTO_BACKUP_CONTACTS
        backup_contacts.sub_text = "Yes"



        it.add(notification)
        it.add(geosensing)
        it.add(location_proximity)
        it.add(font_size)
        it.add(backup_contacts)

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

            listViewItem.root.setOnClickListener(getViewOnClickListener(position))

            return listViewItem.root

        }else if(type == Type.TYPE_2.ordinal) {

            var listViewItem = ListTextSwitchButtonItemBinding.inflate(
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
                handleNotification()
            }

            if(item.text == Constants.STR_LOCATION_PROXIMITY){
                handleLocationProximity()
            }

            if(item.text == Constants.STR_GEOSENSING){
                handleGeosensing()
            }

            if(item.text == Constants.STR_FONT_SIZE){
                handleFontSize()
            }

            if(item.text == Constants.STR_AUTO_BACKUP_CONTACTS){
                handleBackupContacts()
            }

        }
    }

    private fun handleGeosensing() {
        val builder = AlertDialog.Builder(this.cxt)
        // Set the dialog title
        builder.setTitle("Geosensing")
            .setMessage("TODO")


        builder.create()
        builder.show()
    }

    private fun handleLocationProximity() {

        val builder = AlertDialog.Builder(this.cxt)
        // Set the dialog title
        builder.setTitle("Location Proximity")
            .setMessage("TODO")


        builder.create()
        builder.show()
    }

    private fun handleNotification() {
        val arr = arrayOf<String>("Mute","Vibrate Only","Vibrate & Sound")

        val builder = AlertDialog.Builder(this.cxt)
        // Set the dialog title
        builder.setTitle("Notification")
            .setSingleChoiceItems(arr, 0 //TODO Set the checked item
            ) { dialog, which ->

            }


        builder.create()
        builder.show()
    }

    private fun handleFontSize() {
        val arr = arrayOf<String>("Small","Medium","Large")

        val builder = AlertDialog.Builder(this.cxt)
        // Set the dialog title
        builder.setTitle("Font Size")
            .setSingleChoiceItems(arr, 0, //TODO Set the checked item
                DialogInterface.OnClickListener { dialog, which ->

                })


        builder.create()
        builder.show()
    }

    private fun handleBackupContacts() {
        //TODO - SAVE TO PREFERENCE
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

}