package com.example.tracetogether

import android.content.Context
import android.content.SharedPreferences

object Preference {
    private const val PREF_ID = "Tracer_pref"
    private const val IS_ONBOARDED = "IS_ONBOARDED"
    private const val PHONE_NUMBER = "PHONE_NUMBER"
    private const val CHECK_POINT = "CHECK_POINT"
    private const val UUID = "UUID"
    private const val UUID_RETRY_ATTEMPTS = "UUID_RETRY_ATTEMPTS"

    private const val NEXT_FETCH_TIME = "NEXT_FETCH_TIME"
    private const val EXPIRY_TIME = "EXPIRY_TIME"
    private const val LAST_FETCH_TIME = "LAST_FETCH_TIME"

    private const val LAST_PURGE_TIME = "LAST_PURGE_TIME"

    private const val ANNOUNCEMENT = "ANNOUNCEMENT"
    private const val LOCALIZATION_TIME = "LOCALIZATION_TIME"
    private const val LAST_SYSTEM_LANG = "LAST_SYS_LANG"
    private const val FEATURE_MHR = "FEATURE_MHR"
    private const val URL_DATA = "URL_DATA"

    fun getUUIDRetryAttempts(context: Context): Int {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getInt(UUID_RETRY_ATTEMPTS, 0)
    }

    fun putUUIDRetryAttempts(context: Context, value: Int) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putInt(UUID_RETRY_ATTEMPTS, value).apply()
    }

    fun putUUID(context: Context, value: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(UUID, value).apply()
    }

    fun getUUID(context: Context?): String {
        return context?.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)?.getString(UUID, "")
                ?: ""
    }

    fun putIsOnBoarded(context: Context, value: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putBoolean(IS_ONBOARDED, value).apply()
    }

    fun isOnBoarded(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getBoolean(IS_ONBOARDED, false)
    }

    fun putPhoneNumber(context: Context, value: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(PHONE_NUMBER, value).apply()
    }

    fun getPhoneNumber(context: Context): String {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(PHONE_NUMBER, "") ?: ""
    }

    fun putCheckpoint(context: Context, value: Int) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putInt(CHECK_POINT, value).apply()
    }

    fun getCheckpoint(context: Context): Int {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getInt(CHECK_POINT, 0)
    }

    fun getLastFetchTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(
                        LAST_FETCH_TIME, 0
                )
    }

    fun putLastFetchTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(LAST_FETCH_TIME, time).apply()
    }

    fun putNextFetchTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(NEXT_FETCH_TIME, time).apply()
    }

    fun getNextFetchTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(
                        NEXT_FETCH_TIME, 0
                )
    }

    fun putExpiryTimeInMillis(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(EXPIRY_TIME, time).apply()
    }

    fun getExpiryTimeInMillis(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(
                        EXPIRY_TIME, 0
                )
    }

    fun putAnnouncement(context: Context, announcement: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(ANNOUNCEMENT, announcement).apply()
    }

    fun getAnnouncement(context: Context): String {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(ANNOUNCEMENT, "") ?: ""
    }

    fun putLastPurgeTime(context: Context, lastPurgeTime: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(LAST_PURGE_TIME, lastPurgeTime).apply()
    }

    fun getLastPurgeTime(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(LAST_PURGE_TIME, 0)
    }

    fun registerListener(
            context: Context,
            listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(
            context: Context,
            listener: SharedPreferences.OnSharedPreferenceChangeListener
    ) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .unregisterOnSharedPreferenceChangeListener(listener)
    }

    fun putLocalizationFetchTime(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(LOCALIZATION_TIME, time).apply()
    }

    fun getLocalizationFetchTime(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(
                        LOCALIZATION_TIME, 0
                )
    }

    fun putSystemLang(context: Context, lang: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(LAST_SYSTEM_LANG, lang).apply()
    }

    fun getSystemLang(context: Context): String? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(
                        LAST_SYSTEM_LANG, ""
                )
    }

    fun setFeatureMHR(context: Context, key: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putBoolean(FEATURE_MHR, key).apply()
    }

    fun shouldShowFeatureMHR(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getBoolean(
                        FEATURE_MHR, false
                )
    }

    fun putUrlData(context: Context, lang: String) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putString(URL_DATA, lang).apply()
    }

    fun getUrlData(context: Context): String? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(
                        URL_DATA, ""
                )
    }
}
