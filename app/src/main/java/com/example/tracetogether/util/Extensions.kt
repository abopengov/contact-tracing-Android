package com.example.tracetogether.util

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.tracetogether.Preference
import com.example.tracetogether.status.persistence.StatusRecord
import com.example.tracetogether.streetpass.persistence.StreetPassRecord
import org.json.JSONObject
import java.text.NumberFormat
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*


object Extensions {

    fun EditText.setLocalizedStringHint(key: String) {
        this.hint = LocalizationHandler.getInstance().getString(key)
    }

    fun TextView.setLocalizedString(key: String) {
        this.text = LocalizationHandler.getInstance().getString(key)
    }

    fun TextView.underline() {
        paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    fun TextView.addHyperlink(textToFind: String, callback: () -> Unit) {
        val spannableString = SpannableString(text)
        val start = text.indexOf(textToFind)

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                callback()
            }

            override fun updateDrawState(textPaint: TextPaint) {
                super.updateDrawState(textPaint)
                textPaint.isUnderlineText = true
                textPaint.color = textPaint.linkColor
            }
        }

        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            start,
            start + textToFind.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )
        spannableString.setSpan(
            clickableSpan,
            start,
            start + textToFind.length,
            Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
        text = spannableString
        movementMethod = LinkMovementMethod.getInstance();
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

    fun String.getUrl(context: Context, fallback: String = ""): String {
        return Preference.getUrlData(context)?.let { urlData ->
            return if (urlData.isNotEmpty()) {
                JSONObject(urlData).optString(this, fallback)
            } else {
                fallback
            }
        } ?: fallback
    }

    fun Fragment.statusBarHeight(): Int {
        val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else 0
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

    fun Int.withCommas(): String = NumberFormat.getNumberInstance(Locale.CANADA).format(this)

    fun Long.withCommas(): String = NumberFormat.getNumberInstance(Locale.CANADA).format(this)

    fun Float.withCommas(): String = NumberFormat.getNumberInstance(Locale.CANADA).format(this)

    fun LocalTime.nextDateTime(now: LocalDateTime = LocalDateTime.now()): LocalDateTime {
        val date = now.toLocalDate().atTime(this)
        return if (date.isBefore(now)) {
            now.toLocalDate().plusDays(1).atTime(this)
        } else {
            date
        }
    }

    fun LocalTime.withinTimePeriod(startTime: LocalTime?, endTime: LocalTime?): Boolean {
        if (startTime == null || endTime == null) {
            return false
        }

        return if (startTime < endTime) {
            startTime <= this && endTime > this
        } else {
            startTime <= this || endTime > this
        }
    }

    fun LocalTime.formatTime(): String {
        val isPM = hour >= 12
        return String.format(
                "%02d:%02d %s",
                if (hour == 12 || hour == 0) 12 else hour % 12,
                minute,
                if (isPM) "PM" else "AM"
        )
    }

    fun <T> List<T>.splitBy(predicate: (T) -> Boolean): List<List<T>> {
        val list = mutableListOf<List<T>>()
        var subList = mutableListOf<T>()
        this.forEach { num ->
            if (predicate(num)) {
                list.add(subList)
                subList = mutableListOf()
            } else {
                subList.add(num)
            }
        }
        list.add(subList)

        return list
    }
}