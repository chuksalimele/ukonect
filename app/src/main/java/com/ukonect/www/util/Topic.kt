package com.ukonect.www.util

import com.laurencegarmstrong.kwamp.core.Uri
import com.ukonect.www.data.AppUser

object Topic {

    val BACKEND_FETCH_BY_TOPICS = "com.ukonectinfo.backend.fetch.by.topics"

    private val chat = "chat"
    private val group = "group"
    private val post = "group"
    private val message = "sent"
    private val sent = "sent"
    private val seen = "seen"
    private val read = "read"
    private val location = "location"
    private val change = "change"
    private val enter = "enter"
    private val exit = "exit"
    private val boundary = "boundary"
    private val name = "name"
    private val member = "member"
    private val added = "added"
    private val removed = "removed"
    private val left = "left"
    private val comment = "comment"
    private val user = "user"
    private val profile = "profile"
    private val update = "update"
    private val modified = "modified"
    private val deleted = "deleted"
    private val edited = "edited"
    private val online = "online"
    private val status = "status"


    fun chatMessageUri(appUser: AppUser, other:String): Uri {

        val suffix = if (other > appUser.user_id)
            "$other.and.${appUser.user_id}"
        else
            "${appUser.user_id}.and.$other"

        return Uri("${Config.WAMP_URI_PREFIX}.$chat.$message.$suffix")
    }

    fun chatSentUri(appUser: AppUser, other:String): Uri{//we may not use this, instead we will set acknowledgement to true in PublishOption

        val suffix = if (other > appUser.user_id)
            "$other.and.${appUser.user_id}"
        else
            "${appUser.user_id}.and.$other"

        return Uri("${Config.WAMP_URI_PREFIX}.$chat.$sent.$suffix")
    }

    fun chatSeenUri(appUser: AppUser, other:String): Uri {

        val suffix = if (other > appUser.user_id)
            "$other.and.${appUser.user_id}"
        else
            "${appUser.user_id}.and.$other"

        return Uri("${Config.WAMP_URI_PREFIX}.$chat.$seen.$suffix")
    }

    fun chatReadUri(appUser: AppUser, other:String): Uri{

        val suffix = if (other > appUser.user_id)
            "$other.and.${appUser.user_id}"
        else
            "${appUser.user_id}.and.$other"

        return Uri("${Config.WAMP_URI_PREFIX}.$chat.$read.$suffix")
    }

    fun chatModifiedUri(appUser: AppUser, other:String): Uri{

        val suffix = if (other > appUser.user_id)
            "$other.and.${appUser.user_id}"
        else
            "${appUser.user_id}.and.$other"

        return Uri("${Config.WAMP_URI_PREFIX}.$chat.$modified.$suffix")
    }

    fun chatDeletedUri(appUser: AppUser, other:String): Uri{

        val suffix = if (other > appUser.user_id)
            "$other.and.${appUser.user_id}"
        else
            "${appUser.user_id}.and.$other"

        return Uri("${Config.WAMP_URI_PREFIX}.$chat.$deleted.$suffix")
    }

    fun groupEditedUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$edited.$group_name")
    }

    fun groupMemberAddedUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$member.$added.$group_name")
    }

    fun groupMemberRemovedUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$member.$removed.$group_name")
    }

    fun groupMemberLeftUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$member.$left.$group_name")
    }

    fun groupChatMessageUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$chat.$message.$group_name")
    }

    fun groupChatSentUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$chat.$sent.$group_name")
    }

    fun groupChatSeenUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$chat.$seen.$group_name")
    }

    fun groupChatReadUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$chat.$read.$group_name")
    }

    fun groupChatModifiedUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$chat.$modified.$group_name")
    }

    fun groupChatDeletedUri(group_name:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$group.$chat.$deleted.$group_name")
    }

    fun postMessageUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$message.$user_id")
    }

    fun postMessageCommentUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$message.$comment.$user_id")
    }

    fun postSentUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$sent.$user_id")
    }

    fun postSeenUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$seen.$user_id")
    }

    fun postReadUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$read.$user_id")
    }

    fun postModifiedUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$modified.$user_id")
    }

    fun postDeletedUri(user_id: String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$post.$deleted.$user_id")
    }

    fun locationChangeUri(user_id:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$location.$change.$user_id")
    }

    fun userProfileUpdate(user_id:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$user.$profile.$update.$user_id")
    }

    fun userOnlineStatus(user_id:String): Uri{
        return Uri("${Config.WAMP_URI_PREFIX}.$user.$online.$status.$user_id")
    }
}