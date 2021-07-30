package com.example.tracetogether.model

import org.json.JSONObject

data class GuidanceTile(
        val title: String,
        val text: String,
        val link: String?
) {
    companion object {
        fun parse(json: JSONObject): GuidanceTile {
            return GuidanceTile(
                    json.getString("title"),
                    json.getString("text"),
                    json.optString("link")
            )
        }
    }

    override fun toString(): String {
        return JSONObject()
                .put("title", title)
                .put("text", text)
                .put("link", link)
                .toString()
    }
}