package com.ukonect.www.util


object Config {
    //val REMORT_HOST = "ukonectinfo.com"
    //val REMORT_HOST = "77.81.247.35"//vps ip
    val REMORT_HOST = "192.168.43.174"
    val REMORT_PORT = 8080
    val REMORT_PATH = "/"
    val WAMP_URI_PREFIX = "com.ukonectinfo"//reverse domain but without the com.ukonect.www
    val WAMP_ADDRESS = "ws://"+REMORT_HOST+REMORT_PORT
    val REALM = "realm1"
}