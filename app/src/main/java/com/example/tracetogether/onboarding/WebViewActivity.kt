package com.example.tracetogether.onboarding

import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.FragmentActivity
import com.example.tracetogether.BuildConfig
import com.example.tracetogether.R
import com.example.tracetogether.logging.CentralLog
import kotlinx.android.synthetic.main.webview.*


class WebViewActivity : FragmentActivity() {

    private val TAG = "WebViewActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        webview?.webViewClient = WebViewClient()

        //Grabs extra intent data to see which url to use
        //0 - for privacy url (default)
        //1 - for faq url
        val type = intent.getIntExtra("type",0)
        var url = ""
        if(type == 1){
            url = BuildConfig.FAQ_URL
            tv_title?.text = getString(R.string.faq_webview_title)
        }
        else{
            url = BuildConfig.PRIVACY_URL
            tv_title?.text = getString(R.string.privacy_policy_webview_title)
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
