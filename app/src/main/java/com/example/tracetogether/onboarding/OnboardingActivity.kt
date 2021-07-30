package com.example.tracetogether.onboarding

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.api.ErrorCode
import com.example.tracetogether.api.Request
import com.example.tracetogether.api.auth.SmsCodeChallengeHandler
import com.example.tracetogether.idmanager.TempIDManager
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.services.BluetoothMonitoringService
import com.example.tracetogether.util.Extensions.getLocalizedText
import kotlinx.android.synthetic.main.activity_onboarding.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val TOU_INDEX = 0
private const val DISCLOSURE_INDEX = 1
private const val REGISTER_INDEX = 2
private const val OTP_INDEX = 3
private const val LOCATION_PERMISSION_INDEX = 4
private const val BACKGROUND_PERMISSION_INDEX = 5
private const val SETUP_DONE_INDEX = 6

/*
    Activity used for the Onboarding flow,
    holds most of the functionality for the Onboarding fragments
 */
class OnboardingActivity : FragmentActivity(), CoroutineScope by MainScope(),
    SetupCompleteFragment.OnFragmentInteractionListener,
    RegisterNumberFragment.OnFragmentInteractionListener,
    OTPFragment.OnFragmentInteractionListener,
    TOUFragment.OnFragmentInteractionListener {

    private var TAG: String = "OnboardingActivity"
    private var pagerAdapter: ScreenSlidePagerAdapter? = null
    private var resendingCode = false

    private var mIsResetup = false

    val smsCodeRequiredReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            // Might receive a challenge again if something failed, only navigate to next page if not on otp fragment yet
            if (pager.currentItem == REGISTER_INDEX) {
                navigateToNextPage()
            }
        }
    }
    val smsCodeSuccessReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            navigateToNextPage()
        }
    }
    val smsCodeFailReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            updateOTPError("verification_failed".getLocalizedText())
        }
    }
    val challengeCancelledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            // We cancel the challenge when calling resendCode, so continuing the resendCode flow here
            requestForOTP("", true)
        }
    }

    private fun enableFragmentbutton() {
        val interfaceObject: OnboardingFragmentInterface? = pagerAdapter?.getItem(pager.currentItem)
        interfaceObject?.enableButton()
    }

    private fun alertDialog(desc: String?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder
            .setTitle("alert_title".getLocalizedText())
            .setMessage(desc)
            .setCancelable(false)
            .setPositiveButton("alert_done".getLocalizedText()) { dialog, _ ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun requestTempIdsIfNeeded() {

        CentralLog.d(TAG, "requestTempIdsIfNeeded")

        if (BluetoothMonitoringService.broadcastMessage == null || TempIDManager.needToUpdate(
                applicationContext
            )
        ) {
            getTemporaryID()
        }
    }

    private fun getTemporaryID() {
        TempIDManager.getTemporaryIDs(this)
        CentralLog.d(TAG, "Retrieved Temporary ID successfully")
        //Was causing the SetUpFragment to skip
        //Keeping here incase we need it
        //navigateToNextPage()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter

        tabDots?.setupWithViewPager(pager, true)

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
                CentralLog.d(TAG, "OnPageScrollStateChanged")
            }

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
                CentralLog.d(TAG, "OnPageScrolled")
            }

            override fun onPageSelected(position: Int) {
                CentralLog.d(TAG, "position: $position")
                val onboardingFragment: OnboardingFragmentInterface =
                    pagerAdapter!!.getItem(position)
                onboardingFragment.becomesVisible()
                when (position) {
                    TOU_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    DISCLOSURE_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    REGISTER_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    OTP_INDEX -> {
                        //Cannot put check point at this page without triggering OTP
                    }
                    LOCATION_PERMISSION_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    BACKGROUND_PERMISSION_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    SETUP_DONE_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                }

            }
        })

        //disable swiping
        pager.setPagingEnabled(false)
        pager.offscreenPageLimit = 7

        val extras = intent.extras
        if (extras != null) {
            mIsResetup = true
            var page = extras.getInt("page", TOU_INDEX)
            navigateTo(page)
        } else {
            var checkPoint = Preference.getCheckpoint(this)
            navigateTo(checkPoint)
        }

        // We need to register for broadcast events coming from /auth/SmsCodeChallengeHandler
        registerForSmsCodeChallengeBroadcasts()
    }

    private fun registerForSmsCodeChallengeBroadcasts() {
        val broadcastManager = LocalBroadcastManager.getInstance(this)

        broadcastManager.registerReceiver(
            smsCodeRequiredReceiver,
            IntentFilter(SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE_REQUIRED)
        )
        broadcastManager.registerReceiver(
            smsCodeSuccessReceiver,
            IntentFilter(SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE_SUCCESS)
        )
        broadcastManager.registerReceiver(
            smsCodeFailReceiver,
            IntentFilter(SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE_FAIL)
        )
        broadcastManager.registerReceiver(
            challengeCancelledReceiver,
            IntentFilter(SmsCodeChallengeHandler.ACTION_CHALLENGE_CANCELLED)
        )
    }

    override fun onBackPressed() {
        if (pager.currentItem > REGISTER_INDEX && pager.currentItem != TOU_INDEX) {
            if (pager.currentItem == OTP_INDEX) {
                cancelChallengeAndGoBack()
            } else {
                navigateToPreviousPage()
            }
            return
        }
        super.onBackPressed()
    }

    override fun onDestroy() {
        val broadcastManager = LocalBroadcastManager.getInstance(this)
        broadcastManager.unregisterReceiver(smsCodeRequiredReceiver)
        broadcastManager.unregisterReceiver(smsCodeSuccessReceiver)
        broadcastManager.unregisterReceiver(smsCodeFailReceiver)
        broadcastManager.unregisterReceiver(challengeCancelledReceiver)
        cancel()
        super.onDestroy()
    }

    fun cancelChallengeAndGoBack() {
        // We are on the otp screen and waiting for the user to verify the otp (answer the challenge), need to cancel the challenge
        val intent = Intent()
        intent.action = SmsCodeChallengeHandler.ACTION_CANCEL_CHALLENGE
        intent.putExtra(SmsCodeChallengeHandler.EXTRA_SKIP_CANCELLED_BROADCAST, true)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        navigateToPreviousPage()
    }

    fun navigateToNextPage() {
        CentralLog.d(TAG, "Navigating to next page")
        pager.currentItem = pager.currentItem + 1
        pagerAdapter!!.notifyDataSetChanged()
    }

    fun navigateToPreviousPage() {
        CentralLog.d(TAG, "Navigating to previous page")
        if (mIsResetup) {
            if (pager.currentItem >= 4) {
                pager.currentItem = pager.currentItem - 1
                pagerAdapter!!.notifyDataSetChanged()
            } else {
                finish()
            }
        } else {
            if (pager.currentItem == LOCATION_PERMISSION_INDEX) {
                finish()
            }
            else {
                pager.currentItem = pager.currentItem - 1
                pagerAdapter!!.notifyDataSetChanged()
            }
        }
    }

    private fun navigateTo(page: Int) {
        CentralLog.d(TAG, "Navigating to page")
        pager.currentItem = page
        pagerAdapter!!.notifyDataSetChanged()
    }

    fun requestForOTP(phoneNumber: String, skipRegister: Boolean = false) = launch {

        resendingCode = false

        var continueToOtp = true
        if (!skipRegister) {
            // 1. Register the phone number
            val authResponse = Request.runRequest(
                "/adapters/smsOtpService/phone/register/${phoneNumber}",
                Request.POST
            )

            if (authResponse.status != 200) {
                continueToOtp = false
                updatePhoneNumberError(authResponse.error)
                enableFragmentbutton()
            }
        }

        if (continueToOtp) {
            // 2. Call to trigger sms otp, this will trigger an sms challenge (See /auth/SmsCodeChallengeHandler)
            // Also see registerForSmsCodeChallengeBroadcasts function that handles the broadcasts received
            val triggerSmsOTPResponse = Request.runRequest(
                "/adapters/smsOtpService/phone/verifySmsOTP",
                Request.POST,
                data = JSONObject()
            )

            if (!triggerSmsOTPResponse.isSuccess() || triggerSmsOTPResponse.status != 200 || triggerSmsOTPResponse.data == null) {
                // No need to show an error for CHALLENGE_HANDLING_CANCELED, it means we cancelled the challenge in progress (via otp screen by either pressing back or click on "wrong number?")
                if (triggerSmsOTPResponse.errorCode != ErrorCode.CHALLENGE_HANDLING_CANCELED) {
                    updateOTPError("onboarding_otp_incorrect".getLocalizedText())
                }
                enableFragmentbutton()
            } else {
                try {
                    Preference.putUUID(
                        applicationContext,
                        triggerSmsOTPResponse.data.getString("userId")
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                requestTempIdsIfNeeded()
            }
        }
    }

    fun validateOTP(otp: String) {

        if (TextUtils.isEmpty(otp) || otp.length < 6) {
            updateOTPError("must_be_six_digit".getLocalizedText())
            return
        }
        val intent = Intent()
        intent.action = SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE
        intent.putExtra("otp", otp)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    fun resendCode(skipCancelChallenge: Boolean = false) {

        resendingCode = true

        if (!skipCancelChallenge) {
            val intent = Intent()
            intent.action = SmsCodeChallengeHandler.ACTION_CANCEL_CHALLENGE
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
            // See registerForSmsCodeChallengeBroadcasts function that handles the broadcasts received: ACTION_CHALLENGE_CANCELLED
        } else {
            requestForOTP("", true)
        }
    }

    fun updatePhoneNumber(num: String) {
        val onboardingFragment: OnboardingFragmentInterface = pagerAdapter!!.getItem(OTP_INDEX)
        onboardingFragment.onUpdatePhoneNumber(num)
    }

    fun updatePhoneNumberError(error: String) {
        val registerNumberFragment: OnboardingFragmentInterface =
            pagerAdapter!!.getItem(REGISTER_INDEX)
        registerNumberFragment.onError(error)
    }

    private fun updateOTPError(error: String) {
        val onboardingFragment: OnboardingFragmentInterface = pagerAdapter!!.getItem(OTP_INDEX)
        onboardingFragment.onError(error)
        alertDialog(error)
    }

    override fun onFragmentInteraction(uri: Uri) {
        CentralLog.d(TAG, "########## fragment interaction: $uri")
    }

    private inner class ScreenSlidePagerAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm) {

        val fragmentMap: MutableMap<Int, OnboardingFragmentInterface> = HashMap()

        override fun getCount(): Int = 7

        override fun getItem(position: Int): OnboardingFragmentInterface {
            return fragmentMap.getOrPut(position, { createFragAtIndex(position) })
        }

        private fun createFragAtIndex(index: Int): OnboardingFragmentInterface {
            return when (index) {
                TOU_INDEX -> return TOUFragment()
                DISCLOSURE_INDEX -> return InAppDisclosureFragment()
                REGISTER_INDEX -> return RegisterNumberFragment()
                OTP_INDEX -> return OTPFragment()
                LOCATION_PERMISSION_INDEX -> return LocationPermissionFragment()
                BACKGROUND_PERMISSION_INDEX -> return BackgroundPermissionFragment()
                SETUP_DONE_INDEX -> return SetupCompleteFragment()
                else -> {
                    TOUFragment()
                }
            }
        }

    }

}
