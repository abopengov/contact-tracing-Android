package com.example.tracetogether.api

import org.json.JSONObject

data class Response (val status: Int?, val text: String?, val data: JSONObject?, val error: String = "", val errorCode: String = "") {
    fun isSuccess(): Boolean {
        return error == "" && status != null
    }
}