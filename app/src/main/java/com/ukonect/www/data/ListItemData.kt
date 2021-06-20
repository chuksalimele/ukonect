package com.ukonect.www.data

import android.graphics.drawable.Drawable

class ListItemData {

    var type : Int = -1
    var left_image_url: String = ""
    var left_image_placeholder: Drawable? = null
    var left_image_width: Int? = null
    var left_image_height: Int? = null

    var right_image_url: String = ""
    var right_image_placeholder: Drawable? = null
    var right_image_width: Int? = null
    var right_image_height: Int? = null

    var text=""
    var text_font_size : Int? = null

    var sub_text=""
    var sub_text_font_size : Int? = null

    var divider_full_length: Boolean = false
    var divider_show: Boolean? = true


}