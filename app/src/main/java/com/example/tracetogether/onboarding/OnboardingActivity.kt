package com.example.tracetogether.onboarding

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager.widget.ViewPager
import kotlinx.android.synthetic.main.activity_onboarding.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import com.example.tracetogether.Preference
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.api.ErrorCode
import com.example.tracetogether.api.Request
import com.example.tracetogether.api.auth.SmsCodeChallengeHandler
import com.example.tracetogether.idmanager.TempIDManager
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.services.BluetoothMonitoringService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.json.JSONObject

private const val REQUEST_ENABLE_BT = 123
private const val PERMISSION_REQUEST_ACCESS_LOCATION = 456
private const val BATTERY_OPTIMISER = 789

private const val TOU_INDEX = 0
private const val REGISTER_INDEX = 1
private const val OTP_INDEX = 2
private const val SETUP_INDEX = 3
private const val SETUP_DONE_INDEX = 4

/*
    Activity used for the Onboarding flow,
    holds most of the functionality for the Onboarding fragments
 */
class OnboardingActivity : FragmentActivity(), CoroutineScope by MainScope(),
    SetupFragment.OnFragmentInteractionListener,
    SetupCompleteFragment.OnFragmentInteractionListener,
    RegisterNumberFragment.OnFragmentInteractionListener,
    OTPFragment.OnFragmentInteractionListener,
    TOUFragment.OnFragmentInteractionListener {

    private var TAG: String = "OnboardingActivity"
    private var pagerAdapter: ScreenSlidePagerAdapter? = null
    private var bleSupported = false
    private var resendingCode = false

    private fun enableFragmentbutton() {
        var interfaceObject: OnboardingFragmentInterface? = pagerAdapter?.getItem(pager.currentItem)
        interfaceObject?.enableButton()
    }

    private fun alertDialog(desc: String?) {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage(desc)
            .setCancelable(false)
            .setPositiveButton(
                getString(R.string.ok),
                DialogInterface.OnClickListener { dialog, id ->
                    dialog.dismiss()
                })

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

    private var mIsOpenSetting = false
    private var mIsResetup = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)
        pagerAdapter = ScreenSlidePagerAdapter(supportFragmentManager)
        pager.adapter = pagerAdapter

        tabDots.setupWithViewPager(pager, true)

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
                    REGISTER_INDEX -> {
                        Preference.putCheckpoint(
                            baseContext,
                            position
                        )
                    }
                    OTP_INDEX -> {
                        //Cannot put check point at this page without triggering OTP
                    }
                    SETUP_INDEX -> {
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
        pager.offscreenPageLimit = 5

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

        //Receive "verify sms code required"
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                // Might receive a challenge again if something failed, only navigate to next page if not on otp fragment yet
                if (pager.currentItem == REGISTER_INDEX) {
                    navigateToNextPage()
                }
            }
        }, IntentFilter(SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE_REQUIRED))

        //Receive "sms code verified"
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                navigateToNextPage()
            }
        }, IntentFilter(SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE_SUCCESS))

        //Receive "sms code failed to verify"
        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                updateOTPError(getString(R.string.verification_failed))
            }
        }, IntentFilter(SmsCodeChallengeHandler.ACTION_VERIFY_SMS_CODE_FAIL))

        broadcastManager.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // We cancel the challenge when calling resendCode, so continuing the resendCode flow here
                requestForOTP("", true)
            }
        }, IntentFilter(SmsCodeChallengeHandler.ACTION_CHALLENGE_CANCELLED))
    }

    override fun onResume() {
        super.onResume()
        if (mIsOpenSetting) {
            Handler().postDelayed(Runnable { setupPermissionsAndSettings() }, 1000)
        }
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

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    fun enableBluetooth() {
        CentralLog.d(TAG, "[enableBluetooth]")
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.let {
            if (it.isDisabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(
                    enableBtIntent,
                    REQUEST_ENABLE_BT
                )
            } else {
                setupPermissionsAndSettings()
            }
        }
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_ACCESS_LOCATION)
    fun setupPermissionsAndSettings() {
        CentralLog.d(TAG, "[setupPermissionsAndSettings]")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            var perms = Utils.getRequiredPermissions()

            if (EasyPermissions.hasPermissions(this, *perms)) {
                // Already have permission, do the thing
                initBluetooth()
                excludeFromBatteryOptimization()
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(
                    this, getString(R.string.permission_location_rationale),
                    PERMISSION_REQUEST_ACCESS_LOCATION, *perms
                )
            }
        } else {
            initBluetooth()
            navigateToNextPage()
        }
    }

    private fun initBluetooth() {
        checkBLESupport()
    }

    private fun checkBLESupport() {
        CentralLog.d(TAG, "[checkBLESupport] ")
        if (!BluetoothAdapter.getDefaultAdapter().isMultipleAdvertisementSupported) {
            bleSupported = false
            Utils.stopBluetoothMonitoringService(this)
        } else {
            bleSupported = true
        }
    }

    private fun excludeFromBatteryOptimization() {
        CentralLog.d(TAG, "[excludeFromBatteryOptimization] ")
        val powerManager =
            this.getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        val packageName = this.packageName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val intent =
                Utils.getBatteryOptimizerExemptionIntent(
                    packageName
                )

            if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
                CentralLog.d(TAG, "Not on Battery Optimization whitelist")
                //check if there's any activity that can handle this
                if (Utils.canHandleIntent(
                        intent,
                        packageManager
                    )
                ) {
                    this.startActivityForResult(
                        intent,
                        BATTERY_OPTIMISER
                    )
                } else {
                    //no way of handling battery optimizer
                    navigateToNextPage()
                }
            } else {
                CentralLog.d(TAG, "On Battery Optimization whitelist")
                navigateToNextPage()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        CentralLog.d(TAG, "requestCode $requestCode resultCode $resultCode")
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                finish()
                return
            } else {
                setupPermissionsAndSettings()
            }
        } else if (requestCode == BATTERY_OPTIMISER) {
            if (resultCode != Activity.RESULT_CANCELED) {
                Handler().postDelayed({
                    navigateToNextPage()
                }, 1000)
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLog.d(TAG, "[onRequestPermissionsResult] requestCode $requestCode")
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_LOCATION -> {
                for (x in 0 until permissions.size) {
                    var permission = permissions[x]
                    if (grantResults[x] == PackageManager.PERMISSION_DENIED) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            var showRationale = shouldShowRequestPermissionRationale(permission)
                            if (!showRationale) {

                                // build alert dialog
                                val dialogBuilder = AlertDialog.Builder(this)
                                // set message of alert dialog
                                dialogBuilder.setMessage(getString(R.string.open_location_setting))
                                    // if the dialog is cancelable
                                    .setCancelable(false)
                                    // positive button text and action
                                    .setPositiveButton(
                                        getString(R.string.ok),
                                        DialogInterface.OnClickListener { dialog, id ->
                                            CentralLog.d(TAG, "user also CHECKED never ask again")
                                            mIsOpenSetting = true
                                            var intent =
                                                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            var uri: Uri =
                                                Uri.fromParts("package", packageName, null)
                                            intent.data = uri
                                            startActivity(intent)

                                        })
                                    // negative button text and action
                                    .setNegativeButton(
                                        getString(R.string.cancel),
                                        DialogInterface.OnClickListener { dialog, id ->
                                            dialog.cancel()
                                        })

                                // create dialog box
                                val alert = dialogBuilder.create()

                                // show alert dialog
                                alert.show()

                            } else if (Manifest.permission.WRITE_CONTACTS.equals(permission)) {
                                CentralLog.d(TAG, "user did not CHECKED never ask again")
                            } else {
                                excludeFromBatteryOptimization()
                            }
                        }
                    } else if (grantResults[x] == PackageManager.PERMISSION_GRANTED) {
                        excludeFromBatteryOptimization()
                    }
                }
            }
        }
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
            pager.currentItem = pager.currentItem - 1
            pagerAdapter!!.notifyDataSetChanged()
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
            val authResponse = Request.runRequest("/adapters/smsOtpService/phone/register/${phoneNumber}", Request.POST)


            if (authResponse.status != 200) {
                continueToOtp = false
                updatePhoneNumberError(authResponse.error)
                enableFragmentbutton()
            }
        }

        if (continueToOtp) {
            // 2. Call to trigger sms otp, this will trigger an sms challenge (See /auth/SmsCodeChallengeHandler)
            // Also see registerForSmsCodeChallengeBroadcasts function that handles the broadcasts received
            val triggerSmsOTPResponse = Request.runRequest("/adapters/smsOtpService/phone/verifySmsOTP", Request.POST, data = JSONObject())

            if (!triggerSmsOTPResponse.isSuccess() || triggerSmsOTPResponse.status != 200 || triggerSmsOTPResponse.data == null) {
                // No need to show an error for CHALLENGE_HANDLING_CANCELED, it means we cancelled the challenge in progress (via otp screen by either pressing back or click on "wrong number?")
                if (triggerSmsOTPResponse.errorCode != ErrorCode.CHALLENGE_HANDLING_CANCELED) {
                    updateOTPError(getString(R.string.invalid_otp))
                }
                enableFragmentbutton()
            } else {
                try {
                    Preference.putUUID(applicationContext, triggerSmsOTPResponse.data.getString("userId"))
                } catch (e: Exception) {
                    e.printStackTrace()
                }
                requestTempIdsIfNeeded()
            }
        }
    }

    fun validateOTP(otp: String) {

        if (TextUtils.isEmpty(otp) || otp.length < 6) {
            updateOTPError(getString(R.string.must_be_six_digit))
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
        val registerNumberFragment: OnboardingFragmentInterface = pagerAdapter!!.getItem(REGISTER_INDEX)
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

        override fun getCount(): Int = 5

        override fun getItem(position: Int): OnboardingFragmentInterface {
            return fragmentMap.getOrPut(position, { createFragAtIndex(position) })
        }

        private fun createFragAtIndex(index: Int): OnboardingFragmentInterface {
            return when (index) {
                TOU_INDEX -> return TOUFragment()
                REGISTER_INDEX -> return RegisterNumberFragment()
                OTP_INDEX -> return OTPFragment()
                SETUP_INDEX -> return SetupFragment()
                SETUP_DONE_INDEX -> return SetupCompleteFragment()
                else -> {
                    TOUFragment()
                }
            }
        }

    }

}
