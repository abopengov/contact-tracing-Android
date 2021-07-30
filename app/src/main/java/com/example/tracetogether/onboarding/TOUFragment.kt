package com.example.tracetogether.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.privacy.PrivacyViewModel
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_tou.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*
    Fragment for the Privacy Acceptance screen
 */
class TOUFragment : OnboardingFragmentInterface(), CoroutineScope by MainScope() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "TOUFragment"
    private lateinit var mainContext: Context

    private val privacyViewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun becomesVisible() {}

    override fun onButtonClick(buttonView: View) {
        CentralLog.d(TAG, "OnButtonClick 4")
        privacyViewModel.acceptPrivacyPolicy()

        val onboardActivity = context as OnboardingActivity?
        onboardActivity?.let {
            it.navigateToNextPage()
        } ?: (Utils.restartAppWithNoContext(0, "TOUFragment not attached to OnboardingActivity"))
    }

    override fun onBackButtonClick() {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tou, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btn_next?.setLocalizedString("next_button")

        wv_privacy_policy?.setWebViewClient(object : WebViewClient() {
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
                progress_bar?.visibility = View.GONE
                wv_privacy_policy?.visibility = View.VISIBLE
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
            checkbox_agreement?.toggle()
        }

        tv_agreement?.setLocalizedString("agreement")

        privacyViewModel.privacyPolicy.observe(viewLifecycleOwner, Observer { privacyPolicy ->
            privacyPolicy?.let {
                wv_privacy_policy?.loadData(it, "text/html; charset=utf-8", null)
            }
        })

        privacyViewModel.getPrivacyPolicy()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainContext = context
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must  implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
