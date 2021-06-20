package com.ukonect.www.remote

import com.laurencegarmstrong.kwamp.client.core.ClientImpl
import com.laurencegarmstrong.kwamp.client.core.call.CallResult
import com.laurencegarmstrong.kwamp.core.messages.Dict
import com.ukonect.www.util.Procedure
import io.reactivex.Observable


class WampCallee internal constructor(private val wampClient: ClientImpl?) {

    private fun checkIsOnline(
        arguments: List<Any?>?, argumentsKw: Dict?
    ): CallResult {
        return  CallResult(listOf(true))// just return true. That fact that this method was called at all means I am online
    }


    fun registerCheckUserIsOnline(): Observable<Boolean>? {

        return Observable.create { emitter ->

            try {

                val registrationHandle =
                    wampClient?.register(Procedure.checkIsUserOnlineUri(), this::checkIsOnline)


                emitter.onNext(true)
            } catch (ex: Throwable) {
                emitter.onError(ex);
            } finally {
                emitter.onComplete()
            }
        }
    }
}