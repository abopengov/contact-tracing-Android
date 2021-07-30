package com.example.tracetogether.onboarding

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.underline
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import kotlinx.android.synthetic.main.fragment_otp.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.floor


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*
    Fragment for the OTP entry screen
 */
class OTPFragment : OnboardingFragmentInterface() {

    companion object {
        const val SIX_DIGITS_REGEX = "(|^)\\d{6}"
    }

    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "OTPFragment"
    private val COUNTDOWN_DURATION = 180L
    private var stopWatch: CountDownTimer? = null
    private var otpInputs: MutableList<EditText> = mutableListOf()
    private var timerHasFinished: Boolean = false


    private lateinit var phoneNumber: String

    private val startSmsConsent =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val message = result.data?.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                if (message != null) {
                    getOtpFromMessage(message)
                }
            }
        }

    private val smsVerificationReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
                val extras = intent.extras
                val smsRetrieverStatus = extras?.get(SmsRetriever.EXTRA_STATUS) as Status

                when (smsRetrieverStatus.statusCode) {
                    CommonStatusCodes.SUCCESS -> {
                        val consentIntent =
                            extras.getParcelable<Intent>(SmsRetriever.EXTRA_CONSENT_INTENT)
                        startSmsConsent.launch(consentIntent)
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(smsVerificationReceiver, intentFilter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireActivity().unregisterReceiver(smsVerificationReceiver)
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser && context != null) {
            startTimer()
            SmsRetriever.getClient(requireContext()).startSmsUserConsent(null)
        } else {
            resetTimer()
        }
    }

    private fun resetTimer() {
        stopWatch?.cancel()
    }

    override fun becomesVisible() {
        CentralLog.d(TAG, "becomes visible")
        clearInputs()
    }

    override fun onButtonClick(view: View) {
        CentralLog.d(TAG, "OnButtonClick 3B")

        val otp = getOtp()

        val onboardActivity = context as OnboardingActivity?

        if (timerHasFinished) {
            resendCodeAndStartTimer()
        } else {
            onboardActivity?.let {
                it.validateOTP(otp)
            } ?: (Utils.restartAppWithNoContext(
                0,
                "OTPFragment not attached to OnboardingActivity"
            ))
        }
    }

    override fun onBackButtonClick() {
        val onboardActivity = context as OnboardingActivity?
        onboardActivity?.let {
            it.onBackPressed()
        } ?: (Utils.restartAppWithNoContext(0, "OTPFragment not attached to OnboardingActivity"))
    }

    private fun setOtp(otp: String) {
        if (otp.length == otpInputs.size) {
            otpInputs.forEachIndexed { index, editText ->
                editText.setText(otp[index].toString())
            }

            otpInputs.last().setSelection(otpInputs.last().text.length)
        }
    }

    private fun getOtp(): String {
        var otp = ""
        for (input in otpInputs) {
            otp += input.text
        }
        return otp
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_otp, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        btn_next?.setLocalizedString("next_button")
        tv_sent_to?.text = String.format(
            "onboarding_otp_description".getLocalizedText(),
            buildPhoneString(Preference.getPhoneNumber(requireContext()))
        )

        tv_enter_otp?.setLocalizedString("onboarding_otp_title")

        tv_receive_code?.setLocalizedString("resend_code_label")
        tv_resend_code?.setLocalizedString("resend_code")
        tv_resend_code?.underline()

        tv_expired?.setLocalizedString("otp_countdown_expired")

        otpInputs.add(otp_et1)
        otpInputs.add(otp_et2)
        otpInputs.add(otp_et3)
        otpInputs.add(otp_et4)
        otpInputs.add(otp_et5)
        otpInputs.add(otp_et6)


        otp_et1?.addTextChangedListener(OTPTextWatcher(otp_et1, otpInputs))
        otp_et2?.addTextChangedListener(OTPTextWatcher(otp_et2, otpInputs))
        otp_et3?.addTextChangedListener(OTPTextWatcher(otp_et3, otpInputs))
        otp_et4?.addTextChangedListener(OTPTextWatcher(otp_et4, otpInputs))
        otp_et5?.addTextChangedListener(OTPTextWatcher(otp_et5, otpInputs))
        otp_et6?.addTextChangedListener(OTPTextWatcher(otp_et6, otpInputs))

        otp_et1?.setOnKeyListener(OTPKeyListener())
        otp_et2?.setOnKeyListener(OTPKeyListener())
        otp_et3?.setOnKeyListener(OTPKeyListener())
        otp_et4?.setOnKeyListener(OTPKeyListener())
        otp_et5?.setOnKeyListener(OTPKeyListener())
        otp_et6?.setOnKeyListener(OTPKeyListener())

        tv_resend_code?.setOnClickListener {
            CentralLog.d(TAG, "resend pressed")
            resendCodeAndStartTimer()
        }

        otp_et6?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                Utils.hideKeyboardFrom(view.context, view)
                val otp = getOtp()
                val onboardActivity = context as OnboardingActivity?
                onboardActivity?.let {
                    it.validateOTP(otp)
                    true
                } ?: (Utils.restartAppWithNoContext(
                    0,
                    "OTPFragment not attached to OnboardingActivity"
                ))
                false
            } else {
                false
            }
        }
    }

    override fun onUpdatePhoneNumber(num: String) {
        CentralLog.d(TAG, "onUpdatePhoneNumber $num")
        tv_sent_to?.text = String.format(
            "onboarding_otp_description".getLocalizedText(),
            buildPhoneString(Preference.getPhoneNumber(requireContext()))
        )

        phoneNumber = num
    }

    private fun startTimer() {
        timerHasFinished = false
        stopWatch = object : CountDownTimer(COUNTDOWN_DURATION * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val numberOfMins = floor((millisUntilFinished * 1.0) / 60000)
                val numberOfMinsInt = numberOfMins.toInt()
                val numberOfSeconds = floor((millisUntilFinished / 1000.0) % 60)
                val numberOfSecondsInt = numberOfSeconds.toInt()
                var finalNumberOfSecondsString = ""
                if (numberOfSecondsInt < 10) {
                    finalNumberOfSecondsString = "0$numberOfSecondsInt"
                } else {
                    finalNumberOfSecondsString = "$numberOfSecondsInt"
                }

                tv_timer?.text = String.format(
                    "otp_countdown".getLocalizedText(),
                    "$numberOfMinsInt:$finalNumberOfSecondsString"
                )
            }

            override fun onFinish() {
                tv_timer?.visibility = View.GONE
                tv_expired?.visibility = View.VISIBLE
                tv_receive_code?.visibility = View.GONE
                setButtonText("resend_button".getLocalizedText())
                enableButton()
                timerHasFinished = true;
            }
        }
        stopWatch?.start()
        tv_timer?.visibility = View.VISIBLE
        tv_expired?.visibility = View.GONE
        tv_receive_code?.visibility = View.VISIBLE
    }

    override fun onError(error: String) {
        var onboardActivity = context as OnboardingActivity?
        onboardActivity?.let {
            it.onBackPressed()
        } ?: (Utils.restartAppWithNoContext(0, "OTPFragment not attached to OnboardingActivity"))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException(context.toString() + " must implement OnFragmentInteractionListener")
        }

    }

    override fun onDetach() {
        super.onDetach()
        listener = null

    }

    override fun onDestroy() {
        super.onDestroy()
        stopWatch?.cancel()
    }

    private fun buildPhoneString(phone: String): String {
        if (phone.length < 6) {
            return phone
        }
        return "+1 " + phone.substring(0, 3) + "-" + phone.substring(
            3,
            6
        ) + "-" + phone.substring(6);
    }

    private fun validateNumber(num: String): Boolean {
        return num.length >= 6
    }

    private fun clearInputs() {
        otp_et6?.setText("")
        otp_et5?.setText("")
        otp_et4?.setText("")
        otp_et3?.setText("")
        otp_et2?.setText("")
        otp_et1?.setText("")
    }

    private fun resendCodeAndStartTimer() {
        val onboardingActivity = activity as OnboardingActivity?

        clearInputs()

        onboardingActivity?.let {
            it.resendCode()
        } ?: (Utils.restartAppWithNoContext(0, "OTPFragment not attached to OnboardingActivity"))

        resetTimer()
        startTimer()
    }

    private fun getOtpFromMessage(message: String) {
        val pattern: Pattern = Pattern.compile(SIX_DIGITS_REGEX)
        val matcher: Matcher = pattern.matcher(message)
        if (matcher.find()) {
            matcher.group(0)?.let { otp -> setOtp(otp) }
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }

    inner class OTPKeyListener : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (keyCode == KeyEvent.KEYCODE_DEL && event?.action == KeyEvent.ACTION_DOWN) {
                val editText = v as EditText
                val previousBox = getPreviousBox(editText.id)
                if (editText.text.isEmpty() && previousBox != null) {
                    previousBox.setText("")
                    previousBox.requestFocus()
                    return true
                }
            }
            return false
        }

        private fun getPreviousBox(id: Int): EditText? {
            return when (id) {
                R.id.otp_et2 -> otpInputs[0]
                R.id.otp_et3 -> otpInputs[1]
                R.id.otp_et4 -> otpInputs[2]
                R.id.otp_et5 -> otpInputs[3]
                R.id.otp_et6 -> otpInputs[4]
                else -> null
            }
        }
    }

    //Custom TextWatch for the several OTP EditText elements
    //Switches between the several inputs
    //This could be made more generic and stored somewheres else
    inner class OTPTextWatcher(val view: View, otpInputs: MutableList<EditText>) : TextWatcher {
        private var inputs = otpInputs
        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (view.id) {
                R.id.otp_et1 -> if (text.length == 1) inputs[1].requestFocus()
                R.id.otp_et2 -> if (text.length == 1) inputs[2].requestFocus()
                R.id.otp_et3 -> if (text.length == 1) inputs[3].requestFocus()
                R.id.otp_et4 -> if (text.length == 1) inputs[4].requestFocus()
                R.id.otp_et5 -> if (text.length == 1) inputs[5].requestFocus()
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
            if (validateNumber(getOtp())) {
                Utils.hideKeyboardFrom(view.context, view)
                enableButton()
                setButtonText("submit_button".getLocalizedText())
            } else {
                disableButton()
                setButtonText("next_button".getLocalizedText())
            }
        }
    }
}
