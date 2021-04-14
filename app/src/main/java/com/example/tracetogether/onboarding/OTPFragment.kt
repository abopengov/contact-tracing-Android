package com.example.tracetogether.onboarding

import android.content.Context
import android.graphics.Paint
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.button_and_progress.*
import kotlinx.android.synthetic.main.fragment_otp.*
import kotlin.math.floor


private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/*
    Fragment for the OTP entry screen
 */
class OTPFragment : OnboardingFragmentInterface() {

    private var param1: String? = null
    private var param2: String? = null
    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "OTPFragment"
    private val COUNTDOWN_DURATION = 180L
    private var stopWatch: CountDownTimer? = null
    private var colorError: Int = 0
    private var colorText: Int = 0
    private var otpInputs: MutableList<EditText> = mutableListOf()
    private var timerHasFinished: Boolean = false


    private lateinit var phoneNumber: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun setUserVisibleHint(isVisibleToUser: Boolean) {
        super.setUserVisibleHint(isVisibleToUser)
        if (isVisibleToUser) {
            startTimer()
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

    override fun onBackButtonClick(view: View) {
        val onboardActivity = context as OnboardingActivity?
        onboardActivity?.let {
            it.onBackPressed()
        } ?: (Utils.restartAppWithNoContext(0, "OTPFragment not attached to OnboardingActivity"))
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
        onboardingButtonText?.setLocalizedString("next_button")
        sent_to?.text = HtmlCompat.fromHtml(
                "otp_sent".getLocalizedText()
                        .replace("%s", buildPhoneString(Preference.getPhoneNumber(context!!)))
                , HtmlCompat.FROM_HTML_MODE_LEGACY
        )

        tv_step_two?.setLocalizedString("otp_step")
        tv_enter_otp?.setLocalizedString("enter_otp")

        tv_receive_code?.setLocalizedString("resend_code_label")
        resendCode?.setLocalizedString("resend_code")
        resendCode.paintFlags = resendCode.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        colorError = ContextCompat.getColor(context!!, R.color.error)
        colorText = ContextCompat.getColor(context!!, R.color.grey_3)

        otpInputs.add(otp_et1)
        otpInputs.add(otp_et2)
        otpInputs.add(otp_et3)
        otpInputs.add(otp_et4)
        otpInputs.add(otp_et5)
        otpInputs.add(otp_et6)


        otp_et1?.addTextChangedListener(OTPTextWatcher(otp_et1, otpInputs));
        otp_et2?.addTextChangedListener(OTPTextWatcher(otp_et2, otpInputs));
        otp_et3?.addTextChangedListener(OTPTextWatcher(otp_et3, otpInputs));
        otp_et4?.addTextChangedListener(OTPTextWatcher(otp_et4, otpInputs));
        otp_et5?.addTextChangedListener(OTPTextWatcher(otp_et5, otpInputs));
        otp_et6?.addTextChangedListener(OTPTextWatcher(otp_et6, otpInputs));

        resendCode?.setOnClickListener {
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
        sent_to?.text = HtmlCompat.fromHtml(
                "otp_sent".getLocalizedText()
                        .replace("%s", buildPhoneString(num))
                , HtmlCompat.FROM_HTML_MODE_LEGACY
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

                timer?.text = HtmlCompat.fromHtml(
                        "otp_countdown".getLocalizedText().replace(
                                "%s",
                                "<b>$numberOfMinsInt:$finalNumberOfSecondsString</b>"
                        ),
                        HtmlCompat.FROM_HTML_MODE_LEGACY
                )
            }

            override fun onFinish() {
                timer?.setLocalizedString("otp_countdown_expired")
                timer?.setTextColor(colorError)
                setButtonText("resend_button".getLocalizedText())
                enableButton()
                timerHasFinished = true;
            }
        }
        stopWatch?.start()
        timer?.setTextColor(colorText)
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

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
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
                R.id.otp_et2 -> if (text.length == 1) inputs[2].requestFocus() else if (text.isEmpty()) inputs[0].requestFocus()
                R.id.otp_et3 -> if (text.length == 1) inputs[3].requestFocus() else if (text.isEmpty()) inputs[1].requestFocus()
                R.id.otp_et4 -> if (text.length == 1) inputs[4].requestFocus() else if (text.isEmpty()) inputs[2].requestFocus()
                R.id.otp_et5 -> if (text.length == 1) inputs[5].requestFocus() else if (text.isEmpty()) inputs[3].requestFocus()
                R.id.otp_et6 -> if (text.isEmpty()) inputs[4].requestFocus()
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
