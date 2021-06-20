package com.ukonect.www.exception

import com.ukonect.www.remote.model.Model

class UkonectException: Throwable {
    var errMsg = ""
    var debugMsg = ""
    var stackTraceMsg = ""
    constructor(message: String?, cause: Throwable?) : super(message, cause)

    constructor(response: Model.Result){
        this.errMsg = response.errMsg
        this.debugMsg = response.debugMsg
        this.stackTraceMsg = response.stackTraceMsg
    }

    fun isRemoteError():Boolean{
        return !errMsg.isNullOrEmpty() || !debugMsg.isNullOrEmpty() ||!stackTraceMsg.isNullOrEmpty()
    }

}