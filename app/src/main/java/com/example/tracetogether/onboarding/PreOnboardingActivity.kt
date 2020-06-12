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

import kotlinx.coroutines.*

class PreOnboardingActivity : FragmentActivity() , CoroutineScope by MainScope() {

    private var TAG: String = "PreOnboardingActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_onboarding)

        tv_app_version?.text = getString(R.string.app_version_label) + BuildConfig.VERSION_NAME
        btn_onboardingStart?.setOnClickListener {
            var intent = Intent(this, HowItWorksActivity::class.java)
            startActivity(intent)
        }
        if(Preference.getUUIDRetryAttempts(applicationContext) > 2){
            btn_onboardingStart?.isEnabled = false
            tv_desc?.text = getString(R.string.tv_uuid_retry_error)
            tv_desc?.setTextColor(ContextCompat.getColor(this, R.color.error))
        }
    }
    override fun onResume(){
        super.onResume()
        showAppRegistrationStatus()
    }

    private fun showAppRegistrationStatus() = launch {
        if (!Utils.checkIfAppRegistered()) {
            CentralLog.i(TAG,"App version not supported")
            transparent_layer?.visibility = View.VISIBLE
            Utils.buildWrongVersionDialog(this@PreOnboardingActivity, getString(R.string.wrong_version_msg))
        }
    }
}
