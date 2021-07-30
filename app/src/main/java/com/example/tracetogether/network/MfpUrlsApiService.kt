package com.example.tracetogether.network

import android.content.Context
import android.text.TextUtils
import com.example.tracetogether.Preference
import com.example.tracetogether.api.Request
import com.example.tracetogether.model.GuidanceTile
import com.example.tracetogether.more.MoreLink
import com.example.tracetogether.util.AppConstants
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import org.json.JSONObject
import java.lang.Exception
import java.lang.reflect.Type
import java.util.*
import kotlin.collections.HashMap

class MfpUrlsApiService : UrlsApiService {
    private val moreLinksArrayType: Type = Types.newParameterizedType(
            List::class.java,
            MoreLink::class.java
    )

    private val moreLinksArrayMoshiAdapter = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build().adapter<List<MoreLink>>(moreLinksArrayType)

    override suspend fun retrieveAndCacheUrls(context: Context) {
        getUrls()?.let { jsonData ->
            persistUrlsFromJsonData(context, jsonData)
        }
    }

    override suspend fun getMoreLinks(context: Context): List<MoreLink> {
        return getUrls()?.let { jsonData ->
            persistUrlsFromJsonData(context, jsonData)
            jsonData.optJSONArray("moreLinks")?.let {
                moreLinksArrayMoshiAdapter.fromJson(it.toString())
            }
        }!!
    }

    override suspend fun getGuidanceTile(context: Context): GuidanceTile {
        return getUrls()?.let { jsonData ->
            persistUrlsFromJsonData(context, jsonData)
            jsonData.optJSONObject("guidanceTile")?.let {
                GuidanceTile.parse(it)
            }
        }!!
    }

    private fun persistUrlsFromJsonData(context: Context, jsonData: JSONObject) {
        Preference.setFeatureMHR(
                context,
                jsonData.optBoolean(AppConstants.KEY_MHR)
        )
        Preference.putUrlData(
                context, jsonData.toString()
        )
        Preference.setGuidanceTile(
                context,
                jsonData.optJSONObject("guidanceTile")?.let {
                    try {
                        GuidanceTile.parse(it)
                    } catch (e: Exception) {
                        null
                    }
                }
        )
        Preference.setMoreLinks(
                context,
                jsonData.optJSONArray("moreLinks")?.let {
                    moreLinksArrayMoshiAdapter.fromJson(it.toString())
                }
        )
    }

    private suspend fun getUrls(): JSONObject? {
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language
        val authResponse =
                Request.runRequest(
                        "/adapters/applicationDataAdapter/getUrls",
                        Request.GET,
                        queryParams = queryParams
                )

        if (authResponse.isSuccess() && !TextUtils.isEmpty(authResponse?.data?.toString())) {
            return authResponse.data
        }
        throw java.lang.Exception("Failed to get URLs")
    }
}