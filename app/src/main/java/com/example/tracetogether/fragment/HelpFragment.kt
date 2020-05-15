package com.example.tracetogether.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.Fragment
import com.example.tracetogether.BuildConfig
import com.example.tracetogether.R
import com.example.tracetogether.logging.CentralLog
import kotlinx.android.synthetic.main.fragment_help.*

/*
    Fragment for the Help screen,
    contains the Web View activity to show the FAQ page inline
 */
class HelpFragment : Fragment() {
    private lateinit var inflater: LayoutInflater
    private val TAG = "HomeFragment"


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
        help_webview?.loadUrl(BuildConfig.FAQ_URL)

        val wbc: WebChromeClient = object : WebChromeClient() {
            override fun onCloseWindow(w: WebView) {
                CentralLog.d(TAG, "OnCloseWindow for WebChromeClient")
            }
        }

        help_webview?.webChromeClient = wbc
    }

}
