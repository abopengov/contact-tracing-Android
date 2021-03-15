package com.example.tracetogether.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.example.tracetogether.Preference
import com.example.tracetogether.status.persistence.StatusRecord
import com.example.tracetogether.streetpass.persistence.StreetPassRecord
import org.json.JSONObject

object Extensions {

    fun TextView.setLocalizedString(key: String) {
        this.text = LocalizationHandler.getInstance().getString(key)
    }

    fun String.getLocalizedText(): String {
        return LocalizationHandler.getInstance().getString(this) ?: ""
    }

    fun View.hide() {
        this?.visibility = View.GONE
    }

    fun View.show() {
        this?.visibility = View.VISIBLE
    }

    fun String.getUrl(context: Context): String? {
        return Preference.getUrlData(context)?.let { urlData ->
            return if (urlData.isNotEmpty()) {
                JSONObject(urlData).optString(this)
            } else {
                null
            }
        }
    }

    fun StreetPassRecord.toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("v", this.v)
        jsonObject.put("msg", this.msg)
        jsonObject.put("org", this.org)
        jsonObject.put("modelP", this.modelP)
        jsonObject.put("modelC", this.modelC)
        jsonObject.put("rssi", this.rssi)
        jsonObject.put("txPower", this.txPower)
        jsonObject.put("timestamp", this.timestamp)
        return jsonObject
    }

    fun StatusRecord.toJSONObject(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("msg", this.msg)
        jsonObject.put("timestamp", this.timestamp)
        return jsonObject
    }
}