package com.example.tracetogether.onboarding

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.AppConstants.KEY_FAQ
import com.example.tracetogether.util.AppConstants.KEY_HELP
import com.example.tracetogether.util.AppConstants.KEY_PRIVACY
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.button_and_progress.*
import kotlinx.android.synthetic.main.fragment_tou.*


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*
    Fragment for the Privacy Acceptance screen
 */
class TOUFragment : OnboardingFragmentInterface() {
    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "TOUFragment"
    private lateinit var mainContext: Context

    private var helpEmail: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
        helpEmail = KEY_HELP.getUrl(context!!) ?: getString(R.string.help_desk_email)
    }

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun becomesVisible() {}

    override fun onButtonClick(buttonView: View) {
        CentralLog.d(TAG, "OnButtonClick 4")
        val onboardActivity = context as OnboardingActivity?
        onboardActivity?.let {
            it.navigateToNextPage()
        } ?: (Utils.restartAppWithNoContext(0, "TOUFragment not attached to OnboardingActivity"))
    }

    override fun onBackButtonClick(view: View) {}

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_tou, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("privacy_policy_title")
        privacy_desc1?.setLocalizedString("privacy_policy_text1")
        privacy_desc2?.setLocalizedString("privacy_policy_text2")
        privacy_desc3?.setLocalizedString("privacy_policy_text3")
        privacy_desc4?.setLocalizedString("privacy_policy_text4")
        privacy_desc5?.setLocalizedString("privacy_policy_text5")
        privacy_desc7?.setLocalizedString("privacy_policy_text7")
        privacy_desc8?.setLocalizedString("privacy_policy_text8")
        onboardingButtonText?.setLocalizedString("next_button")

        //Add extra data to WebView intent, to determine which url to use
        //0 - for privacy url
        //1 - for faq url
        privacy_button?.setOnClickListener {
            CentralLog.d(TAG, "clicked view privacy")
            startWebActivityIntent(0)
        }
        faq_button?.setOnClickListener {
            CentralLog.d(TAG, "clicked view faq")
            startWebActivityIntent(1)
        }

        privacy_button?.setLocalizedString("view_privacy")
        faq_button?.setLocalizedString("view_faq")

        disableButton()

        val privacyClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                startWebActivityIntent(0)
            }
        }

        val faqClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                startWebActivityIntent(1)
            }
        }

        privacy_desc6?.text = createSpannableStringByText(
                "privacy_policy_text6".getLocalizedText(), "privacy_policy_text6_key".getLocalizedText(),
                privacyClickableSpan
        )
        privacy_desc9?.text = createSpannableStringByText(
                "privacy_policy_text9".getLocalizedText(), "privacy_policy_text9_key".getLocalizedText(),
                faqClickableSpan
        )

        val emailClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                CentralLog.d(TAG, "Starting send email intent")
                sendEmailIntent()
            }
        }


        privacy_desc10?.text =
                createSpannableString(
                        "privacy_policy_text10".getLocalizedText().replace(".","") + " %s",
                        "$helpEmail.",
                        emailClickableSpan
                )
        privacy_desc11?.setLocalizedString("privacy_policy_text11")
        privacy_desc12?.text =
                createSpannableString(
                        "privacy_policy_text12".getLocalizedText() + " %s",
                        "$helpEmail.",
                        emailClickableSpan
                )

        checkbox_agreement?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (checkbox_agreement.isChecked) enableButton() else disableButton()
        }

        privacy_desc6?.setMovementMethod(LinkMovementMethod.getInstance())
        privacy_desc9?.setMovementMethod(LinkMovementMethod.getInstance())
        privacy_desc10?.setMovementMethod(LinkMovementMethod.getInstance())
        privacy_desc12?.setMovementMethod(LinkMovementMethod.getInstance())

        tv_agreement?.setLocalizedString("agreement")

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

    private fun sendEmailIntent() {
        var email: Array<String> = arrayOf(helpEmail)
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
            type = "text/plain"
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, email)
        }
        if (emailIntent.resolveActivity(context!!.packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    private fun startWebActivityIntent(type: Int) {
        val intent = Intent(mainContext, WebViewActivity::class.java)
        intent.putExtra("type", type)
        startActivity(intent)
    }

    private fun createSpannableString(
            string: String,
            span: String,
            clickableSpan: ClickableSpan
    ): SpannableString {
        val spanIndex = string.indexOf("%s")
        val newString = string.replaceFirst(Regex("\\%s\\b"), span)

        val ss = SpannableString(newString)

        if (spanIndex > 0) {
            ss.setSpan(
                    clickableSpan,
                    spanIndex,
                    spanIndex + span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return ss
    }

    private fun createSpannableStringByText(
            string: String,
            span: String,
            clickableSpan: ClickableSpan
    ): SpannableString {
        val spanIndex = string.indexOf(span)
        val ss = SpannableString(string)

        if (spanIndex > 0) {
            ss.setSpan(
                    clickableSpan,
                    spanIndex,
                    spanIndex + span.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        return ss
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
