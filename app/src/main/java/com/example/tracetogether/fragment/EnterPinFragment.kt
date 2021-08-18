package com.example.tracetogether.fragment

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.TracerApp
import com.example.tracetogether.Utils
import com.example.tracetogether.api.Request
import com.example.tracetogether.api.Response
import com.example.tracetogether.home.HomeFragment
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.model.CovidTestData
import com.example.tracetogether.model.ExportData
import com.example.tracetogether.status.persistence.StatusRecordStorage
import com.example.tracetogether.streetpass.persistence.StreetPassRecordStorage
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.toJSONObject
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.common.collect.Lists
import io.reactivex.disposables.Disposable
import kotlinx.android.synthetic.main.fragment_upload_enterpin.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

/*
    Fragment for OTP entry for the Upload flow,
    Handles retrieving upload token,
    retrieving encounter log from storage,
    and encounter log upload
 */
class EnterPinFragment : Fragment(), CoroutineScope by MainScope() {
    companion object {
        const val TAG = "UploadFragment"
        const val ENCOUNTER_PARTITION_SIZE = 50000
        const val OTP_CODE_PREFIX = ": "
        const val OTP_CODE_LENGTH = 6
        private const val EXTRA_COVID_TEST_DATA = "EXTRA_COVID_TEST_DATA"

        fun newInstance(covidTestData: CovidTestData): EnterPinFragment {
                val fragment = EnterPinFragment()
                val bundle = Bundle(1)
                bundle.putParcelable(EXTRA_COVID_TEST_DATA, covidTestData)
                fragment.arguments = bundle
                return fragment
            }
    }

    private var disposeObj: Disposable? = null

    private var uploadToken: String? = null
    private var otpInputs: MutableList<EditText> = mutableListOf()

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

        SmsRetriever.getClient(requireContext()).startSmsUserConsent(null)

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        requireActivity().registerReceiver(smsVerificationReceiver, intentFilter)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_upload_enterpin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        clearInputs()
        btn_next?.isEnabled = false

        tv_title?.setLocalizedString("enter_pin_title")
        tv_desc?.setLocalizedString("enter_pin_desc")
        enterPinFragmentErrorMessage?.setLocalizedString("invalid_pin")
        btn_next?.setLocalizedString("upload_button")

        otpInputs.add(otp_et1)
        otpInputs.add(otp_et2)
        otpInputs.add(otp_et3)
        otpInputs.add(otp_et4)
        otpInputs.add(otp_et5)
        otpInputs.add(otp_et6)


        otp_et1?.addTextChangedListener(OTPTextWatcher(otp_et1))
        otp_et2?.addTextChangedListener(OTPTextWatcher(otp_et2))
        otp_et3?.addTextChangedListener(OTPTextWatcher(otp_et3))
        otp_et4?.addTextChangedListener(OTPTextWatcher(otp_et4))
        otp_et5?.addTextChangedListener(OTPTextWatcher(otp_et5))
        otp_et6?.addTextChangedListener(OTPTextWatcher(otp_et6))

        otp_et1?.setOnKeyListener(OTPKeyListener())
        otp_et2?.setOnKeyListener(OTPKeyListener())
        otp_et3?.setOnKeyListener(OTPKeyListener())
        otp_et4?.setOnKeyListener(OTPKeyListener())
        otp_et5?.setOnKeyListener(OTPKeyListener())
        otp_et6?.setOnKeyListener(OTPKeyListener())

        btn_next?.setOnClickListener {
            if (uploadToken?.equals(getOtp()) == true) {
                enterPinFragmentErrorMessage.visibility = View.INVISIBLE
                uploadData()
            } else {
                clearInputs()
                enterPinFragmentErrorMessage?.setLocalizedString("invalid_pin")
                enterPinFragmentErrorMessage.visibility = View.VISIBLE
                SmsRetriever.getClient(requireContext()).startSmsUserConsent(null)
            }
        }

        toolbar.setNavigationOnClickListener {
            val fragManager: FragmentManager? = activity?.supportFragmentManager
            fragManager?.popBackStack()
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
        disposeObj?.dispose()

        requireActivity().unregisterReceiver(smsVerificationReceiver)
    }

    fun turnOnLoadingProgress() {
        uploadPageFragmentLoadingProgressBarFrame.visibility = View.VISIBLE
    }

    fun turnOffLoadingProgress() {
        uploadPageFragmentLoadingProgressBarFrame.visibility = View.INVISIBLE
    }

    private fun getUploadToken() = launch {
        CentralLog.d("EnterPinFragment", "Fetching upload token")

        turnOnLoadingProgress()

        val queryParams = java.util.HashMap<String, String>()
        queryParams["userId"] = Preference.getUUID(context)

        val getUploadTokenResponse = Request.runRequest(
                "/adapters/getUploadTokenAdapter/getUploadToken",
                Request.GET,
                queryParams = queryParams
        )
        val token =
                getUploadTokenResponse.data?.get("token") // if the service failed, it could return token as a JSON object with error instead of string: {"token":{"error":"Unable to get the upload token"}}
        if (!getUploadTokenResponse.isSuccess() || getUploadTokenResponse.status != 200 || token == null || token !is String) {
            enterPinFragmentErrorMessage?.setLocalizedString("failed_to_send_pin")
            enterPinFragmentErrorMessage?.visibility = View.VISIBLE

            if (getUploadTokenResponse.status == 401 || token == null) {
                btn_next?.isEnabled = false
            }

        } else {
            uploadToken = token
            btn_next?.isEnabled = true
        }

        turnOffLoadingProgress()
    }

    private fun uploadData() = launch {

        var errorKey: String? = null

        turnOnLoadingProgress()

        val partitionedJson = getPartitionedEncounterJson()

        for (jsonData in partitionedJson) {
            if (jsonData.getJSONArray("records").length() == 0) {
                errorKey = "no_encounter_data_available"
                break
            } else {
                val uploadResponse = uploadJson(jsonData)

                if (!uploadResponse.isSuccess()) {
                    errorKey = "failed_to_upload_data"
                    break
                }
            }
        }

        turnOffLoadingProgress()

        if (errorKey != null) {
            enterPinFragmentErrorMessage?.setLocalizedString(errorKey)
            enterPinFragmentErrorMessage?.visibility = View.VISIBLE
        } else {
            navigateToUploadComplete()
        }

    }

    private fun navigateToUploadComplete() {
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        val uploadCompleteFragment = UploadCompleteFragment()

        fragmentManager?.popBackStack(HomeFragment::class.java.name, 0)
        fragmentTransaction?.replace(R.id.content, uploadCompleteFragment)
        fragmentTransaction?.addToBackStack(UploadCompleteFragment::class.java.name)
        fragmentTransaction?.commit()
    }

    private suspend fun uploadJson(jsonData: JSONObject): Response {
        val queryParams = HashMap<String, String>()
        queryParams["userId"] = Preference.getUUID(context)

        return Request.runRequest(
                "/adapters/uploadData/uploadData",
                Request.POST,
                0,
                data = jsonData,
                queryParams = queryParams
        )
    }

    private suspend fun getPartitionedEncounterJson(): List<JSONObject> =
            withContext(Dispatchers.IO) {
                val partitionedRecords =
                        Lists.partition(
                                StreetPassRecordStorage(TracerApp.AppContext).getAllRecords(),
                                ENCOUNTER_PARTITION_SIZE
                        )

                val statusRecords = StatusRecordStorage(TracerApp.AppContext).getAllRecords()

                partitionedRecords
                        .map { streetPassRecords -> ExportData(streetPassRecords, statusRecords) }
                        .map(::convertToUploadPayload)
            }

    private fun convertToUploadPayload(exportedData: ExportData): JSONObject {
        CentralLog.d(TAG, "records: ${exportedData.recordList}")
        CentralLog.d(TAG, "status: ${exportedData.statusList}")

        val updatedDeviceList = exportedData.recordList.map {
            it.timestamp = it.timestamp / 1000
            return@map it.toJSONObject()
        }

        val updatedStatusList = exportedData.statusList.map {
            it.timestamp = it.timestamp / 1000
            return@map it.toJSONObject()
        }

        val jsonObject = JSONObject()
        arguments?.getParcelable<CovidTestData>(EXTRA_COVID_TEST_DATA)?.let { covidTestData ->
            jsonObject.put("covidTestData", JSONObject().apply {
                put("testDate", covidTestData.testDate / 1000)
                covidTestData.symptomsDate?.let { symptomsDate ->
                    put("symptomsDate", symptomsDate / 1000)
                }
            })
        }
        jsonObject.put("token", uploadToken)
        jsonObject.put("records", JSONArray(updatedDeviceList))
        jsonObject.put("events", JSONArray(updatedStatusList))
        return jsonObject
    }

    private fun clearInputs() {
        otp_et6?.setText("")
        otp_et5?.setText("")
        otp_et4?.setText("")
        otp_et3?.setText("")
        otp_et2?.setText("")
        otp_et1?.setText("")
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

    private fun validateOtp(otp: String): Boolean {
        return otp.length >= OTP_CODE_LENGTH
    }

    private fun getOtpFromMessage(message: String) {
        val startIndex = message.indexOf(OTP_CODE_PREFIX)
        if (message.length >= 8 && startIndex > 0) {
            setOtp(
                    message.substring(
                            startIndex + OTP_CODE_PREFIX.length,
                            startIndex + OTP_CODE_PREFIX.length + OTP_CODE_LENGTH
                    )
            )
        }
    }

    inner class OTPKeyListener : View.OnKeyListener {
        override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
            if (keyCode == KeyEvent.KEYCODE_DEL) {
                val editText = v as AppCompatEditText
                if (editText.selectionStart == 0 && editText.selectionStart == editText.selectionEnd) {
                    v.id.let {
                        val previousBox = getPreviousBox(it)
                        previousBox?.post {
                            previousBox.requestFocus()
                        }
                    }
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
    inner class OTPTextWatcher(val view: EditText) : TextWatcher {

        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            when (view.id) {
                R.id.otp_et1 -> if (text.length == 1) otpInputs[1].requestFocus()
                R.id.otp_et2 -> if (text.length == 1) otpInputs[2].requestFocus() else if (text.isEmpty()) otpInputs[0].requestFocus()
                R.id.otp_et3 -> if (text.length == 1) otpInputs[3].requestFocus() else if (text.isEmpty()) otpInputs[1].requestFocus()
                R.id.otp_et4 -> if (text.length == 1) otpInputs[4].requestFocus() else if (text.isEmpty()) otpInputs[2].requestFocus()
                R.id.otp_et5 -> if (text.length == 1) otpInputs[5].requestFocus() else if (text.isEmpty()) otpInputs[3].requestFocus()
                R.id.otp_et6 -> {
                    if (text.isEmpty()) otpInputs[4].requestFocus()

                    if (otp_et1.text.isNotEmpty() && otp_et2.text.isNotEmpty() && otp_et3.text.isNotEmpty()
                            && otp_et4.text.isNotEmpty() && otp_et5.text.isNotEmpty() && otp_et6.text.isNotEmpty()
                    ) {
                        getUploadToken()
                    }
                }
            }
        }

        override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}

        override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
            if (validateOtp(getOtp())) {
                Utils.hideKeyboardFrom(view.context, view)
                btn_next?.isEnabled = true
                btn_next?.setLocalizedString("upload_button")
            } else {
                btn_next?.isEnabled = false
                btn_next?.setLocalizedString("submit_button")
            }
        }
    }
}
