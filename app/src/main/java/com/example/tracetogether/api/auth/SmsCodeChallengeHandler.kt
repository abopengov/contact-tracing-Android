package com.example.tracetogether.api.auth

import android.content.*
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.tracetogether.logging.CentralLog
import com.worklight.wlclient.api.*
import com.worklight.wlclient.api.challengehandler.SecurityCheckChallengeHandler
import org.json.JSONException
import org.json.JSONObject
import java.lang.NullPointerException

class SmsCodeChallengeHandler : SecurityCheckChallengeHandler("smsOtpService") {

    companion object {
        const val ACTION_VERIFY_SMS_CODE_REQUIRED = "ACTION_VERIFY_SMS_CODE_REQUIRED"
        const val ACTION_VERIFY_SMS_CODE = "ACTION_VERIFY_SMS_CODE"
        const val ACTION_VERIFY_SMS_CODE_SUCCESS = "ACTION_VERIFY_SMS_CODE_SUCCESS"
        const val ACTION_VERIFY_SMS_CODE_FAIL = "ACTION_VERIFY_SMS_CODE_FAIL"
        const val ACTION_CANCEL_CHALLENGE = "ACTION_CANCEL_CHALLENGE"
        const val ACTION_CHALLENGE_CANCELLED = "ACTION_CHALLENGE_CANCELLED"
        const val EXTRA_SKIP_CANCELLED_BROADCAST = "EXTRA_SKIP_CANCELLED_BROADCAST"
    }

    private var errorMsg = ""
    private val context: Context = WLClient.getInstance().context
    private var isChallenged = false
    private val broadcastManager: LocalBroadcastManager

    override fun handleChallenge(jsonObject: JSONObject) {
        CentralLog.d("smsOTP", "Challenge Received - $jsonObject")
        isChallenged = true
        try {
            errorMsg = if (jsonObject.isNull("errorMsg")) {
                ""
            } else {
                jsonObject.getString("errorMsg")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val intent = Intent()
        intent.action = ACTION_VERIFY_SMS_CODE_REQUIRED
        intent.putExtra("errorMsg", errorMsg)
        broadcastManager.sendBroadcast(intent)
    }

    override fun handleFailure(error: JSONObject) {
        super.handleFailure(error)
        CentralLog.d("smsOTP", "handleFailure - $error")
        isChallenged = false
        if (error.isNull("failure")) {
            errorMsg = "Failed to veify . Please try again later."
        } else {
            try {
                errorMsg = error.getString("failure")
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
        val intent = Intent()
        intent.action = ACTION_VERIFY_SMS_CODE_FAIL
        intent.putExtra("errorMsg", errorMsg)
        broadcastManager.sendBroadcast(intent)
    }

    override fun handleSuccess(identity: JSONObject) {
        super.handleSuccess(identity)
        isChallenged = false

        val intent = Intent()
        intent.action = ACTION_VERIFY_SMS_CODE_SUCCESS
        broadcastManager.sendBroadcast(intent)
        CentralLog.d("smsOTP", "handleSuccess")
    }

    fun verifySmsCode(otp: String?) {
        val answer = JSONObject()
        answer.put("code", otp)
        submitChallengeAnswer(answer)
    }

    fun cancelChallenge(skipCancelledBroadcast : Boolean = false) {
        try{
            cancel()

            if (!skipCancelledBroadcast) {
                val intent = Intent()
                intent.action = ACTION_CHALLENGE_CANCELLED
                broadcastManager.sendBroadcast(intent)
            }
            CentralLog.d("smsOTP", "canceledChallenge")
        }
        catch(e: NullPointerException){
            CentralLog.d("smsOTP", "no challenge to cancel")
        }
    }

    init {
        broadcastManager = LocalBroadcastManager.getInstance(context)
        //Receive "verify sms code" requests
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                try {
                    verifySmsCode(intent.getStringExtra("otp"))
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }, IntentFilter(ACTION_VERIFY_SMS_CODE))

        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                cancelChallenge(intent.getBooleanExtra(EXTRA_SKIP_CANCELLED_BROADCAST, false))
            }
        }, IntentFilter(ACTION_CANCEL_CHALLENGE))
    }
}