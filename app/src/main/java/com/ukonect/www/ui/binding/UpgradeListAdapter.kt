package com.ukonect.www.ui.binding

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import com.ukonect.www.R
import com.ukonect.www.data.AppUser
import com.ukonect.www.data.ListItemData
import com.ukonect.www.databinding.ListTwoTextRightLeftImageBinding
import com.ukonect.www.ui.upgrade.UpgradeFragmentDirections
import com.ukonect.www.util.Constants

class UpgradeListAdapter (private val cxt: Context,
                          private val navController: NavController,
                          private val app_user: AppUser?) : ArrayAdapter<String?>(cxt, 0) {

    var items = mutableListOf<ListItemData>().also {
        createListData(it)
    }

    enum class Type {
        TYPE_1
    }


    private fun createListData(it: MutableList<ListItemData>) {

        var free_users = ListItemData()
        free_users.type = Type.TYPE_1.ordinal
        free_users.text = Constants.STR_FREE_USERS
        free_users.sub_text = "Basic features available"
        free_users.left_image_placeholder = cxt.getDrawable(R.drawable.ic_star_half_gray_100dp)
        free_users.right_image_placeholder = if(app_user?.user_type == Constants.FREE_USERS )
            cxt.getDrawable(R.drawable.ic_check_green_24dp)
        else
            null

        var silver_users = ListItemData()
        silver_users.type = Type.TYPE_1.ordinal
        silver_users.text = Constants.STR_SILVER_USERS
        silver_users.sub_text = "More exiciting features - No ads"
        silver_users.left_image_placeholder = cxt.getDrawable(R.drawable.ic_star_silver_100dp)
        silver_users.right_image_placeholder = if(app_user?.user_type == Constants.SILVER_USERS )
            cxt.getDrawable(R.drawable.ic_check_green_24dp)
        else
            null

        var gold_users = ListItemData()
        gold_users.type = Type.TYPE_1.ordinal
        gold_users.text = Constants.STR_GOLD_USERS
        gold_users.sub_text = "Unlimited access - No ads"
        gold_users.left_image_placeholder = cxt.getDrawable(R.drawable.ic_star_gold_100dp)
        gold_users.right_image_placeholder = if(app_user?.user_type == Constants.GOLD_USERS )
            cxt.getDrawable(R.drawable.ic_check_green_24dp)
        else
            null


        it.add(free_users)
        it.add(silver_users)
        it.add(gold_users)

    }

    @SuppressLint("ViewHolder")
    override fun getView(
        position: Int,
        view: View?,
        parent: ViewGroup
    ): View {


        val type = getItemViewType(position)

        if(type == Type.TYPE_1.ordinal) {

            var listViewItem = ListTwoTextRightLeftImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            listViewItem.item = items[position]

            listViewItem.root.setOnClickListener {

                val item = items[position]
                var upgrade = ""
                if(item.text == Constants.STR_FREE_USERS){
                    upgrade = Constants.STR_FREE_USERS
                }

                if(item.text == Constants.STR_SILVER_USERS){
                    upgrade = Constants.STR_SILVER_USERS
                }

                if(item.text == Constants.STR_GOLD_USERS){
                    upgrade = Constants.STR_GOLD_USERS
                }

                val bundle = bundleOf(Constants.UPGRADE to upgrade)
                var direction = UpgradeFragmentDirections.toUpgradePaymentFragment()
                navController.navigate(direction.actionId, bundle)
            }

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

}