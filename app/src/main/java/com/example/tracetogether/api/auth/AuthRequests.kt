package com.example.tracetogether.api.auth

import com.example.tracetogether.TracerApp
import com.example.tracetogether.api.ErrorCode
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.logging.WFLog
import com.worklight.wlclient.api.*
import com.worklight.wlclient.auth.AccessToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object AuthRequests : CoroutineScope by MainScope() {

    init {
        WLClient.getInstance().registerChallengeHandler(SmsCodeChallengeHandler())
    }

    suspend fun obtainAccessToken(scope: String = "smsOTP"): AuthResponse =
            suspendCoroutine { cont ->
                WLAuthorizationManager.getInstance().obtainAccessToken(scope, object :
                        WLAccessTokenListener {

                    override fun onSuccess(accessToken: AccessToken) {
                        cont.resume(AuthResponse(accessToken))
                    }

                    override fun onFailure(response: WLFailResponse) {
                        WFLog.logError("Error retrieving Access Token")
                        response.errorMsg?.let {
                            CentralLog.d("Request", "${response.errorStatusCode} - ${response.errorMsg}")
                        }
                        cont.resume(AuthResponse(null, response.errorStatusCode, ErrorCode.getStringForErrorCode(TracerApp.AppContext, response.errorStatusCode ?: "")))
                    }
                })
            }
}