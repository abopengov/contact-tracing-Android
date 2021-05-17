package com.example.tracetogether.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import kotlinx.android.synthetic.main.main_activity_howitworks.*
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString

class HowItWorksActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_howitworks)

        app_will_title?.setLocalizedString("howitworks_will_title")
        app_not_title?.setLocalizedString("howitworks_not_title")
        will_sublabel1_text?.setLocalizedString("howitworks_will_sublabel1")
        will_sublabel2_text?.setLocalizedString("howitworks_will_sublabel2")
        will_sublabel3_text?.setLocalizedString("howitworks_will_sublabel3")
        will_sublabel4_text?.setLocalizedString("howitworks_will_sublabel4")
        not_sublabel1_text?.setLocalizedString("howitworks_not_sublabel1")
        not_sublabel2_text?.setLocalizedString("howitworks_not_sublabel2")
        continue_button?.setLocalizedString("continue_button")

        btn_onboardingStart?.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }
    }
}
