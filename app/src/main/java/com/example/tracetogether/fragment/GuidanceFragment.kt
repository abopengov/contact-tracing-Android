package com.example.tracetogether.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.api.Request
import com.example.tracetogether.util.AppConstants.KEY_GUIDANCE
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.hide
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_guidance.*
import kotlinx.android.synthetic.main.fragment_guidance.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class GuidanceFragment : Fragment(), CoroutineScope by MainScope() {

    private var url: String? = null

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_guidance, container, false).apply {
            wvGuidance?.webViewClient = WebViewClient()
            wvGuidance?.settings?.javaScriptEnabled = true
            wvGuidance?.settings?.loadWithOverviewMode = true
            wvGuidance?.webChromeClient = WebChromeClient()

            if (context != null) {
                url = KEY_GUIDANCE.getUrl(context!!)
            }
            if (TextUtils.isEmpty(url)) {
                requestUrl()
            } else {
                pgWebView?.hide()
                wvGuidance?.loadUrl(url)
            }

        }
    }


    private fun requestUrl() = launch {
        tvErrorWebView?.setLocalizedString("failed_to_load_data")
        pgWebView?.isVisible = true
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language

        val authResponse =
            Request.runRequest(
                "/adapters/applicationDataAdapter/getUrls",
                Request.GET,
                queryParams = queryParams
            )
        pgWebView?.isVisible = false

        if (authResponse.isSuccess()) {
            authResponse.data?.optString(KEY_GUIDANCE)?.let {
                wvGuidance?.loadUrl(it as String)
            }
        }

        tvErrorWebView?.isVisible = !authResponse.isSuccess()
        wvGuidance?.isVisible = authResponse.isSuccess()
    }


}
