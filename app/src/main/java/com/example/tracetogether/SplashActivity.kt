package com.example.tracetogether

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.Observer
import com.example.tracetogether.api.Request
import com.example.tracetogether.herald.FairEfficacyInstrumentation
import com.example.tracetogether.home.HomeViewModel
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.network.UrlsApi
import com.example.tracetogether.onboarding.PreOnboardingActivity
import com.example.tracetogether.onboarding.PrivacyFragment
import com.example.tracetogether.privacy.PrivacyViewModel
import com.example.tracetogether.util.AppConstants.LANG_EN
import com.example.tracetogether.util.Extensions.setLocalizedString
import com.example.tracetogether.util.LocalizationHandler
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.collections.HashMap

/*
    Entry activity of the application
 */
class SplashActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private val SPLASH_TIME: Long = 1000
    var needToUpdateApp = false

    private lateinit var mHandler: Handler
    private val privacyViewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        mHandler = Handler()

        //check if the intent was from notification and its a update notification
        intent.extras?.let {
            val notifEvent: String? = it.getString("event", null)

            notifEvent?.let {
                if (it.equals("update")) {
                    needToUpdateApp = true
                    intent = Intent(Intent.ACTION_VIEW);
                    //Copy App URL from Google Play Store.
                    intent.data = Uri.parse(BuildConfig.STORE_URL)

                    startActivity(intent)
                    finish()
                }
            }
        }

        requestUrl()

        privacyViewModel.newPrivacyPolicyAvailable.observe(this, Observer { newVersionAvailable ->
            if (newVersionAvailable) {
                showPrivacyPolicyFragment()
            } else {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        })

        tv_desc_sub?.setLocalizedString("tv_onboarding_desc_sub")
        Preference.putSystemLang(this, Locale.getDefault().language)

        if (FairEfficacyInstrumentation.enabled) {
            FairEfficacyInstrumentation.requestPermissions(this)
            return
        }
    }

    override fun onResume() {
        super.onResume()

        val lastSysLang = Preference.getSystemLang(this)
        val currentLang = Locale.getDefault().language

        if (currentLang.equals(LANG_EN, true) || lastSysLang.equals(currentLang, true)) {
            LocalizationHandler.getInstance().loadJSONFromAsset(applicationContext)
            LocalizationHandler.getInstance().getUpdatedData(applicationContext)

            loadNextScreen()
        } else {
            loader?.visibility = View.VISIBLE
            getUpdatedCopies()
        }
    }

    private fun loadNextScreen() {
        if (!needToUpdateApp && !FairEfficacyInstrumentation.enabled) {
            mHandler.postDelayed({
                goToNextScreen()
            }, SPLASH_TIME)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (FairEfficacyInstrumentation.enabled) {
            FairEfficacyInstrumentation.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
            )
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        mHandler.removeCallbacksAndMessages(null)
    }

    private fun goToNextScreen() {
        if (!Preference.isOnBoarded(this)) {
            startActivity(Intent(this, PreOnboardingActivity::class.java))
            finish()
        } else {
            privacyViewModel.checkPrivacyPolicy()
        }
    }

    private fun getUpdatedCopies() = launch {
        val fileName: String = "strings-" + Locale.getDefault().language + ".json"
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language

        val authResponse =
            Request.runRequest(
                "/adapters/applicationDataAdapter/getContent",
                Request.GET,
                queryParams = queryParams
            )

        if (authResponse.isSuccess() && !TextUtils.isEmpty(authResponse?.data?.toString())) {
            val file = File(applicationContext?.filesDir, fileName)
            file.writeText(authResponse?.data?.toString() ?: "")
            Preference.putLocalizationFetchTime(applicationContext, System.currentTimeMillis())
        }
        //incase api fails, use the local file
        LocalizationHandler.getInstance().loadJSONFromAsset(applicationContext)

        loader?.visibility = View.GONE
        loadNextScreen()
    }

    private fun requestUrl() = launch {
        if (System.currentTimeMillis() - Preference.getLastFetchedUrlDataTimestamp(applicationContext)
                > Utils.getUrlsCacheDuration()) {
            try {
                UrlsApi.service.retrieveAndCacheUrls(applicationContext)
            } catch (ex: Exception) {
                CentralLog.e(HomeViewModel.TAG, "Failed to get URLs: " + ex.message)
            }
        }
    }

    private fun showPrivacyPolicyFragment() {
        val fragmentTransaction: FragmentTransaction? = supportFragmentManager.beginTransaction()
        val privacyFragment = PrivacyFragment()

        fragmentTransaction?.replace(R.id.fragment_container, privacyFragment)
        fragmentTransaction?.commit()
    }
}
