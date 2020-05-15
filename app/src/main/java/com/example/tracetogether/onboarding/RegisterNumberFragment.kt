package com.example.tracetogether.onboarding

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import com.example.tracetogether.*
import kotlinx.android.synthetic.main.fragment_register_number.*
import com.example.tracetogether.logging.CentralLog

/*
    Fragment for the mobile number register screen
 */
class RegisterNumberFragment : OnboardingFragmentInterface() {

    private var listener: OnFragmentInteractionListener? = null
    private val TAG: String = "RegisterNumberFragment"

    private var mView: View? = null

    private var backspaceFlag: Boolean = false;
    private var editFlag: Boolean = false;
    private var selectionPointer: Int = 0;

    override fun becomesVisible() {
        CentralLog.d(TAG, "becomes visible")
        val myActivity = this as OnboardingFragmentInterface


        if(validateNumber(phone_number?.text.toString())){
            myActivity.enableButton()
        }else{
            myActivity.disableButton()
        }


    }

    override fun onButtonClick(buttonView: View) {
        CentralLog.d(TAG, "OnButtonClick")
        disableButtonAndRequestOTP()
    }

    override fun onBackButtonClick(view: View) {
        val onboardActivity = context as OnboardingActivity
        onboardActivity.onBackPressed()
    }

    private fun disableButtonAndRequestOTP() {
        var myactivity = this as OnboardingFragmentInterface
        myactivity.disableButton()
        requestOTP()
    }

    private fun requestOTP() {
        mView?.let { view ->
            phone_number_error.visibility = View.INVISIBLE

            var numberText = getUnmaskedNumber(phone_number.text.toString())
            CentralLog.d(TAG, "The value retrieved: ${numberText}")

            val onboardActivity = context as OnboardingActivity
            Preference.putPhoneNumber(TracerApp.AppContext, numberText)
            onboardActivity.updatePhoneNumber(numberText)
            onboardActivity.requestForOTP(numberText)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        CentralLog.i(TAG, "View created")
        mView = view

        phone_number?.addTextChangedListener(object : PhoneNumberFormattingTextWatcher() {
            override fun afterTextChanged(s: Editable) {
                applyMask(s.toString())
            }
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
                selectionPointer = s.length - phone_number.selectionStart
                backspaceFlag = count>after
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                phone_number_error.visibility = View.GONE
                if(validateNumber(s.toString())){
                    enableButton()
                }else{
                    disableButton()
                }
            }
        })
        phone_number?.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                Utils.hideKeyboardFrom(view.context, view)
                disableButtonAndRequestOTP()
                true
            } else {
                false
            }
        }
        tv_app_version?.text = getString(R.string.app_version_label) + BuildConfig.VERSION_NAME

        disableButton()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        CentralLog.i(TAG, "Making view")
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_register_number, container, false)
    }



    override fun onUpdatePhoneNumber(num: String) {
        CentralLog.d(TAG, "onUpdatePhoneNumber $num")
    }

    override fun onError(error: String) {
        phone_number_error.let {
            phone_number_error.visibility = View.VISIBLE
            phone_number_error.text = error
        }
        CentralLog.e(TAG, "error: ${error.toString()}")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnFragmentInteractionListener) {
            listener = context
        } else {
            throw RuntimeException("$context must implement OnFragmentInteractionListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
        mView = null

        CentralLog.i(TAG, "Detached??")
    }

    private fun validateNumber(num:String):Boolean{
        val phone: String = getUnmaskedNumber(num)
        return phone.length == 10
    }

    private fun getUnmaskedNumber(num:String):String{
        return num.replace("[^\\d]".toRegex(), "")
    }

    private fun applyMask(num: String){
        val phone: String = getUnmaskedNumber(num)
        val phoneLength: Int = phone.length
        var masked: String = ""

        if(!editFlag){
            if(phoneLength >= 6 && !backspaceFlag){
                editFlag = true;
                masked = "(" + phone.substring(0,3) + ") " + phone.substring(3,6) + "-" + phone.substring(6);
                phone_number.setText(masked);
                phone_number.setSelection(phone_number.text.length - selectionPointer)
            }
            else if(phoneLength >=3 && !backspaceFlag){
                editFlag = true;
                masked = "(" + phone.substring(0,3) + ") " + phone.substring(3)
                phone_number.setText(masked);
                phone_number.setSelection(phone_number.text.length - selectionPointer)
            }
        }
        else{
            editFlag = false;
        }
    }

    interface OnFragmentInteractionListener {
        fun onFragmentInteraction(uri: Uri)
    }
}
