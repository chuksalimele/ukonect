package com.ukonect.www.remote.update

import com.ukonect.www.data.OnlineStatus

interface IUserOnlineStatusUpdate {
    fun onOnlineStatus(online_status: OnlineStatus);
}