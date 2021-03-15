package com.example.tracetogether.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.TracerApp
import com.example.tracetogether.Utils
import com.example.tracetogether.api.Request
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.status.persistence.StatusRecord
import com.example.tracetogether.status.persistence.StatusRecordStorage
import com.example.tracetogether.streetpass.persistence.StreetPassRecord
import com.example.tracetogether.streetpass.persistence.StreetPassRecordStorage
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.toJSONObject
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.fragment_upload_enterpin.*
import kotlinx.android.synthetic.main.fragment_upload_enterpin.otp_et1
import kotlinx.android.synthetic.main.fragment_upload_enterpin.otp_et2
import kotlinx.android.synthetic.main.fragment_upload_enterpin.otp_et3
import kotlinx.android.synthetic.main.fragment_upload_enterpin.otp_et4
import kotlinx.android.synthetic.main.fragment_upload_enterpin.otp_et5
import kotlinx.android.synthetic.main.fragment_upload_enterpin.otp_et6
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/*
    Fragment for OTP entry for the Upload flow,
    Handles retrieving upload token,
    retrieving encounter log from storage,
    and encounter log upload
 */
class EnterPinFragment(private val isFirstStep: Boolean) : Fragment(),
        CoroutineScope by MainScope() {

    companion object {
        const val TAG = "UploadFragment"
        const val TEMP_UPLOAD_FILE_NAME = "StreetPassRecord.json"
    }

    private var disposeObj: Disposable? = null

    private var uploadToken: String? = null
    private var otpInputs: MutableList<EditText> = mutableListOf()

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
        enterPinActionButton?.isEnabled = false

        tv_title?.setLocalizedString("enter_pin_title")
        tv_desc?.setLocalizedString("enter_pin_desc")
        enterPinFragmentErrorMessage?.setLocalizedString("invalid_pin")
        enterPinButtonText?.setLocalizedString("upload_button")

        otpInputs.add(otp_et1)
        otpInputs.add(otp_et2)
        otpInputs.add(otp_et3)
        otpInputs.add(otp_et4)
        otpInputs.add(otp_et5)
        otpInputs.add(otp_et6)


        otp_et1?.addTextChangedListener(OTPTextWatcher(otp_et1));
        otp_et2?.addTextChangedListener(OTPTextWatcher(otp_et2));
        otp_et3?.addTextChangedListener(OTPTextWatcher(otp_et3));
        otp_et4?.addTextChangedListener(OTPTextWatcher(otp_et4));
        otp_et5?.addTextChangedListener(OTPTextWatcher(otp_et5));
        otp_et6?.addTextChangedListener(OTPTextWatcher(otp_et6));

        enterPinActionButton?.setOnClickListener {
            if (uploadToken?.equals(getOtp()) == true) {
                enterPinFragmentErrorMessage.visibility = View.INVISIBLE
                uploadData()
            } else {
                clearInputs()
                enterPinFragmentErrorMessage?.setLocalizedString("invalid_pin")
                enterPinFragmentErrorMessage.visibility = View.VISIBLE
            }
        }

        enterPinBackButton?.setOnClickListener {
            var myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)

            if (isFirstStep) {
                myParentFragment.navigateToOTCFragment()
            } else {
                myParentFragment.popStack()
            }
        }
    }

    override fun onDestroy() {
        cancel()
        super.onDestroy()
        disposeObj?.dispose()
    }

    private fun getUploadToken() = launch {
        CentralLog.d("EnterPinFragment", "Fetching upload token")

        val myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
        myParentFragment.turnOnLoadingProgress()

        val queryParams = java.util.HashMap<String, String>()
        queryParams["userId"] = Preference.getUUID(context)

        val getUploadTokenResponse = Request.runRequest(
                "/adapters/getUploadTokenAdapter/getUploadToken",
                Request.GET,
                queryParams = queryParams
        )
        val token =
                getUploadTokenResponse.data?.get("token") // if the service failed, it could return token as a JSON object with error instead of string: {"token":{"error":"Unable to get the upload token"}}
        if (!getUploadTokenResponse.isSuccess() || getUploadTokenResponse.status != 200 || token is JSONObject || token == null) {
            enterPinFragmentErrorMessage?.setLocalizedString("failed_to_send_pin")
            enterPinFragmentErrorMessage?.visibility = View.VISIBLE

            if (getUploadTokenResponse.status == 401 || token == null) {
                enterPinActionButton?.isEnabled = false
            }

        } else {
            uploadToken = token as String
            enterPinActionButton?.isEnabled = true
        }

        myParentFragment.turnOffLoadingProgress()
    }

    private fun uploadData() = launch {

        val myParentFragment: UploadPageFragment = (parentFragment as UploadPageFragment)
        myParentFragment.turnOnLoadingProgress()

        val jsonData = getEncounterJSON()

        if (jsonData.length() == 0) {
            enterPinFragmentErrorMessage?.setLocalizedString("no_encounter_data_available")
            enterPinFragmentErrorMessage?.visibility = View.VISIBLE
        } else {
            val queryParams = java.util.HashMap<String, String>()
            queryParams["userId"] = Preference.getUUID(context)

            val uploadResponse = Request.runRequest(
                    "/adapters/uploadData/uploadData",
                    Request.POST,
                    0,
                    data = jsonData,
                    queryParams = queryParams
            )
            myParentFragment.turnOffLoadingProgress()
            if (!uploadResponse.isSuccess()) {
                enterPinFragmentErrorMessage?.setLocalizedString("failed_to_upload_data")
                enterPinFragmentErrorMessage?.visibility = View.VISIBLE
            } else {
                // Successfully uploaded data, delete the temp file
                // Note: leaving the temp file code here in case it's decided to make use of IBM JSONStorage sdk to upload a file
//                try {
//                    File(context?.filesDir, TEMP_UPLOAD_FILE_NAME).delete()
//                    CentralLog.i(TAG, "Upload file deleted")
//                } catch (e: Exception) {
//                    CentralLog.e(TAG, "Failed to delete upload file")
//                }
                myParentFragment.navigateToUploadComplete()
            }
        }
    }

    private suspend fun getEncounterJSON(): JSONObject =
            suspendCoroutine { cont ->
                var observableStreetRecords = Observable.create<List<StreetPassRecord>> {
                    val result = StreetPassRecordStorage(TracerApp.AppContext).getAllRecords()
                    it.onNext(result)
                }
                var observableStatusRecords = Observable.create<List<StatusRecord>> {
                    val result = StatusRecordStorage(TracerApp.AppContext).getAllRecords()
                    it.onNext(result)
                }

                disposeObj = Observable.zip(observableStreetRecords, observableStatusRecords,

                        BiFunction<List<StreetPassRecord>, List<StatusRecord>, ExportData> { records, status ->
                            ExportData(
                                    records,
                                    status
                            )
                        }

                )
                        .subscribeOn(Schedulers.io())
                        .subscribe { exportedData ->
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
                            jsonObject.put("token", uploadToken)
                            jsonObject.put("records", JSONArray(updatedDeviceList))
                            jsonObject.put("events", JSONArray(updatedStatusList))
//                        Note: leaving the temp file code here in case it's decided to make use of IBM JSONStorage sdk to upload a file
//                        context?.openFileOutput(TEMP_UPLOAD_FILE_NAME, Context.MODE_PRIVATE).use {
//                            it?.write(mapString.toByteArray())
//                        }
//
//                        CentralLog.i(
//                            TAG,
//                            "File $TEMP_UPLOAD_FILE_NAME written to internal storage."
//                        )
                            cont.resume(jsonObject)
                        }
            }

    private fun clearInputs() {
        otp_et6?.setText("")
        otp_et5?.setText("")
        otp_et4?.setText("")
        otp_et3?.setText("")
        otp_et2?.setText("")
        otp_et1?.setText("")
    }

    private fun getOtp(): String {
        var otp = ""
        for (input in otpInputs) {
            otp += input.text
        }
        return otp
    }

    private fun validateOtp(otp: String): Boolean {
        return otp.length >= 6
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
                enterPinActionButton?.isEnabled = true
                enterPinButtonText?.setLocalizedString("upload_button")
            } else {
                enterPinActionButton?.isEnabled = false
                enterPinButtonText?.setLocalizedString("submit_button")
            }
        }

    }
}
