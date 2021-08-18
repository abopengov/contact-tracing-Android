package com.example.tracetogether.network

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

class DataJsonAdapter : JsonAdapter<Date?>() {
    override fun fromJson(reader: JsonReader): Date? {
        if (reader.peek() === JsonReader.Token.NULL) {
            return reader.nextNull()
        }
        val string: String = reader.nextString()
        return parseDate(string)
    }

    override fun toJson(writer: JsonWriter, value: Date?) {
        if (value == null) {
            writer.nullValue()
        } else {
            val string: String = formatDate(value)
            writer.value(string)
        }
    }

    private fun parseDate(value: String): Date? {
        val parser = SimpleDateFormat(DATE_FORMAT, Locale.CANADA)
        return parser.parse(value)
    }

    private fun formatDate(date: Date): String {
        val df = SimpleDateFormat(DATE_FORMAT, Locale.CANADA)
        return df.format(date)
    }

    companion object {
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }
}