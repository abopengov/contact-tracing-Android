package com.example.tracetogether.home

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
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.tracetogether.*
import com.example.tracetogether.fragment.ForUseFragment
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.network.HomeStats
import com.example.tracetogether.pause.PauseDialogFragment
import com.example.tracetogether.pause.PauseScheduler
import com.example.tracetogether.stats.CovidCasesFragment
import com.example.tracetogether.stats.StatsMapFragment
import com.example.tracetogether.stats.VaccinationsFragment
import com.example.tracetogether.util.AppConstants
import com.example.tracetogether.util.Extensions.formatTime
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.statusBarHeight
import com.example.tracetogether.util.Extensions.underline
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.view_allow_location_permission.*
import kotlinx.android.synthetic.main.view_guidance_banner.*
import kotlinx.android.synthetic.main.view_home_stats.*
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

    private val homeViewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        extendViewToStatusBar()
        setUpView()

        setupHomeScreen()

        tv_learn_how_it_works?.setOnClickListener { goToLearnTab() }
        tv_resume_detection?.setOnClickListener { resumeDetection() }
        iv_upload?.setOnClickListener { goToUpload() }
        tv_upload_data_title?.setOnClickListener { goToUpload() }
        tv_pause_title?.setOnClickListener { gotoPauseSchedule() }
        banner_guidance?.setOnClickListener { gotoExternalGuidance() }
        banner_share_app?.setOnClickListener { shareThisApp() }
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
                val intent = Intent(context, PeekActivity::class.java)
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

    override fun onResume() {
        super.onResume()
        if (!mIsBroadcastListenerRegistered) {
            // bluetooth on/off
            val f = IntentFilter()
            f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            f.addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
            requireActivity().registerReceiver(mBroadcastListener, f)
            mIsBroadcastListenerRegistered = true
        }

        PauseScheduler.pauseToggled = {
            requireActivity().runOnUiThread {
                setupHomeScreen()
            }
        }

        setupHomeScreen()
        checkAndStartScanning()
    }

    override fun onDestroyView() {
        retractViewFromStatusBar()
        super.onDestroyView()
    }

    override fun onPause() {
        super.onPause()
        if (mIsBroadcastListenerRegistered) {
            requireActivity().unregisterReceiver(mBroadcastListener)
            mIsBroadcastListenerRegistered = false
        }
        PauseScheduler.pauseToggled = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        // User chose not to enable Bluetooth.
        if (requestCode == REQUEST_ENABLE_BT) {
            iv_bluetooth?.isSelected = resultCode == Activity.RESULT_OK
        }
        setupHomeScreen()
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

        setupHomeScreen()
    }

    private fun setupHomeScreen() {
        updatedPremLabels()
        updateAppWorking()
        loadHomeStats()
        loadGuidanceBanner()
    }

    private fun loadHomeStats() {
        val tintColor = if (PauseScheduler.withinPauseSchedule(requireContext())) {
            ContextCompat.getColor(requireContext(), R.color.primary_purple)
        } else {
            ContextCompat.getColor(requireContext(), R.color.final_blue)
        }

        rv_home_stats.adapter = HomeStatsAdapter(
                ::handleHomeStatsTapped,
                tintColor
        )

        homeViewModel.homeStats.observe(viewLifecycleOwner, Observer { homeStats ->
            (rv_home_stats.adapter as? HomeStatsAdapter)?.submitList(homeStats)
        })

        homeViewModel.getHomeStats()
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

                setupHomeScreen()
            }
            if (action == LocationManager.PROVIDERS_CHANGED_ACTION) {
                setupHomeScreen()
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
                (activity as MainActivity).getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private fun updatedPremLabels() {
        if (PauseScheduler.withinPauseSchedule(requireContext())) {
            permission_view.visibility = View.GONE
        } else {
            permission_view.visibility = View.VISIBLE
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
    }

    private fun checkAndStartScanning() {
        val appWorking = iv_location.isSelected && iv_bluetooth.isSelected
        val appPaused = PauseScheduler.withinPauseSchedule(requireContext())
        if (appWorking && !appPaused) {
            Utils.startBluetoothMonitoringService(requireContext())
        }
    }

    private fun updateAppWorking() {
        val appWorking = iv_location.isSelected && iv_bluetooth.isSelected
        val appPaused = PauseScheduler.withinPauseSchedule(requireContext())
        app_is_working_container.visibility = if (appWorking && !appPaused) View.VISIBLE else View.GONE
        app_is_not_working_container.visibility = if (appWorking || appPaused) View.GONE else View.VISIBLE
        app_is_paused_container.visibility = if (appPaused) View.VISIBLE else View.GONE
        Preference.getPauseEndTime(requireContext())?.let { endTime ->
            tv_paused_until?.text = "home_pause_until".getLocalizedText() + " " + endTime.formatTime()
        }
    }

    private fun loadGuidanceBanner() {
        homeViewModel.guidanceTile.observe(viewLifecycleOwner, Observer { guidanceTile ->
            if (guidanceTile != null) {
                tv_guidance_title.text = guidanceTile.title
                tv_guidance_details.text = guidanceTile.text
                banner_guidance.visibility = View.VISIBLE
            } else {
                banner_guidance.visibility = View.GONE
            }
        })

        homeViewModel.getGuidanceTile(requireContext())
    }

    private fun resumeDetection() {
        CentralLog.d(TAG, "Resumed - Starting Herald")
        Preference.setPauseScheduled(requireContext(), false)
        PauseScheduler.cancel(requireContext())
        PauseScheduler.togglePause(requireContext())
        setupHomeScreen()
    }

    private fun setUpView() {
        tv_app_is_working_title?.setLocalizedString("home_app_is_working")
        tv_app_is_not_working_title?.setLocalizedString("home_app_is_not_working")
        tv_app_is_paused_title?.setLocalizedString("home_pause_detection_paused")

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

        tv_resume_detection?.setLocalizedString("home_pause_resume")
        tv_resume_detection?.underline()

        tv_push_instructions_step1?.setLocalizedString("home_notification_permission_step1_android")
        tv_push_instructions_step2?.setLocalizedString("home_notification_permission_step2_android")
        tv_push_instructions_step3?.setLocalizedString("home_notification_permission_step3_android")
        btn_push_settings?.setLocalizedString("goto_app_settings")

        tv_share_app_title?.setLocalizedString("share_this_app")
        tv_upload_data_title?.setLocalizedString("home_upload_data")
        tv_upload_data_title?.underline()
        tv_upload_data_content?.setLocalizedString("home_upload_data_content")

        tv_pause_title?.setLocalizedString("home_pause_set_schedule")
        tv_pause_title?.underline()
        Preference.getPauseEndTime(requireContext())?.let { endTime ->
            tv_paused_until?.text = "home_pause_until".getLocalizedText() + " " + endTime.formatTime()
        }

        val versionLabel =
                "app_version_label".getLocalizedText() + BuildConfig.VERSION_NAME + Utils.getVersionSuffix()
        tv_app_version?.text = versionLabel

        tv_bluetooth?.setLocalizedString("home_bluetooth_permission")
        tv_location?.setLocalizedString("home_location_permission")
        tv_push?.setLocalizedString("home_notification_permission")

        app_working_buffer_view?.layoutParams?.height = statusBarHeight()
        app_paused_buffer_view?.layoutParams?.height = statusBarHeight()
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
        val perms = Utils.getLocationPermissionIdentifier()
        return EasyPermissions.hasPermissions(activity as MainActivity, *perms)
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager =
                requireContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return LocationManagerCompat.isLocationEnabled(locationManager)
    }

    private fun handleHomeStatsTapped(homeStats: HomeStats) {
        when (homeStats.type) {
            HomeStats.Type.CASES -> gotoCovidCases()
            HomeStats.Type.VACCINATIONS -> gotoVaccinations()
            HomeStats.Type.MAP -> gotoStatsMap()
            HomeStats.Type.DASHBOARD -> gotoStatsDashboard()
            else -> gotoStatsTab()
        }
    }

    private fun gotoStatsTab() {
        (activity as? MainActivity)?.goToStatsTab()
    }

    private fun goToLearnTab() {
        (activity as? MainActivity)?.goToLearnTab()
    }

    private fun gotoCovidCases() {
        val activity = activity as MainActivity
        activity.goToStatsTab()
        val fragment = CovidCasesFragment()
        addFragmentToBackStack(fragment)
    }

    private fun gotoVaccinations() {
        val activity = activity as MainActivity
        activity.goToStatsTab()
        val fragment = VaccinationsFragment()
        addFragmentToBackStack(fragment)
    }

    private fun gotoStatsMap() {
        val activity = activity as MainActivity
        activity.goToStatsTab()
        val fragment = StatsMapFragment()
        addFragmentToBackStack(fragment)
    }

    private fun gotoStatsDashboard() {
        val url = AppConstants.KEY_STATS.getUrl(requireContext(), getString(R.string.stats_url))
        val customTabsIntent: CustomTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
    }

    private fun goToUpload() {
        val fragment = ForUseFragment()
        addFragmentToBackStack(fragment)
        retractViewFromStatusBar()
    }

    private fun gotoPauseSchedule() {
        PauseDialogFragment().show(childFragmentManager, "pauseDialog")
    }

    private fun gotoExternalGuidance() {
        val url = Preference.getGuidanceTile(requireContext())?.link
                ?: getString(R.string.guidance_url)

        val builder: CustomTabsIntent.Builder = CustomTabsIntent.Builder()
        val customTabsIntent: CustomTabsIntent = builder.build()
        customTabsIntent.launchUrl(requireContext(), Uri.parse(url))
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

