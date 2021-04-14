package com.example.tracetogether.util

import android.content.Context
import android.text.TextUtils
import com.example.tracetogether.Preference
import com.example.tracetogether.api.Request
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.lang.Exception
import java.util.*


class LocalizationHandler : CoroutineScope by MainScope() {
    private var json: JSONObject? = null
    private var fileName: String = "strings-" + Locale.getDefault().language + ".json"

    companion object {
        @Volatile
        private var INSTANCE: LocalizationHandler? = null

        fun getInstance(): LocalizationHandler {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = LocalizationHandler()
                INSTANCE = instance
                return instance
            }
        }

    }

    fun loadJSONFromAsset(context: Context?) {
        fileName = "strings-" + Locale.getDefault().language + ".json"

        val file = File(context?.filesDir, fileName)

        if (!file.exists()) {
            writeJSON(file, context)
        }
        readData(context)

    }


    private fun writeJSON(file: File, context: Context?) {
        try {
            var stream: InputStream? = try {
                context?.assets?.open(fileName)
            } catch (e: FileNotFoundException) {
                context?.assets?.open("strings-en.json")
            }
            var size: Int? = stream?.available()

            val buffer = ByteArray(size ?: 0)
            stream?.read(buffer)
            stream?.close()
            val jsonString = String(buffer, Charsets.UTF_8)
            file.writeText(jsonString)

            json = JSONObject(jsonString)
        } catch (e: Exception) {
        }
    }


    private fun readData(context: Context?) {
        try {
            val f = File(context?.filesDir, fileName)
            //check whether file exists
            val stream = FileInputStream(f)
            val size = stream.available()
            val buffer = ByteArray(size)
            stream.read(buffer)
            stream.close()

            if (buffer.isNotEmpty()) {
                try {
                    json = JSONObject(String(buffer))
                } catch (e: JSONException) {
                    writeJSON(f, context)
                }
            }

        } catch (e: Exception) {
        }
    }

    fun getUpdatedData(context: Context?) {
        if (context != null && shouldFetchData(context)) {
            getUpdatedCopies(context)
        }
    }


    fun getString(key: String): String? {
        return json?.optString(key, "")
    }

    private fun shouldFetchData(context: Context): Boolean {
        val lastFetchTime = Preference.getLocalizationFetchTime(context) / 1000
        val currentTime = System.currentTimeMillis() / 1000
        var hours = (currentTime - lastFetchTime) / 3600
        return hours > 24
    }

    private fun getUpdatedCopies(context: Context?) = launch {
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language

        val authResponse =
            Request.runRequest(
                "/adapters/applicationDataAdapter/getContent",
                Request.GET,
                queryParams = queryParams
            )

        if (authResponse.isSuccess() && !TextUtils.isEmpty(authResponse?.data?.toString())) {
            val file = File(context?.filesDir, fileName)
            file.writeText(authResponse?.data?.toString() ?: "")
            if (context != null) {
                Preference.putLocalizationFetchTime(context, System.currentTimeMillis())
            }
        }

    }

    fun getTextForNotification(context: Context?, key: String): String? {
        if (json == null) {
            loadJSONFromAsset(context)
        }
        return getString(key)
    }

}