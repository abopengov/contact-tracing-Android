package com.example.tracetogether.fragment

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import com.example.tracetogether.*
import com.example.tracetogether.PeekActivity
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.statusBarHeight
import com.example.tracetogether.util.Extensions.underline
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.fragment_webview.*
import kotlinx.android.synthetic.main.view_allow_location_permission.*
import kotlinx.android.synthetic.main.view_turn_bluetooth_on.*
import kotlinx.android.synthetic.main.view_turn_location_on.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import pub.devrel.easypermissions.EasyPermissions


private const val REQUEST_ENABLE_BT = 123
private const val PERMISSION_REQUEST_ACCESS_LOCATION = 456

/*
    Fragment for the Home Screen
 */
class HomeFragment : Fragment(), CoroutineScope by MainScope() {
    private val TAG = "HomeFragment"

    private var mIsBroadcastListenerRegistered = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extendViewToStatusBar()
        setUpView()

        showSetup()

        tv_learn_how_it_works?.setOnClickListener { goToHelp() }
        banner_case_highlights?.setOnClickListener { goStatistics() }
        iv_upload?.setOnClickListener { goToUpload() }
        tv_upload_data_title?.setOnClickListener { goToUpload() }
        btn_share?.setOnClickListener { shareThisApp() }
        btn_bluetooth_settings.setOnClickListener { goToBluetoothSettings() }
        btn_location_settings.setOnClickListener { goToLocationSettings() }
        btn_location_permission_settings.setOnClickListener { goToAppSettings() }
        btn_push_settings?.setOnClickListener { goToAppSettings() }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val showDebugScreen = {
            if (BuildConfig.DEBUG) {
                var intent = Intent(context, PeekActivity::class.java)
                context?.startActivity(intent)
            }
        }

        iv_checkmark?.setOnClickListener {
            showDebugScreen()
        }

        iv_red_x?.setOnClickListener {
            showDebugScreen()
        }
    }

    fun showSetup() {
        updatedPremLabels()
        updateAppWorking()
    }

    override fun onResume() {
        super.onResume()
        if (!mIsBroadcastListenerRegistered) {
            // bluetooth on/off
            var f = IntentFilter()
            f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            f.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            activity!!.registerReceiver(mBroadcastListener, f)
            mIsBroadcastListenerRegistered = true
        }

        showSetup()
    }

    override fun onDestroyView() {
        retractViewFromStatusBar()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        if (mIsBroadcastListenerRegistered) {
            activity!!.unregisterReceiver(mBroadcastListener)
            mIsBroadcastListenerRegistered = false
        }
    }

    private fun shareThisApp() {
        val newIntent = Intent(Intent.ACTION_SEND)
        newIntent.type = "text/plain"
        newIntent.putExtra(
            Intent.EXTRA_SUBJECT,
            requireContext().applicationInfo.loadLabel(requireContext().packageManager).toString()
        )
        val shareMessage = "share_message".getLocalizedText() + BuildConfig.SHARE_URL
        newIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(newIntent, "Choose one"))
    }

    private val mBroadcastListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            CentralLog.d(TAG, intent.action.toString())
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                if (state == BluetoothAdapter.STATE_OFF) {
                    iv_bluetooth?.isSelected = false
                } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    iv_bluetooth?.isSelected = false
                } else if (state == BluetoothAdapter.STATE_ON) {
                    iv_bluetooth?.isSelected = true
                }

                showSetup()
            }
            if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                showSetup()
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
            (activity as MainActivity).getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            iv_bluetooth?.isSelected = resultCode == Activity.RESULT_OK
        }
        showSetup()
        super.onActivityResult(requestCode, resultCode, data)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        CentralLog.d(TAG, "[onRequestPermissionsResult]requestCode $requestCode")
        when (requestCode) {
            PERMISSION_REQUEST_ACCESS_LOCATION -> {
                iv_location?.isSelected = permissions.isNotEmpty()
            }
        }

        showSetup()
    }

    private fun updatedPremLabels() {
        val enabled = "enabled".getLocalizedText()
        val disabled = "disabled".getLocalizedText()

        bluetoothAdapter?.let { bluetoothStatus ->
            iv_bluetooth.isSelected = bluetoothStatus.isEnabled
            tv_bluetooth_status?.let {
                tv_bluetooth_status.text = if (bluetoothStatus.isEnabled) enabled else disabled
            }
            view_turn_bluetooth_on.visibility =
                if (bluetoothStatus.isEnabled) View.GONE else View.VISIBLE
        }

        val locationAvailable = hasLocationPermission() && isLocationEnabled()
        iv_location?.isSelected = locationAvailable
        tv_location_status?.text = if (locationAvailable) enabled else disabled
        view_turn_location_on.visibility = if (isLocationEnabled()) View.GONE else View.VISIBLE
        view_allow_location_permission.visibility =
            if (hasLocationPermission()) View.GONE else View.VISIBLE

        val pushNotificationsEnabled =
            NotificationManagerCompat.from(activity as MainActivity).areNotificationsEnabled()
        iv_push?.isSelected = pushNotificationsEnabled
        tv_push_status?.text = if (pushNotificationsEnabled) enabled else disabled

        togglePushPermissionInstructions(pushNotificationsEnabled)
    }

    private fun updateAppWorking() {
        val appWorking = iv_location.isSelected && iv_bluetooth.isSelected
        app_is_working_container.visibility = if (appWorking) View.VISIBLE else View.GONE
        app_is_not_working_container.visibility = if (appWorking) View.GONE else View.VISIBLE
    }

    private fun goToHelp() {
        val activity = activity as MainActivity
        activity?.goToHelp()
    }

    private fun goStatistics() {
        val activity = activity as MainActivity
        activity?.goToStatistics()
    }

    private fun goToUpload() {
        val fragment = ForUseFragment()
        addFragmentToBackStack(fragment)
        retractViewFromStatusBar()
    }

    private fun setUpView() {
        tv_app_is_working_title?.setLocalizedString("home_app_is_working")
        tv_app_is_not_working_title?.setLocalizedString("home_app_is_not_working")

        tv_bluetooth_instructions_title?.setLocalizedString("home_turn_on_bluetooth_title")
        tv_bluetooth_instructions_step1?.setLocalizedString("home_turn_on_bluetooth_step1_android")
        tv_bluetooth_instructions_step2?.setLocalizedString("home_turn_on_bluetooth_step2_android")
        tv_bluetooth_instructions_step3?.setLocalizedString("home_turn_on_bluetooth_step3_android")
        btn_bluetooth_settings.setLocalizedString("goto_settings")

        tv_location_instructions_title?.setLocalizedString("home_turn_on_location_title")
        tv_location_instructions_step1?.setLocalizedString("home_turn_on_location_step1_android")
        tv_location_instructions_step2?.setLocalizedString("home_turn_on_location_step2_android")
        tv_location_instructions_step3?.setLocalizedString("home_turn_on_location_step3_android")
        btn_location_settings.setLocalizedString("goto_settings")

        tv_location_permission_instructions_title?.setLocalizedString("home_location_permission_title")
        tv_location_permission_instructions_step1?.setLocalizedString("home_location_permission_step1_android")
        tv_location_permission_instructions_step2?.setLocalizedString("home_location_permission_step2_android")
        tv_location_permission_instructions_step3?.setLocalizedString("home_location_permission_step3_android")
        tv_location_permission_instructions_step4?.setLocalizedString("home_location_permission_step4_android")
        btn_location_permission_settings.setLocalizedString("goto_app_settings")

        tv_learn_how_it_works?.setLocalizedString("home_learn_how_it_works")
        tv_learn_how_it_works?.underline()

        tv_push_instructions_step1?.setLocalizedString("home_notification_permission_step1_android")
        tv_push_instructions_step2?.setLocalizedString("home_notification_permission_step2_android")
        tv_push_instructions_step3?.setLocalizedString("home_notification_permission_step3_android")
        btn_push_settings?.setLocalizedString("goto_app_settings")

        tv_case_highlight_title?.setLocalizedString("home_case_highlight")
        tv_case_highlight_content?.setLocalizedString("home_case_highlight_content")
        btn_share?.setLocalizedString("home_shared_app")
        tv_upload_data_title?.setLocalizedString("home_upload_data")
        tv_upload_data_title?.underline()
        tv_upload_data_content?.setLocalizedString("home_upload_data_content")


        val versionLabel = "app_version_label".getLocalizedText() + BuildConfig.VERSION_NAME + Utils.getVersionSuffix()
        tv_app_version?.text = versionLabel

        tv_title?.setLocalizedString("app_permission_status")
        tv_bluetooth?.setLocalizedString("home_bluetooth_permission")
        tv_location?.setLocalizedString("home_location_permission")
        tv_push?.setLocalizedString("home_notification_permission")
        tvErrorWebView?.setLocalizedString("failed_to_load_data")

        app_working_buffer_view?.layoutParams?.height = statusBarHeight()
        app_not_working_buffer_view?.layoutParams?.height = statusBarHeight()
    }

    private fun addFragmentToBackStack(fragment: Fragment) {
        val fragmentManager: FragmentManager? = activity?.supportFragmentManager
        val fragmentTransaction: FragmentTransaction? = fragmentManager?.beginTransaction()
        fragmentTransaction?.replace(R.id.content, fragment)
        fragmentTransaction?.addToBackStack(fragment::class.java.name)
        fragmentTransaction?.commit()
    }

    private fun extendViewToStatusBar() {
        this.activity?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
            decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            statusBarColor = ContextCompat.getColor(requireContext(), R.color.semi_transparent)
        }
    }

    private fun retractViewFromStatusBar() {
        this.activity?.window?.apply {
            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
            decorView.systemUiVisibility = 0
            statusBarColor = Color.BLACK
        }
    }

    private fun togglePushPermissionInstructions(pushNotificationsEnabled: Boolean) {
        push_not_working_divider1.visibility =
            if (pushNotificationsEnabled) View.GONE else View.VISIBLE
        view_push_setup_steps.visibility = if (pushNotificationsEnabled) View.GONE else View.VISIBLE
        push_card_view.setBackgroundColor(
            ContextCompat.getColor(
                requireContext(),
                if (pushNotificationsEnabled) R.color.white else R.color.light_pink
            )
        )
    }

    private fun hasLocationPermission(): Boolean {
        val perms = Utils.getRequiredPermissions()
        return EasyPermissions.hasPermissions(activity as MainActivity, *perms)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
            requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun goToBluetoothSettings() {
        startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS))
    }

    private fun goToLocationSettings() {
        startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    }

    private fun goToAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri: Uri = Uri.fromParts("package", requireContext().packageName, null)
        intent.data = uri
        startActivity(intent)
    }
}

