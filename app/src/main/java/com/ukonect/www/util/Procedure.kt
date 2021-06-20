package com.ukonect.www.util

import com.laurencegarmstrong.kwamp.core.Uri
import com.ukonect.www.Ukonect.Companion.appUser


object Procedure {

    fun getocationUri(): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.${appUser?.user_id}.getLocation")
    }

    fun checkIsUserOnlineUri(): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.${appUser?.user_id}.checkIsOnline")
    }
}