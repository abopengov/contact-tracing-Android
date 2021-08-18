package com.example.tracetogether.onboarding

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.tracetogether.Preference
import com.example.tracetogether.BuildConfig
import kotlinx.android.synthetic.main.main_activity_onboarding.*
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.Extensions.getLocalizedText
import kotlinx.android.synthetic.main.watermark_foot.*
import kotlinx.coroutines.*

class PreOnboardingActivity : FragmentActivity(), CoroutineScope by MainScope() {

    private var TAG: String = "PreOnboardingActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_onboarding)

        val versionLabel = "app_version_label".getLocalizedText() + BuildConfig.VERSION_NAME + Utils.getVersionSuffix()

        tv_app_version?.text = versionLabel
        tv_title?.setLocalizedString("tv_onboarding_title")
        tv_desc?.setLocalizedString("tv_onboarding_desc")
        btn_next?.setLocalizedString("i_want_to_help")
        watermark_foot_label?.setLocalizedString("tv_onboarding_desc_sub")


        btn_next?.setOnClickListener {
            var intent = Intent(this, HowItWorksActivity::class.java)
            startActivity(intent)
        }
        if (Preference.getUUIDRetryAttempts(applicationContext) > 2) {
            btn_next?.isEnabled = false
            tv_desc?.setLocalizedString("tv_uuid_retry_error")
            tv_desc?.setTextColor(ContextCompat.getColor(this, R.color.error))
        }
    }

    override fun onResume() {
        super.onResume()
        showAppRegistrationStatus()
    }

    private fun showAppRegistrationStatus() = launch {
        if (!Utils.checkIfAppRegistered()) {
            CentralLog.i(TAG, "App version not supported")
            transparent_layer?.visibility = View.VISIBLE
            Utils.buildWrongVersionDialog(
                this@PreOnboardingActivity,
                "wrong_version_msg".getLocalizedText()
            )
        }
    }
}
