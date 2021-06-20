package com.ukonect.www.data

import android.widget.Switch

class AppUser : User() {

    var auth_key = ""
    var line1Number: String = ""
    var favourite_list = mutableSetOf<String>()

    val authenticated get() = auth_state == AuthState.AUTH_STAGE_SUCCESS

    var auth_state = AuthState.AUTH_STAGE_NONE

    fun modifyAuth(stage: AuthState): AppUser{
        auth_state = stage
        return this
    }

    fun isFavourite(user_id: String): Boolean{
        return favourite_list.contains(user_id)
    }

    fun modifyFavourite(view: Switch, user_id: String): Unit {

        if(view.isChecked){
            favourite_list.add(user_id)
        }else{
            favourite_list.remove(user_id)
        }

        //TODO - save changes to system
    }

}