package com.example.tracetogether.api.auth

import com.worklight.wlclient.auth.AccessToken

data class AuthResponse (val accessToken: AccessToken?, val errorCode: String? = null, val error: String = "") {
    fun isSuccess(): Boolean {
        return errorCode == null
    }
}