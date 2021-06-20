package com.ukonect.www.data.msg

import com.ukonect.www.data.*;

open class PostMessage : Message() {

    var media_url: String? = ""//e.g picture, audio, video
    var media_base64 = ""//Note: We will not store  this feild in the mongo document

}