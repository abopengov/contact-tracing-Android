package com.example.tracetogether.fragment

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.tracetogether.BuildConfig
import com.example.tracetogether.R
import com.example.tracetogether.api.Request
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.AppConstants.KEY_FAQ
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.hide
import kotlinx.android.synthetic.main.fragment_help.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

/*
    Fragment for the Help screen,
    contains the Web View activity to show the FAQ page inline
 */
class HelpFragment : Fragment(), CoroutineScope by MainScope() {
    private lateinit var inflater: LayoutInflater
    private val TAG = "HomeFragment"
    private var url: String? = null


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        this.inflater = inflater
        val view = inflater.inflate(R.layout.fragment_help, container, false)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //This can be merge with the WebViewActivity in the onboarding directory

        help_webview?.webViewClient = WebViewClient()
        //Javascript must be enabled or page menu will be too large
        help_webview?.settings!!.javaScriptEnabled = true
        help_webview?.settings!!.loadWithOverviewMode = true

        val wbc: WebChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(w: WebView) {
                CentralLog.d(TAG, "OnCloseWindow for WebChromeClient")
            }
        }

        help_webview?.webChromeClient = wbc

        if (context != null) {
            url = KEY_FAQ?.getUrl(context!!)
        }
        if (TextUtils.isEmpty(url)) {
            getFaqUrl()
        } else {
            pgWebView?.hide()
            help_webview?.loadUrl(url)
        }


    }

    private fun getFaqUrl() = launch {
        pgWebView?.isVisible = true
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language
        val faqUrlResponse =
                Request.runRequest(
                        "/adapters/applicationDataAdapter/getUrls",
                        Request.GET,
                        queryParams = queryParams
                )
        pgWebView?.isVisible = false

        if (faqUrlResponse?.isSuccess()) {
            faqUrlResponse.data?.optString(KEY_FAQ)?.let {
                help_webview.loadUrl(it)
            }
        }

    }

}

