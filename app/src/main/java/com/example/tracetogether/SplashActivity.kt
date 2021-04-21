package com.example.tracetogether

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.tracetogether.BuildConfig
import com.example.tracetogether.R
import com.example.tracetogether.herald.FairEfficacyInstrumentation
import com.example.tracetogether.api.Request
import com.example.tracetogether.onboarding.PreOnboardingActivity
import com.example.tracetogether.util.AppConstants.KEY_MHR
import com.example.tracetogether.util.AppConstants.LANG_EN
import com.example.tracetogether.util.LocalizationHandler
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.activity_splash.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.util.*

/*
    Entry activity of the application
 */
class SplashActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    private val SPLASH_TIME: Long = 1000
    var needToUpdateApp = false

    private lateinit var mHandler: Handler

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
        var lastSysLang = Preference.getSystemLang(this)
        var currentLang = Locale.getDefault().language

        if (currentLang.equals(LANG_EN, true) || lastSysLang.equals(currentLang, true)) {
            LocalizationHandler.getInstance().loadJSONFromAsset(applicationContext)
            LocalizationHandler.getInstance().getUpdatedData(applicationContext)

            loadNextScreen()
        } else {
            loader?.visibility = View.VISIBLE
            getUpdatedCopies()
        }
        tv_desc_sub?.setLocalizedString("tv_onboarding_desc_sub")
        Preference.putSystemLang(this, currentLang)

        if (FairEfficacyInstrumentation.enabled) {
            FairEfficacyInstrumentation.requestPermissions(this)
            return
        }
    }

    private fun loadNextScreen() {
        if (!needToUpdateApp && !FairEfficacyInstrumentation.enabled) {
            mHandler.postDelayed({
                goToNextScreen()
                finish()
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
        } else {
            startActivity(Intent(this, MainActivity::class.java))
        }
    }

    private fun getUpdatedCopies() = launch {
        var fileName: String = "strings-" + Locale.getDefault().language + ".json"
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
        val queryParams = HashMap<String, String>()
        queryParams["lang"] = Locale.getDefault().language
        val authResponse =
            Request.runRequest(
                "/adapters/applicationDataAdapter/getUrls",
                Request.GET,
                queryParams = queryParams
            )

        if (authResponse.isSuccess() && !TextUtils.isEmpty(authResponse?.data?.toString())) {
            Preference.setFeatureMHR(
                applicationContext,
                authResponse.data?.optBoolean(KEY_MHR) ?: false
            )
            Preference.putUrlData(
                applicationContext, authResponse?.data?.toString() ?: ""
            )

        }

    }
}
