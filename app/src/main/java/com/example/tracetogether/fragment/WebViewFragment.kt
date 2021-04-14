package com.example.tracetogether.fragment

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
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.hide
import com.example.tracetogether.util.Extensions.show
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.fragment_webview.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class WebViewFragment : Fragment(), CoroutineScope by MainScope() {

    private val KEY = "key"
    private val TITLE = "title"
    private var url: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_webview, container, false).apply {
            webview?.webViewClient = WebViewClient()
            webview?.settings?.javaScriptEnabled = true
            webview?.settings?.loadWithOverviewMode = true
            webview?.webChromeClient = WebChromeClient()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (TextUtils.isEmpty(arguments?.getString(TITLE))) {
            top_layout?.hide()
        } else {
            tv_title?.text = arguments?.getString(TITLE)
            top_layout?.show()
        }

        if (context != null) {
            url = arguments?.getString(KEY)?.getUrl(context!!)
        }
        if (TextUtils.isEmpty(url)) {
            requestUrl()
        } else {
            webview?.loadUrl(url)
            pgWebView?.hide()
        }

        webviewBackButton?.setOnClickListener {
            activity?.supportFragmentManager?.popBackStack()
        }

    }

    fun newInstance(key: String?, title: String?): WebViewFragment {
        val args = Bundle()
        args.putString(KEY, key)
        args.putString(TITLE, title)
        val fragment = WebViewFragment()
        fragment.arguments = args
        return fragment
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
            authResponse.data?.optString(arguments?.getString(KEY))?.let {
                webview?.loadUrl(it)
            }
        }

        tvErrorWebView?.isVisible = !authResponse.isSuccess()
        webview?.isVisible = authResponse.isSuccess()
    }

}
