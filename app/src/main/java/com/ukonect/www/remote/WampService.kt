package com.ukonect.www.remote

import com.ukonect.www.remote.update.*
import com.ukonect.www.util.Config
import com.laurencegarmstrong.kwamp.client.core.ClientImpl
import com.laurencegarmstrong.kwamp.core.Uri
import com.laurencegarmstrong.kwamp.core.WAMP_DEFAULT
import com.laurencegarmstrong.kwamp.core.WAMP_JSON
import com.laurencegarmstrong.kwamp.core.WAMP_MSG_PACK
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.features.websocket.*
import io.ktor.http.cio.websocket.Frame
import io.ktor.http.cio.websocket.readText
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class WampService {

    private var wampClient: ClientImpl? = null;
    private var caller: WampCaller? = null;
    private var callee: WampCallee? = null;
    private var publisher: WampPublisher? = null;
    private var subscriber: WampSubscriber? = null;


    private fun websocketClient() = HttpClient(OkHttp).config { install(WebSockets) }

    fun start():WampService {

        wampClient = createWebsocketWampClient()

        subscriber = WampSubscriber(wampClient)
        publisher = WampPublisher(wampClient)
        callee = WampCallee(wampClient)
        caller = WampCaller(wampClient)


        subscriber?.isJoin = true;

        subscriber?.users?.let {
            subscriber?.subscribeChat(it)
            subscriber?.subscribePost(it)
            subscriber?.subscribeUserProfile(it)
        }

        subscriber?.group_names?.let {
            subscriber?.subscribeGroup(it)
        }

        subscriber?.locationUsers?.let { lusr ->
            lusr.forEach {
                subscriber?.subscribeLocation(it)
            }
        }

        callee?.registerCheckUserIsOnline()

        return this
    }

    private fun createWebsocketWampClient(): ClientImpl {
        val wampIncoming = Channel<ByteArray>()
        val wampOutgoing = Channel<ByteArray>()
        establishWebsocketConnection(wampIncoming, wampOutgoing)
        return ClientImpl(wampIncoming, wampOutgoing, Uri(Config.REALM))
    }

    private fun establishWebsocketConnection(
        wampIncoming: Channel<ByteArray>,
        wampOutgoing: Channel<ByteArray>,
        protocol: String = WAMP_DEFAULT
    ) {
        runBlocking {
            GlobalScope.launch {
                val client = websocketClient()

                client.ws(host = Config.REMORT_HOST , port = Config.REMORT_PORT , path = Config.REMORT_PATH, request = {
                    this.headers.append("Sec-WebSocket-Protocol","$WAMP_JSON, $WAMP_MSG_PACK")

                }) {
                    GlobalScope.launch {
                        wampOutgoing.consumeEach { message ->
                            send(Frame.Text(message.toString(Charsets.UTF_8)))
                        }

                    }

                    incoming.consumeEach { frame ->
                        if (frame is Frame.Text && protocol == WAMP_JSON) {
                            wampIncoming.send(frame.readText().toByteArray())
                        } else if (frame is Frame.Binary && protocol == WAMP_MSG_PACK) {
                            wampIncoming.send(frame.buffer.array())
                        }
                    }
                }
            }
        }
    }

    fun getCallee():WampCallee?{
        return callee
    }

    fun getCaller():WampCaller?{
        return caller
    }

    fun getPublisher():WampPublisher?{
        return publisher
    }

    fun getSubscriber():WampSubscriber?{
        return subscriber
    }

    fun setChatUpdater(intf: IChatUpdate){
        subscriber?.chatUpdater  = intf;
    }

    fun setGroupUpdater(intf: IGroupUpdate){
        subscriber?.groupUpdater  = intf;
    }

    fun setPostUpdater(intf: IPostUpdate){
        subscriber?.postUpdater  = intf;
    }

    fun setLocationUpdater(intf: ILocationUpdate){
        subscriber?.locationUpdater  = intf;
    }

    fun setUserProfileUpdater(intf: IUserProfileUpdate){
        subscriber?.userProfileUpdater  = intf;
    }

    fun setUserOnlineStatusUpdater(intf: IUserOnlineStatusUpdate){
        subscriber?.userOnlineStatusUpdater  = intf;
    }

}