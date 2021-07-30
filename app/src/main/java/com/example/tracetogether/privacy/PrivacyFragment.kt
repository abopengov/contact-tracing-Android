package com.example.tracetogether.onboarding

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.MainActivity
import com.example.tracetogether.R
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.privacy.PrivacyViewModel
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_tou.*


/*
    Fragment for the Privacy Acceptance screen
 */
class PrivacyFragment : Fragment() {
    private val TAG: String = "PrivacyFragment"

    private val privacyViewModel: PrivacyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tou, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_next?.setLocalizedString("next_button")
        btn_next?.setOnClickListener {
            privacyViewModel.acceptPrivacyPolicy()
            startActivity(Intent(requireContext(), MainActivity::class.java))
            activity?.let {
                ActivityCompat.finishAffinity(it)
            }
        }

        wv_privacy_policy.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                if (url.startsWith("http")) {
                    openUrl(url)
                    return true
                } else if (url.startsWith("mailto:")) {
                    startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    )
                    return true
                }
                return false
            }

            override fun onLoadResource(view: WebView, url: String) {
                progress_bar.visibility = View.GONE
                wv_privacy_policy.visibility = View.VISIBLE
            }
        })

        privacy_button?.setOnClickListener {
            CentralLog.d(TAG, "clicked view privacy")
            openPrivacy()
        }
        faq_button?.setOnClickListener {
            CentralLog.d(TAG, "clicked view faq")
            openFaq()
        }

        privacy_button?.setLocalizedString("view_privacy")
        faq_button?.setLocalizedString("view_faq")

        disableButton()

        checkbox_agreement?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (checkbox_agreement.isChecked) enableButton() else disableButton()
        }

        tv_agreement?.setOnClickListener {
            checkbox_agreement.toggle()
        }

        tv_agreement?.setLocalizedString("agreement")

        privacyViewModel.privacyPolicy.observe(viewLifecycleOwner, Observer { privacyPolicy ->
            privacyPolicy?.let {
                wv_privacy_policy.loadData(it, "text/html; charset=utf-8", null)
            }
        })

        privacyViewModel.getPrivacyPolicy()
    }

    private fun enableButton() {
        btn_next?.isEnabled = true
    }

    private fun disableButton() {
        btn_next?.isEnabled = false
    }

    private fun openFaq() {
        val url = AppConstants.KEY_FAQ.getUrl(requireContext(), getString(R.string.faq_url))
        openUrl(url)
    }

    private fun openPrivacy() {
        val url = AppConstants.KEY_PRIVACY.getUrl(requireContext(), getString(R.string.privacy_url))
        openUrl(url)
    }

    private fun openUrl(url: String) {
        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }
}
