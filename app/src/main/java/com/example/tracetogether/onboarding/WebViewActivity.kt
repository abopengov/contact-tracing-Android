package com.example.tracetogether.onboarding

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import com.example.tracetogether.R
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.AppConstants.KEY_FAQ
import com.example.tracetogether.util.AppConstants.KEY_PRIVACY
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.webview.*


class WebViewActivity : FragmentActivity() {

    private val TAG = "WebViewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        tv_title?.setLocalizedString("webview_title")
        webview?.webViewClient = WebViewClient()

        //Grabs extra intent data to see which url to use
        //0 - for privacy url (default)
        //1 - for faq url
        val type = intent.getIntExtra("type", 0)
        var url = ""
        if (type == 1) {
            url = KEY_FAQ.getUrl(this) ?: getString(R.string.faq_url)
            tv_title?.setLocalizedString("faq_webview_title")
        } else {
            url = KEY_PRIVACY.getUrl(this) ?: getString(R.string.privacy_url)
            tv_title?.setLocalizedString("privacy_policy_webview_title")
        }

        //Javascript must be enabled or page menu will be too large
        webview?.settings!!.javaScriptEnabled = true
        webview?.settings!!.loadWithOverviewMode = true

        webview?.loadUrl(url)

        val wbc: WebChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(w: WebView) {
                CentralLog.d(TAG, "OnCloseWindow for WebChromeClient")
            }
        }
        webview?.webChromeClient = wbc

        webviewBackButton?.let {
            it.setOnClickListener { buttonView ->
                this.finish()
            }
        }
    }
}
