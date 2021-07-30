package com.example.tracetogether

import android.content.Context
import com.example.tracetogether.model.GuidanceTile
import com.example.tracetogether.more.MoreLink
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Type
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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

    private const val LOCALIZATION_TIME = "LOCALIZATION_TIME"
    private const val LAST_SYSTEM_LANG = "LAST_SYS_LANG"
    private const val FEATURE_MHR = "FEATURE_MHR"
    private const val URL_DATA = "URL_DATA"
    private const val LAST_FETCHED_URL_DATA_TIMESTAMP = "LAST_FETCHED_URL_DATA_TIMESTAMP"

    private const val LAST_WHATS_NEW_SEEN = "LAST_WHATS_NEW_SEEN"

    private const val PRIVACY_POLICY_VERSION_ACCEPTED = "PRIVACY_POLICY_VERSION_SEEN"
    private const val PRIVACY_POLICY_FETCH_TIME = "PRIVACY_POLICY_FETCH_TIME"

    private const val PAUSED_SCHEDULED = "PAUSED_SCHEDULED"
    private const val PAUSE_START_TIME = "PAUSE_START_TIME"
    private const val PAUSE_END_TIME = "PAUSE_END_TIME"

    private const val GUIDANCE_TILE = "GUIDANCE_TILE"
    private const val MORE_LINKS = "MORE_LINKS"

    private val DEFAULT_PAUSE_START_TIME = LocalTime.of(0, 0)
    private val DEFAULT_PAUSE_END_TIME = LocalTime.of(8, 0)

    private val moreLinksArrayType: Type = Types.newParameterizedType(
            List::class.java,
            MoreLink::class.java
    )

    private val moreLinksArrayMoshiAdapter = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter<List<MoreLink>>(moreLinksArrayType)

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

    fun putLastPurgeTime(context: Context, lastPurgeTime: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(LAST_PURGE_TIME, lastPurgeTime).apply()
    }

    fun getLastPurgeTime(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getLong(LAST_PURGE_TIME, 0)
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
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit().putLong(LAST_FETCHED_URL_DATA_TIMESTAMP, System.currentTimeMillis()).apply()
    }

    fun getUrlData(context: Context): String? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(
                URL_DATA, ""
            )
    }

    fun getLastFetchedUrlDataTimestamp(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getLong(LAST_FETCHED_URL_DATA_TIMESTAMP, 0)
    }

    fun userHasSeenWhatsNew(context: Context): Boolean {
        val lastSeenAppVersion = context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(
                LAST_WHATS_NEW_SEEN, ""
            )
        return BuildConfig.VERSION_NAME == lastSeenAppVersion
    }

    fun setUserHasSeenWhatsNew(context: Context, userHasSeen: Boolean) {
        if (userHasSeen) {
            context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit()
                .putString(LAST_WHATS_NEW_SEEN, BuildConfig.VERSION_NAME)
                .apply()
        } else {
            context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .edit()
                .remove(LAST_WHATS_NEW_SEEN)
                .apply()
        }
    }

    fun getPrivacyPolicyAccepted(context: Context): Int {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getInt(PRIVACY_POLICY_VERSION_ACCEPTED, 1)
    }

    fun setPrivacyPolicyAccepted(context: Context, privacyVersion: Int) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit()
            .putInt(PRIVACY_POLICY_VERSION_ACCEPTED, privacyVersion)
            .apply()
    }

    fun getPrivacyPolicyFetchTime(context: Context): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getLong(PRIVACY_POLICY_FETCH_TIME, 0)
    }

    fun setPrivacyPolicyFetchTime(context: Context, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(PRIVACY_POLICY_FETCH_TIME, time).apply()
    }

    fun getStatsFetchTime(context: Context, key: String): Long {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getLong(key, 0)
    }

    fun setStatsFetchTime(context: Context, key: String, time: Long) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putLong(key, time).apply()
    }

    fun getPauseScheduled(context: Context): Boolean {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getBoolean(PAUSED_SCHEDULED, false)
    }

    fun setPauseScheduled(context: Context, pauseScheduled: Boolean) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putBoolean(PAUSED_SCHEDULED, pauseScheduled).apply()
    }

    fun getPauseStartTime(context: Context): LocalTime {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(PAUSE_START_TIME, null)
            ?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }
            ?: DEFAULT_PAUSE_START_TIME
    }

    fun setPauseStartTime(context: Context, pauseStartTime: LocalTime) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(PAUSE_START_TIME, DateTimeFormatter.ISO_LOCAL_TIME.format(pauseStartTime)).apply()
    }

    fun getPauseEndTime(context: Context): LocalTime {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .getString(PAUSE_END_TIME, null)
            ?.let { LocalTime.parse(it, DateTimeFormatter.ISO_LOCAL_TIME) }
            ?: DEFAULT_PAUSE_END_TIME
    }

    fun setPauseEndTime(context: Context, pauseEndTime: LocalTime) {
        context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
            .edit().putString(PAUSE_END_TIME, DateTimeFormatter.ISO_LOCAL_TIME.format(pauseEndTime)).apply()
    }

    fun getGuidanceTile(context: Context): GuidanceTile? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(GUIDANCE_TILE, null)
                ?.let {
                    try {
                        GuidanceTile.parse(JSONObject(it))
                    } catch (e: Exception) {
                        null
                    }
                }
    }

    fun setGuidanceTile(context: Context, guidanceTile: GuidanceTile?) {
        val sharedPreferences = context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
        if (guidanceTile != null) {
            sharedPreferences.edit().putString(GUIDANCE_TILE, guidanceTile.toString()).apply()
        } else {
            sharedPreferences.edit().remove(GUIDANCE_TILE).apply()
        }
    }

    fun getMoreLinks(context: Context): List<MoreLink>? {
        return context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
                .getString(MORE_LINKS, null)
                ?.let {
                    try {
                        moreLinksArrayMoshiAdapter.fromJson(it)
                    } catch (e: Exception) {
                        null
                    }
                }
    }

    fun setMoreLinks(context: Context, moreLinks: List<MoreLink>?) {
        val sharedPreferences = context.getSharedPreferences(PREF_ID, Context.MODE_PRIVATE)
        if (moreLinks != null) {
            val moreLinksArrayString = moreLinksArrayMoshiAdapter.toJson(moreLinks)
            sharedPreferences.edit().putString(MORE_LINKS, moreLinksArrayString).apply()
        } else {
            sharedPreferences.edit().remove(MORE_LINKS).apply()
        }
    }
}
