package com.example.tracetogether.api

import android.content.Context
import com.example.tracetogether.util.Extensions.getLocalizedText

object ErrorCode {

    val APPLICATION_NOT_REGISTERED = "APPLICATION_NOT_REGISTERED"
    val AUTHORIZATION_FAILURE = "AUTHORIZATION_FAILURE"
    val CHALLENGE_HANDLING_CANCELED = "CHALLENGE_HANDLING_CANCELED"
    val ILLEGAL_ARGUMENT_EXCEPTION = "ILLEGAL_ARGUMENT_EXCEPTION"
    val LOGIN_ALREADY_IN_PROCESS = "LOGIN_ALREADY_IN_PROCESS"
    val LOGOUT_ALREADY_IN_PROCESS = "LOGOUT_ALREADY_IN_PROCESS"
    val MINIMUM_SERVER = "MINIMUM_SERVER"
    val MISSING_CHALLENGE_HANDLER = "MISSING_CHALLENGE_HANDLER"
    val REQUEST_TIMEOUT = "REQUEST_TIMEOUT"
    val SERVER_ERROR = "SERVER_ERROR"
    val UNEXPECTED_ERROR = "UNEXPECTED_ERROR"
    val ADAPTER_DOES_NOT_EXIST = "ADAPTER_DOES_NOT_EXIST"
    val APPLICATION_DOES_NOT_EXIST = "APPLICATION_DOES_NOT_EXIST"

    fun getStringForErrorCode(context: Context, errorCode: String? = "") : String {
        return when(errorCode) {
            AUTHORIZATION_FAILURE -> "auth_error".getLocalizedText()
            REQUEST_TIMEOUT -> "timeout_error".getLocalizedText()
            SERVER_ERROR -> "unknown_error".getLocalizedText()
            UNEXPECTED_ERROR, CHALLENGE_HANDLING_CANCELED, LOGIN_ALREADY_IN_PROCESS, LOGOUT_ALREADY_IN_PROCESS  -> "unknown_error".getLocalizedText()
            else -> "unexpected_error".getLocalizedText()// APPLICATION_NOT_REGISTERED, ILLEGAL_ARGUMENT_EXCEPTION, MINIMUM_SERVER, MISSING_CHALLENGE_HANDLER, ADAPTER_DOES_NOT_EXIST
        }
    }
}