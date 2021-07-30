package com.example.tracetogether.logging

import com.worklight.common.Logger
import org.json.JSONObject


class WFLog {

    companion object {

        private val TAG = "*APP NAME*"

        fun logError(message: String) {
            Logger.getInstance(TAG).error(message)
            Logger.send()
        }

        fun logErrorWithException(message: String, e: Throwable?) {
            Logger.getInstance(TAG).error(message,JSONObject(), e)
            Logger.send()
        }

        fun logFatalWithException(message: String, e: Throwable?) {
            Logger.getInstance(TAG).fatal(message, JSONObject(), e)
            Logger.send()
        }

        fun logDebug(message: String) {
            Logger.getInstance(TAG).debug(message)
            Logger.send()
        }

        fun logInfo(message: String) {
            Logger.getInstance(TAG).error(message)
            Logger.send()
        }

        fun log(message: String) {
            Logger.getInstance(TAG).log(message)
            Logger.send()
        }


    }

}
