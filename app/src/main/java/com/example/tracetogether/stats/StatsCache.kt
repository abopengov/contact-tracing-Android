package com.example.tracetogether.stats

import android.content.Context
import com.example.tracetogether.Preference
import com.example.tracetogether.Utils
import com.example.tracetogether.network.DataJsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import java.io.File
import java.lang.reflect.Type
import java.util.*

class StatsCache<T>(
        private val context: Context,
        typeParameterClass: Class<T>,
        private val fileName: String
) {
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, DataJsonAdapter().nullSafe())
            .build()

    private val type: Type = Types.newParameterizedType(
            List::class.java,
            typeParameterClass
    )

    private val adapter = moshi.adapter<List<T>>(type)

    private val file = File(context.cacheDir, fileName)

    fun set(statsData: List<T>) {
        file.writeText(adapter.toJson(statsData))
        Preference.setStatsFetchTime(context, fileName, System.currentTimeMillis())
    }

    fun get(): List<T>? {
        return adapter.fromJson(file.readText())
    }

    fun isExpired(): Boolean {
        val time = Preference.getStatsFetchTime(context, fileName)
        return !file.exists() || time == 0L ||
                System.currentTimeMillis() - time > Utils.getStatsCacheDuration()
    }
}