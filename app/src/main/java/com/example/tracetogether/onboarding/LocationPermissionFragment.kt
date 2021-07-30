package com.example.tracetogether.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.example.tracetogether.R
import com.example.tracetogether.util.Extensions.getLocalizedText
import com.example.tracetogether.util.Extensions.setLocalizedString
import kotlinx.android.synthetic.main.fragment_location_permission.*

class LocationPermissionFragment : OnboardingFragmentInterface() {
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            nextPage()
        }

    override fun onButtonClick(view: View) {
        requestLocation()
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
        return inflater.inflate(R.layout.fragment_location_permission, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tv_title.setLocalizedString("onboarding_location_title")
        tv_description.setLocalizedString("onboarding_location_description")
        btn_next.setLocalizedString("next_button")
    }

    private fun nextPage() {
        (context as OnboardingActivity?)?.navigateToNextPage()
    }

    private fun requestLocation() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                nextPage()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                showRationale()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
        }
    }

    private fun showRationale() {
        AlertDialog.Builder(requireActivity())
            .setMessage("permission_location_rationale".getLocalizedText())
            .setPositiveButton(android.R.string.ok) { _, _ ->
                requestPermissionLauncher.launch(
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            }
            .setNegativeButton(android.R.string.cancel) { _, _ -> }
            .create()
            .show()
    }
}
