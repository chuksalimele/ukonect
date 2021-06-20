package com.ukonect.www.remote.model

import com.ukonect.www.data.AppUser
import com.ukonect.www.data.User
import com.ukonect.www.data.msg.Contact
import java.util.*


object Model {


    class Result{//must match the result class in the server
          var success = false
          var data = ""
          var errMsg = ""
          var debugMsg = ""
          var stackTraceMsg = ""
     }

}