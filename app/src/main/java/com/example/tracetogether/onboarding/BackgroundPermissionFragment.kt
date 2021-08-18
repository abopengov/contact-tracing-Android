package com.example.tracetogether.onboarding

import android.app.Activity
import android.os.Bundle
import android.os.PowerManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.tracetogether.R
import com.example.tracetogether.Utils
import com.example.tracetogether.logging.CentralLog
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_background_permission.*

class BackgroundPermissionFragment : OnboardingFragmentInterface() {
    companion object {
        private const val TAG = "BackgroundPermissionFragment"
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            CentralLog.d(
                TAG,
                "Battery Optimization Result ${if (result.resultCode == Activity.RESULT_OK) "Success" else "Failure"}"
            )

            nextPage()
        }

    override fun onButtonClick(view: View) {
        excludeFromBatteryOptimization()
    }

    override fun onBackButtonClick() {}

    override fun becomesVisible() {}

    override fun onUpdatePhoneNumber(num: String) {}

    override fun onError(error: String) {}

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_background_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title.setLocalizedString("onboarding_background_title")
        tv_description.setLocalizedString("onboarding_background_description")
        btn_next.setLocalizedString("next_button")
    }

    private fun nextPage() {
        (context as OnboardingActivity?)?.navigateToNextPage()
    }

    private fun excludeFromBatteryOptimization() {
        CentralLog.d(TAG, "[excludeFromBatteryOptimization] ")
        val powerManager =
            requireContext().getSystemService(AppCompatActivity.POWER_SERVICE) as PowerManager
        val packageName = requireContext().packageName
        val intent = Utils.getBatteryOptimizerExemptionIntent(packageName)

        if (!powerManager.isIgnoringBatteryOptimizations(packageName)) {
            CentralLog.d(TAG, "Not on Battery Optimization whitelist")
            if (Utils.canHandleIntent(
                    intent,
                    requireContext().packageManager
                )
            ) {
                requestPermissionLauncher.launch(intent)
            } else {
                CentralLog.d(TAG, "No handler for Battery Optimization")

                nextPage()
            }
        } else {
            CentralLog.d(TAG, "On Battery Optimization whitelist")
            nextPage()
        }
    }
}
