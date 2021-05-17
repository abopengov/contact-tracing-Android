package com.example.tracetogether.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import androidx.fragment.app.Fragment
import com.example.tracetogether.R
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.AppConstants.KEY_HOME
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_statistics.*

class StatsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_statistics, container, false)
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_case_highlight_title?.setLocalizedString("stats_case_highlights")
        btn_see_all_stats?.setLocalizedString("stats_see_all_stats")
        btn_see_all_stats?.setOnClickListener { openDashboard() }

        wv_highlights.webViewClient = object : WebViewClient() {
            override fun onPageCommitVisible(view: WebView?, url: String?) {
                progress_bar_container.visibility = View.GONE
                super.onPageCommitVisible(view, url)
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                tv_load_error?.setLocalizedString("failed_to_load_data")
                tv_load_error.visibility = View.VISIBLE
                wv_highlights.visibility = View.GONE
                super.onReceivedError(view, request, error)
            }
        }
        wv_highlights.settings.javaScriptEnabled = true
        wv_highlights.settings.loadWithOverviewMode = true
        wv_highlights.webChromeClient = WebChromeClient()

        val url = KEY_HOME.getUrl(requireContext(), getString(R.string.highlights_url))
        wv_highlights?.loadUrl(url)
    }

    private fun openDashboard() {
        val url = AppConstants.KEY_STATS.getUrl(requireContext(), getString(R.string.stats_url))
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

}
