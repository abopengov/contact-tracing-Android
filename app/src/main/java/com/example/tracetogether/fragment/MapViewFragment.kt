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
import com.example.tracetogether.R
import com.example.tracetogether.api.Request
import com.example.tracetogether.util.AppConstants.KEY_GIS
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.hide
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_mapview.*
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.fragment_webview.pgWebView
import kotlinx.android.synthetic.main.fragment_webview.tvErrorWebView
import kotlinx.android.synthetic.main.fragment_webview.tv_title
import kotlinx.android.synthetic.main.fragment_webview.view.*
import kotlinx.android.synthetic.main.fragment_webview.webview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class MapViewFragment : Fragment(), CoroutineScope by MainScope(), View.OnClickListener {

    private var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_mapview, container, false).apply {
            webview?.webViewClient = WebViewClient()
            webview?.settings?.javaScriptEnabled = true
            webview?.settings?.loadWithOverviewMode = true
            webview?.webChromeClient = WebChromeClient()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("map_header")
        tv_subheader?.setLocalizedString("menu_subheader")
        tv_map?.setLocalizedString("map_map")
        tv_list?.setLocalizedString("map_list")
        tv_map?.setOnClickListener(this)
        tv_list?.setOnClickListener(this)
        tv_map?.isSelected = true

        if (context != null) {
            url = KEY_GIS?.getUrl(context!!)
        }
        if (TextUtils.isEmpty(url)) {
            requestUrl()
        } else {
            webview?.loadUrl(url)
            pgWebView?.hide()
        }

        webview.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                webview?.loadUrl(
                    "javascript:(function() { " +
                            "document.getElementById('list-of-active-cases-by-region').style.display='none'; " +
                            "document.getElementById('htmlwidget-838acccd70e88695096c').style.display='block'; " +
                            "document.getElementById('goa-grid10032').style.display='block'; " +
                            "})()"
                )

            }
        }

    }

    fun newInstance(): MapViewFragment {
        return MapViewFragment()
    }

    private fun requestUrl() = launch {
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
            authResponse.data?.optString(KEY_GIS)?.let {
                webview?.loadUrl(it)
            }
        }

        tvErrorWebView?.isVisible = !authResponse.isSuccess()
        webview?.isVisible = authResponse.isSuccess()
    }

    override fun onClick(v: View?) {
        var id = v?.id
        when (id) {
            R.id.tv_map -> {
                tv_map?.isSelected = true
                tv_list?.isSelected = false

                webview?.loadUrl(
                    "javascript:(function() { " +
                            "document.getElementById('list-of-active-cases-by-region').style.display='none'; " +
                            "document.querySelector('[id^=\"htmlwidget-\"]').style.display='block';" +
                            "document.getElementById('goa-grid10032').style.display='block'; " +
                            "})()"
                )

            }

            R.id.tv_list -> {
                tv_map?.isSelected = false
                tv_list?.isSelected = true
                var css =
                    "javascript:(function() { " +
                            "document.getElementById('goa-grid10032').style.display='none';" +
                            "document.getElementById('list-of-active-cases-by-region').style.display='block'; " +
                            "document.querySelector('[id^=\"htmlwidget-\"]').style.display='none';" +
                            "document.getElementById('list-of-active-cases-by-region').style.margin='-200px auto auto auto'" +
                            "})()"


                webview?.loadUrl(
                    "javascript:(function() { " +
                            css +
                            "})()"
                )

            }
        }

    }

}
