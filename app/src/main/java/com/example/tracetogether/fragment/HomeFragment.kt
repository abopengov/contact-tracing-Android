package com.example.tracetogether.fragment

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.*
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.text.Spannable
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.URLSpan
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationManagerCompat
import androidx.core.text.HtmlCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import kotlinx.android.synthetic.main.fragment_home.*
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions
import com.example.tracetogether.*
import com.example.tracetogether.api.Request
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.onboarding.OnboardingActivity
import com.example.tracetogether.status.persistence.StatusRecord
import com.example.tracetogether.streetpass.persistence.StreetPassRecordDatabase
import com.example.tracetogether.util.AppConstants.KEY_HOME
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.getUrl
import com.example.tracetogether.util.Extensions.hide
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.show
import kotlinx.android.synthetic.main.watermark_foot.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

private const val REQUEST_ENABLE_BT = 123
private const val PERMISSION_REQUEST_ACCESS_LOCATION = 456

/*
    Fragment for the Home Screen
 */
class HomeFragment : Fragment(), CoroutineScope by MainScope() {
    private val TAG = "HomeFragment"
    private var url: String? = null

    private var mIsBroadcastListenerRegistered = false
    private var counter = 0
    private var animationWindow = 10000

    private lateinit var lastKnownScanningStarted: LiveData<StatusRecord?>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = StreetPassRecordDatabase.getDatabase(view.context)

        setUpView()

        lastKnownScanningStarted = db.statusDao().getMostRecentRecord("Scanning Started")
        lastKnownScanningStarted.observe(viewLifecycleOwner,
                Observer { record ->
                    if (record != null) {
                        tv_last_update?.visibility = View.VISIBLE
                        tv_last_update?.text =
                                "last_updated_label".getLocalizedText() + Utils.getTime(record.timestamp)
                        var now = System.currentTimeMillis()
                        CentralLog.d(TAG, record.timestamp.toString())
                        CentralLog.d(TAG, now.toString())
                        if (now - animationWindow >= record.timestamp && record.timestamp <= now + animationWindow) {
                            CentralLog.d(TAG, "Start animation")
                            animation_view?.playAnimation()
                        }
                    }
                })

        val versionSuffix = if (Utils.getServerURL().contains("stg")) ".S" else ""

        tv_app_version?.text =
                "app_version_label".getLocalizedText() + BuildConfig.VERSION_NAME + versionSuffix

        //R2 it was decided to remove battery optimizer view, we may use it in the future
//        switch_battery.setOnCheckedChangeListener { buttonView, isChecked ->
//            if (isChecked) {
//                val batteryIntent = Utils.getBatteryOptimizerExemptionIntent(TracerApp.AppContext.packageName)
//                if(Utils.canHandleIntent(batteryIntent, TracerApp.AppContext.packageManager)){
//                    this.startActivityForResult(batteryIntent, BATTERY_OPTIMISER)
//                }
//            } else {
//                switch_battery.isChecked = true;
//                alertDialog(getString(R.string.turn_off_battery))
//            }
//        }

        prem_info_button?.setOnClickListener {
            alertDialog("app_permission_hint".getLocalizedText())
        }

        showSetup()

        Preference.registerListener(activity!!.applicationContext, listener)
        showNonEmptyAnnouncement()

        btnSeeAllStats.setOnClickListener { goStatistics() }
        btnUploadData?.setOnClickListener { goToUpload() }

        watermark_foot_label?.setLocalizedString("tv_onboarding_desc_sub")
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false).apply {
            Preference.registerListener(activity!!.applicationContext, listener)

//            wvHighlights.webViewClient = WebViewClient()
//            wvHighlights.settings.javaScriptEnabled = true
//            wvHighlights.settings.loadWithOverviewMode = true
//            wvHighlights.webChromeClient = WebChromeClient()
//
//            if (context != null) {
//                url = KEY_HOME?.getUrl(context!!)
//            }
//            if (TextUtils.isEmpty(url)) {
//                requestUrl()
//            } else {
//                pgWebView?.hide()
//                if (TextUtils.isEmpty(url)) {
//                    highlights_view?.hide()
//                } else {
//                    wvHighlights?.loadUrl(url)
//                    highlights_view?.show()
//                }
//            }


        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        share_card_view?.setOnClickListener { shareThisApp() }

        animation_view?.setOnClickListener {
            if (BuildConfig.DEBUG && ++counter == 2) {
                counter = 0
                var intent = Intent(context, PeekActivity::class.java)
                context?.startActivity(intent)
            }
        }
        btn_restart_app_setup?.setOnClickListener {
            var intent = Intent(context, OnboardingActivity::class.java)
            intent.putExtra("page", 3)
            context?.startActivity(intent)
        }

        btn_announcement_close?.setOnClickListener {
            clearAndHideAnnouncement()
        }
    }

    private fun requestUrl() = launch {
        pgWebView?.isVisible = true
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language
        val authResponse =
                Request.runRequest(
                        "/adapters/applicationDataAdapter/getUrls",
                        Request.GET,
                        queryParams = queryParams
                )
        pgWebView?.isVisible = false


        if (authResponse.isSuccess()) {
            var homeUrl = authResponse.data?.optString(KEY_HOME) ?: ""
            if (TextUtils.isEmpty(homeUrl)) {
                highlights_view?.hide()
            } else {
                wvHighlights?.loadUrl(homeUrl)
                highlights_view?.show()
            }
        }

        tvErrorWebView?.isVisible = !authResponse.isSuccess()
        wvHighlights?.isVisible = authResponse.isSuccess()

    }


    private fun isShowRestartSetup(): Boolean {
        iv_bluetooth?.let {
            iv_location?.let {
                if (iv_bluetooth.isSelected && iv_location.isSelected) return false
            }
        }
        return true
    }

    fun showSetup() {
        updatedPremLabels()
        //Saving this for later incase we need it
//        view_setup.isVisible = isShowRestartSetup()
//        view_complete.isVisible = !isShowRestartSetup()
        view_complete?.isVisible = true
    }

    override fun onResume() {
        super.onResume()
        if (!mIsBroadcastListenerRegistered) {
            // bluetooth on/off
            var f = IntentFilter()
            f.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            activity!!.registerReceiver(mBroadcastListener, f)
            mIsBroadcastListenerRegistered = true
        }

        view?.let {
            //location permission
            val perms = Utils.getRequiredPermissions()
            iv_location?.isSelected =
                    EasyPermissions.hasPermissions(activity as MainActivity, *perms)

            //push notification
            iv_push?.isSelected =
                    NotificationManagerCompat.from(activity as MainActivity).areNotificationsEnabled()


            bluetoothAdapter?.let {
                iv_bluetooth.isSelected = !it.isDisabled
            }

            //battery ignore list
            val powerManager =
                    (activity as MainActivity).getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
            val packageName = (activity as MainActivity).packageName

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                battery_card_view?.visibility = View.VISIBLE
                if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
//                    switch_battery?.isChecked = false
                    CentralLog.d(TAG, "Not on Battery Optimization whitelist")
                } else {
//                    switch_battery?.isChecked = true
                    CentralLog.d(TAG, "On Battery Optimization whitelist")
                }
            } else {
//                battery_card_view?.visibility = View.GONE
            }

            showSetup()
        }
    }

    override fun onPause() {
        super.onPause()
        if (mIsBroadcastListenerRegistered) {
            activity!!.unregisterReceiver(mBroadcastListener)
            mIsBroadcastListenerRegistered = false
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Preference.unregisterListener(activity!!.applicationContext, listener)
        lastKnownScanningStarted.removeObservers(viewLifecycleOwner)
    }

    private fun shareThisApp() {
        var newIntent = Intent(Intent.ACTION_SEND)
        newIntent.type = "text/plain"
        newIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.app_name))
        var shareMessage = "share_message".getLocalizedText() + BuildConfig.SHARE_URL
        newIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
        startActivity(Intent.createChooser(newIntent, "Choose one"))
    }

    private val mBroadcastListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            CentralLog.d(TAG, intent.action.toString())
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                var state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)
                if (state == BluetoothAdapter.STATE_OFF) {
                    iv_bluetooth?.isSelected = false
                } else if (state == BluetoothAdapter.STATE_TURNING_OFF) {
                    iv_bluetooth?.isSelected = false
                } else if (state == BluetoothAdapter.STATE_ON) {
                    iv_bluetooth?.isSelected = true
                }

                showSetup()
            }
        }
    }

    private val bluetoothAdapter: BluetoothAdapter? by lazy(LazyThreadSafetyMode.NONE) {
        val bluetoothManager =
                (activity as MainActivity).getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val BluetoothAdapter.isDisabled: Boolean
        get() = !isEnabled

    private fun enableBluetooth() {
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        bluetoothAdapter?.let {
            if (it.isDisabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT)
            }
        }
    }

    @AfterPermissionGranted(PERMISSION_REQUEST_ACCESS_LOCATION)
    fun setupPermissionsAndSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val perms = Utils.getRequiredPermissions()
            if (EasyPermissions.hasPermissions(activity as MainActivity, *perms)) {
                // Already have permission, do the thing
            } else {
                // Do not have permissions, request them now
                EasyPermissions.requestPermissions(
                        this, "permission_location_rationale".getLocalizedText(),
                        PERMISSION_REQUEST_ACCESS_LOCATION, *perms
                )
            }
        }
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

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener =
            SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                when (key) {
                    "ANNOUNCEMENT" -> showNonEmptyAnnouncement()
                }
            }

    private fun clearAndHideAnnouncement() {
        view_announcement?.isVisible = false
        Preference.putAnnouncement(activity!!.applicationContext, "")
    }

    private fun showNonEmptyAnnouncement() {
        val new = Preference.getAnnouncement(activity!!.applicationContext)
        if (new.isEmpty()) return
        tv_announcement?.text = HtmlCompat.fromHtml(new, HtmlCompat.FROM_HTML_MODE_COMPACT)
        tv_announcement?.movementMethod = object : LinkMovementMethod() {
            override fun onTouchEvent(
                    widget: TextView?,
                    buffer: Spannable?,
                    event: MotionEvent?
            ): Boolean {
                if (event?.action == MotionEvent.ACTION_UP && widget != null && buffer != null) {
                    val x = event.x - widget.totalPaddingLeft + widget.scrollX
                    val y = event.y - widget.totalPaddingTop + widget.scrollY
                    val layout = widget.layout
                    val line = layout.getLineForVertical(y.toInt())
                    val off = layout.getOffsetForHorizontal(line, x)

                    val link: Array<out URLSpan> = buffer.getSpans(off, off, URLSpan::class.java)
                    if (link.isNotEmpty()) {
                        clearAndHideAnnouncement()
                    }
                }
                return super.onTouchEvent(widget, buffer, event)
            }
        }
        view_announcement?.isVisible = true
    }

    private fun updatedPremLabels() {
        var endTag = ""
        val yes = "yes".getLocalizedText()
        val no = "no".getLocalizedText()

//        tv_battery?.let {
//            switch_battery?.let {
//                endTag = if (switch_battery.isChecked) yes else no
//                tv_battery.text = HtmlCompat.fromHtml(
//                    getString(
//                        R.string.battery_optimiser,
//                        "<b>$endTag</b>"
//                    ), HtmlCompat.FROM_HTML_MODE_LEGACY
//                )
//            }
//        }

        tv_location?.let {
            endTag = if (iv_location.isSelected) yes else no
            tv_location.text = HtmlCompat.fromHtml(
                    "location_on".getLocalizedText().replace("%s", endTag)
                    , HtmlCompat.FROM_HTML_MODE_LEGACY
            )


        }

        tv_bluetooth?.let {
            endTag = if (iv_bluetooth.isSelected) yes else no
            tv_bluetooth.text = HtmlCompat.fromHtml(
                    "bluetooth_on".getLocalizedText().replace("%s", endTag)
                    , HtmlCompat.FROM_HTML_MODE_LEGACY
            )
        }

        tv_push?.let {
            endTag = if (iv_push.isSelected) yes else no
            tv_push.text = HtmlCompat.fromHtml(
                    "push_noti".getLocalizedText().replace("%s", endTag)
                    , HtmlCompat.FROM_HTML_MODE_LEGACY
            )


        }
    }

    private fun alertDialog(desc: String?) {
        val dialogBuilder = AlertDialog.Builder(activity as MainActivity)
        dialogBuilder.setMessage(desc)
                .setCancelable(false)
                .setPositiveButton("ok".getLocalizedText(),
                        DialogInterface.OnClickListener { dialog, id ->
                            dialog.dismiss()
                        })

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun goStatistics() {
        var activity = activity as MainActivity
        activity?.goStatistics()

    }

    private fun goToUpload() {
        var fragment = ForUseByOTCFragment()
        addFragmentToBackStack(fragment)
    }

    private fun setUpView() {
        tv_home_title?.setLocalizedString("home_title")
        tv_home_subtitle?.setLocalizedString("home_subtitle")
        info_title?.setLocalizedString("info_title")
        info_desc_1?.setLocalizedString("info_desc_1")
        info_desc_2?.setLocalizedString("info_desc_2")
        highlights?.setLocalizedString("highlights")
        tvUpdatedHighlights?.setLocalizedString("updated_label")
        tv_see_stats?.setLocalizedString("home_case_summary_button")
        tv_title?.setLocalizedString("app_permission_status")
        tv_location?.setLocalizedString("location_on")
        tv_bluetooth?.setLocalizedString("bluetooth_on")
        tv_push?.setLocalizedString("push_noti")
        share_this_app?.setLocalizedString("share_this_app")
        ask_friend?.setLocalizedString("ask_friend")
        help_us_identify?.setLocalizedString("help_us_identify")
        btn_restart_app_setup?.setLocalizedString("restart_app_setup")
        tvErrorWebView?.setLocalizedString("failed_to_load_data")
        setup_incomplete?.setLocalizedString("setup_incomplete")
        tv_announcement?.setLocalizedString("no_news")
        tv_home_upload?.setLocalizedString("home_upload_title")
        uploadData?.setLocalizedString("menu_upload")
        btn_uplaod_data?.setLocalizedString("menu_upload")
    }

    private fun addFragmentToBackStack(fragment: Fragment) {
        val fragManager: FragmentManager? = activity?.supportFragmentManager
        val fragTrans: FragmentTransaction? = fragManager?.beginTransaction()
        fragTrans?.add(R.id.content, fragment)
        fragTrans?.addToBackStack(fragment::class.java.name)
        fragTrans?.commit()
    }
}
