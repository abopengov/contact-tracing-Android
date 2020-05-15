package com.example.tracetogether.api

import com.example.tracetogether.TracerApp
import com.example.tracetogether.api.auth.SmsCodeChallengeHandler
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.logging.WFLog
import com.worklight.wlclient.api.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import org.json.JSONObject
import java.net.URI
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object Request : CoroutineScope by MainScope() {

    init {
        WLClient.getInstance().registerChallengeHandler(SmsCodeChallengeHandler())
    }

    const val GET = "GET"
    const val POST = "POST"
    const val PUT = "PUT"
    const val DELETE = "DELETE"
    const val TRACE = "TRACE"
    const val HEAD = "HEAD"
    const val OPTIONS = "OPTIONS"

    suspend fun runRequest(
        path: String,
        method: String,
        timeout: Int = 3000,
        scope: String? = null,
        data: JSONObject? = null,
        queryParams: HashMap<String, String>? = null
    ): Response =
        suspendCoroutine { cont ->
            val request = WLResourceRequest(URI(path), method, timeout, scope)

            CentralLog.d("Request", "method=${request.method} - url=${request.url}")

            if (queryParams != null) {
                request.setQueryParameters(queryParams)
            }

            val listener = object : WLResponseListener {
                override fun onSuccess(response: WLResponse?) {
                    CentralLog.d("Request", "Request.onSuccess url=${request.url} - response=${response?.responseJSON}")
                    cont.resume(
                        Response(
                            response?.status,
                            response?.responseText,
                            response?.responseJSON
                        )
                    )
                }

                override fun onFailure(response: WLFailResponse?) {
                    CentralLog.d("Request", "Request.onFailure url=${request.url} -  response=$response")
                    WFLog.logError("Request.onFailure request=${request} -  response=$response")
                    response?.errorMsg?.let {
                        CentralLog.d(
                            "Request",
                            "${response.errorStatusCode} - ${response.errorMsg}"
                        )
                    }
                    cont.resume(
                        Response(
                            response?.status,
                            response?.responseText,
                            response?.responseJSON,
                            ErrorCode.getStringForErrorCode(
                                TracerApp.AppContext,
                                response?.errorStatusCode ?: ""
                            ),
                            response?.errorStatusCode ?: ""
                        )
                    )
                }
            }

            if (data == null) {
                request.send(listener)
            } else {
                request.send(data, listener)
            }
        }
}