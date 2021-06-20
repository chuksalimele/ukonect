package com.ukonect.www.data

import java.util.*

abstract class Message{

    var message_id = ""//a universally  unique id that identifies the message
    var topic = "" //WAMP subscription topic! The server will set(or override) this even if the client set it when sending the message
    var user = User()// sender
    var text : String = "" //plain text message
    var from_user_id: String = ""// will help to retrieve the user object of the sender if a Ukonect member
    var to_user_id: String = ""// will help to retrieve the user object of the receiver if a Ukonect member
    var time: Long = 0L; // could be sent_time or received_time - determined by the app
    var received_time: Long = 0L;
    var sent_time: Long = 0L;
    var status: Int = UNKNOWN;
    var read_count = 0
    var modified_time: Long = 0L;
    var modified_count = 0//The view will check of this and render as appropriate
    var deleted = false//The view will check of this and render as appropriate

    var type:Int = -1



    companion object {

        val UNKNOWN = 0;
        val MSG_STATUS_NOT_SENT = 1;//not sent
        val MSG_STATUS_SENT = 2;// sent to the server but not to the recipient
        val MSG_STATUS_SEEN = 3;//recipient has seen the message sent to him
        val MSG_STATUS_READ = 4;//recipient has read the message sent to him

    }

}