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
import com.example.tracetogether.logging.CentralLog
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
        helpEmail = getString(R.string.help_desk_email)
    }

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun becomesVisible() {}

    override fun onButtonClick(buttonView: View) {
        CentralLog.d(TAG, "OnButtonClick 4")
        val onboardActivity = context as OnboardingActivity
        onboardActivity.navigateToNextPage()
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

        //Add extra data to WebView intent, to determine which url to use
        //0 - for privacy url
        //1 - for faq url
        privacy_button?.setOnClickListener {
            CentralLog.d(TAG, "clicked view privacy")
            val intent = Intent(mainContext, WebViewActivity::class.java)
            intent.putExtra("type",0)
            startActivity(intent)
        }
        faq_button?.setOnClickListener {
            CentralLog.d(TAG, "clicked view faq")
            val intent = Intent(mainContext, WebViewActivity::class.java)
            intent.putExtra("type",1)
            startActivity(intent)
        }

        disableButton()

        val privacyClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                val intent = Intent(mainContext, WebViewActivity::class.java)
                intent.putExtra("type",0)
                startActivity(intent)
            }
        }

        privacy_desc4?.text = createSpannableString(privacy_desc4.text.toString(),getString(R.string.privacy_statement_label),privacyClickableSpan)


        val emailClickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                CentralLog.d(TAG, "Starting send email intent")
                sendEmailIntent()
            }
        }
        

        privacy_desc7?.text = createSpannableString(privacy_desc7.text.toString(),helpEmail,emailClickableSpan)
        privacy_desc8?.text = createSpannableString(privacy_desc8.text.toString(),helpEmail,emailClickableSpan)

        checkbox_agreement?.setOnCheckedChangeListener { buttonView, isChecked ->
            if(checkbox_agreement.isChecked) enableButton() else disableButton()
        }

        privacy_desc4?.setMovementMethod(LinkMovementMethod.getInstance());
        privacy_desc7?.setMovementMethod(LinkMovementMethod.getInstance());
        privacy_desc8?.setMovementMethod(LinkMovementMethod.getInstance());

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

    private fun sendEmailIntent(){
        var email: Array<String> = arrayOf(helpEmail)
        val emailIntent = Intent(Intent.ACTION_SENDTO).apply{
            type = "text/plain"
            data = Uri.parse("mailto:") // only email apps should handle this
            putExtra(Intent.EXTRA_EMAIL, email)
        }
        if (emailIntent.resolveActivity(context!!.packageManager) != null) {
            startActivity(emailIntent)
        }
    }

    private fun createSpannableString(string : String, span : String, clickableSpan: ClickableSpan): SpannableString {
        val spanIndex = string.indexOf("%s")
        val newString = string.replaceFirst(Regex("\\%s\\b"),span)
        val ss = SpannableString(newString)

        ss.setSpan(
            clickableSpan,
            spanIndex,
            spanIndex + span.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        return ss
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
