package com.example.tracetogether.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_in_app_disclosure.*

class InAppDisclosureFragment : OnboardingFragmentInterface() {
    companion object {
        private const val TAG: String = "InAppDisclosureFragment"
    }

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun becomesVisible() {}

    override fun onButtonClick(buttonView: View) {
        val onboardActivity = context as OnboardingActivity?
        onboardActivity?.let {
            it.navigateToNextPage()
        } ?: (Utils.restartAppWithNoContext(
            0,
            "InAppDisclosureFragment not attached to OnboardingActivity"
        ))
    }

    override fun onBackButtonClick() {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_in_app_disclosure, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title?.setLocalizedString("in_app_disclosure_title")
        tv_item_1_title?.setLocalizedString("in_app_disclosure_bluetooth_title")
        tv_item_1_details?.setLocalizedString("in_app_disclosure_bluetooth_details")
        tv_item_2_title?.setLocalizedString("in_app_disclosure_location_title")
        tv_item_2_details?.setLocalizedString("in_app_disclosure_location_details")
        tv_item_3_title?.setLocalizedString("in_app_disclosure_phone_number_title")
        tv_item_3_details?.setLocalizedString("in_app_disclosure_phone_number_details")
        btn_next?.setLocalizedString("next_button")

        disableButton()

        checkbox_agreement?.setOnCheckedChangeListener { _, _ ->
            if (checkbox_agreement.isChecked) enableButton() else disableButton()
        }

        tv_agreement?.setOnClickListener {
            checkbox_agreement.toggle()
        }

        tv_agreement?.setLocalizedString("in_app_disclosure_agreement")
    }
}
