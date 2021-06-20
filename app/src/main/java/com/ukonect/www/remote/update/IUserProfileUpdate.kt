package com.ukonect.www.remote.update

import com.ukonect.www.data.User

interface IUserProfileUpdate {
    fun onProfileUpdate(user: User);
}