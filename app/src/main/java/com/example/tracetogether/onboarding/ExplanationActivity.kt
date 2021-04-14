package com.example.tracetogether.onboarding

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.main_activity_explanation.*
import kotlinx.android.synthetic.main.main_activity_howitworks.btn_onboardingStart
import kotlinx.android.synthetic.main.main_activity_howitworks.continue_button
import kotlinx.android.synthetic.main.main_activity_howitworks.will_sublabel1_text
import kotlinx.android.synthetic.main.main_activity_howitworks.will_sublabel2_text
import kotlinx.android.synthetic.main.main_activity_howitworks.will_sublabel3_text

class ExplanationActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity_explanation)

        app_will_ask_title?.setLocalizedString("explanation_will_ask_title")

        will_sublabel1_text?.setLocalizedString("explanation_sublabel1")
        will_info_label1_text?.setLocalizedString("explanation_info_label1")

        will_sublabel2_text?.setLocalizedString("explanation_sublabel2")
        will_info_label2_text?.setLocalizedString("explanation_info_label2")

        will_sublabel3_text?.setLocalizedString("explanation_sublabel3")
        will_info_label3_text?.setLocalizedString("explanation_info_label3")

        will_sublabel4_text?.setLocalizedString("explanation_sublabel4")
        will_info_label4_text?.setLocalizedString("explanation_info_label4")

        continue_button?.setLocalizedString("continue_button")

        btn_onboardingStart?.setOnClickListener {
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
        }
    }
}
